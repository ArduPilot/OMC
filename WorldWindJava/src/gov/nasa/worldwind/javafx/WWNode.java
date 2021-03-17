/*
 * Copyright (C) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.javafx;

import com.sun.javafx.tk.Toolkit;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.util.*;
import javafx.application.Platform;
import javafx.beans.value.*;
import javafx.event.*;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import com.jogamp.opengl.*;
import java.beans.*;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class WWNode extends Region implements WorldWindow, Sink
{
    private static final Field prismImagePixelBufferField;
    private static final Method pixelsDirtyMethod;

    static
    {
        Method method = null;
        Field pixelBufferField = null;

        try
        {
            method = Image.class.getDeclaredMethod("pixelsDirty");
            method.setAccessible(true);

            pixelBufferField = com.sun.prism.Image.class.getDeclaredField("pixelBuffer");
            pixelBufferField.setAccessible(true);
        }
        catch (ReflectiveOperationException e)
        {
            e.printStackTrace();
        }

        pixelsDirtyMethod = method;
        prismImagePixelBufferField = pixelBufferField;
    }

    private WWOffscreenDrawable wwd;
    private WritableImage writableImage;
    private com.sun.prism.Image platformImage;
    private ByteBuffer pixelBuffer;
    private final ImageView imageView = new ImageView();
    private final Object bufferSyncToken = new Object();
    private final Queue<Runnable> sinkQueue = new ArrayDeque<>();
    private final Object syncToken = new Object();
    private boolean renderingEnabled;
    private volatile boolean exitThreadRequested;
    private ResetEvent sceneDirtyEvent = new ResetEvent();
    private Thread redrawThread;

    private Model cachedModel;
    private View cachedView;
    private InputHandler inputHandler;
    private SceneController sceneController;

    private final EventHandler<Event> requestFocusHandler = new EventHandler<Event>()
    {
        @Override
        public void handle(Event event)
        {
            if (!isFocused()) {
                requestFocus();
            }
        }
    };

    public WWNode()
    {
        setScaleY(-1);

        wwd = new WWOffscreenDrawable(WorldWind.getScheduledTaskService(), 0, 0);
        wwd.setFrameRenderedHandler(this::updateBuffer);
        wwd.initGpuResourceCache(WWOffscreenDrawable.getSharedGpuResourceCache());
        wwd.addRedrawListener(this::redrawRequested);

        sceneController = (SceneController)WorldWind.createConfigurationComponent(
            AVKey.SCENE_CONTROLLER_CLASS_NAME);
        wwd.setSceneController(sceneController);

        createDefaultInputHandler();
        WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, wwd);

        // Set up to initiate a repaint whenever a file is retrieved and added to the local file store.
        WorldWind.getDataFileStore().addPropertyChangeListener(wwd);

        addEventHandler(MouseEvent.MOUSE_PRESSED, requestFocusHandler);
        addEventHandler(TouchEvent.TOUCH_PRESSED, requestFocusHandler);
        addEventHandler(WindowEvent.WINDOW_HIDING, event -> shutdown());
        sceneProperty().addListener(this::onSceneChanged);
        widthProperty().addListener(this::onSizeChanged);
        heightProperty().addListener(this::onSizeChanged);
        imageView.setSmooth(false);
        getChildren().add(imageView);

        redrawThread = new Thread("WWNode render thread")
        {
            @Override
            public void run()
            {
                try
                {
                    while (!exitThreadRequested)
                    {
                        sceneDirtyEvent.await();
                        sceneDirtyEvent.reset();
                        processSinkQueue();
                        wwd.redraw();
                    }
                }
                catch (InterruptedException e)
                {
                    // let the thread terminate
                }
            }
        };
    }

    public Thread getRenderThread()
    {
        return redrawThread;
    }

    public void startRendering()
    {
        synchronized (syncToken)
        {
            if (!renderingEnabled)
            {
                renderingEnabled = true;
                redrawThread.start();
            }
        }
    }

    /** Constructs and attaches the {@link View} for this <code>WorldWindow</code>. */
    protected void createView()
    {
        this.setView((View)WorldWind.createConfigurationComponent(AVKey.VIEW_CLASS_NAME));
    }

    /** Constructs and attaches the {@link InputHandler} for this <code>WorldWindow</code>. */
    protected void createDefaultInputHandler()
    {
        this.setInputHandler(new JfxInputHandler());
    }

    private void onSceneChanged(ObservableValue<? extends Scene> observable, Scene oldScene, Scene newScene)
    {
        if (newScene == null) {
            shutdown();
        }
    }

    private void onSizeChanged(ObservableValue<? extends Number> observable, Number oldSize, Number newSize)
    {
        sizeInvalidated();
    }

    private void redrawRequested(int millis) {
        if (millis == 0) {
            redraw();
        } else {
            new JfxTimer(Duration.millis(millis), this::redraw).start();
        }
    }

    private void updateBuffer(ByteBuffer pixels, int width, int height) {
        synchronized (bufferSyncToken)
        {
            if (pixelBuffer == null
                    || (int)writableImage.getWidth() != width
                    || (int)writableImage.getHeight() != height) {
                return;
            }

            pixels.get(pixelBuffer.array());
            updateDirtyFlag();
        }
    }

    private void updateDirtyFlag()
    {
        if (!Platform.isFxApplicationThread())
        {
            Platform.runLater(this::updateDirtyFlag);
        }
        else {
            try {
                platformImage
                    .getSerial()
                    .update(new com.sun.javafx.geom.Rectangle(
                            0, 0, platformImage.getWidth(), platformImage.getHeight()));
                pixelsDirtyMethod.invoke(writableImage);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void sizeInvalidated() {
        int width;
        int height;

        synchronized (bufferSyncToken)
        {
            final Scene scene = getScene();
            if (scene == null)
            {
                return;
            }

            final Window window = scene.getWindow();
            if (window == null)
            {
                return;
            }

            width = (int)(getWidth() * window.getOutputScaleX());
            height = (int)(getHeight() * window.getOutputScaleY());

            if (width > 0 && height > 0)
            {
                writableImage = new WritableImage(width, height);
                platformImage = (com.sun.prism.Image)Toolkit.getImageAccessor().getPlatformImage(writableImage);
                pixelBuffer = getPixelBuffer(writableImage);
                imageView.setImage(writableImage);
                imageView.setFitWidth(getWidth());
                imageView.setFitHeight(getHeight());
            }
            else
            {
                writableImage = null;
                platformImage = null;
                pixelBuffer = null;
                imageView.setImage(null);
            }
        }

        if (width > 0 && height > 0) {
            wwd.setSize(width, height);
            redraw();
        }
    }

    private static ByteBuffer getPixelBuffer(WritableImage image) {
        try
        {
            return (ByteBuffer)prismImagePixelBufferField.get(
                    Toolkit.getImageAccessor().getImageProperty(image).get());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void verifyAccess(String methodName)
    {
        if (Thread.currentThread() != redrawThread)
        {
            throw new RuntimeException(
                getClass().getName() + "." + methodName
                    + "() can only be called on the rendering thread.");
        }
    }

    @Override
    public void accept(Runnable runnable)
    {
        if (Thread.currentThread() == redrawThread)
        {
            runnable.run();
        }
        else
        {
            synchronized (sinkQueue) {
                sinkQueue.add(runnable);
            }
        }

        sceneDirtyEvent.set();
    }

    @Override
    public void acceptAndWait(Runnable runnable)
    {
        if (Thread.currentThread() == redrawThread)
        {
            runnable.run();
            sceneDirtyEvent.set();
        }
        else
        {
            var awaitableRunnable = new AwaitableRunnable(runnable);

            synchronized (sinkQueue) {
                sinkQueue.add(awaitableRunnable);
            }

            sceneDirtyEvent.set();
            awaitableRunnable.await();
        }
    }

    private void processSinkQueue()
    {
        List<Runnable> list;

        synchronized (sinkQueue)
        {
            list = new ArrayList<>(sinkQueue);
            sinkQueue.clear();
        }

        for (Runnable runnable : list) {
            runnable.run();
        }
    }

    @Override
    public void setModel(Model model)
    {
        cachedModel = model;
        accept(() -> wwd.setModel(model));
    }

    @Override
    public Model getModel()
    {
        return cachedModel;
    }

    @Override
    public synchronized void setView(View view)
    {
        cachedView = view;
        accept(() -> wwd.setView(view));
    }

    @Override
    public View getView()
    {
        return cachedView;
    }

    @Override
    public synchronized void setModelAndView(Model model, View view)
    {
        cachedModel = model;
        cachedView = view;
        accept(() -> {
            wwd.setModel(model);
            wwd.setView(view);
        });
    }

    @Override
    public SceneController getSceneController()
    {
        return sceneController;
    }

    @Override
    public void setSceneController(SceneController sceneController)
    {
        this.sceneController = sceneController;
        accept(() -> wwd.setSceneController(sceneController));
    }

    @Override
    public InputHandler getInputHandler()
    {
        return inputHandler;
    }

    @Override
    public void setInputHandler(InputHandler inputHandler)
    {
        if (this.inputHandler != null) {
            if (this.inputHandler instanceof JfxInputHandler) {
                ((JfxInputHandler)this.inputHandler).setSink(null);
            }

            this.inputHandler.setEventSource(null);
        }

        if (inputHandler != null) {
            if (inputHandler instanceof JfxInputHandler) {
                ((JfxInputHandler)inputHandler).setSink(this);
            }

            this.inputHandler = inputHandler;
            inputHandler.setEventSource(this);
        } else {
            this.inputHandler = new NoOpInputHandler();
        }
    }

    @Override
    public void addRenderingListener(RenderingListener listener)
    {
        wwd.addRenderingListener(listener);
    }

    @Override
    public void removeRenderingListener(RenderingListener listener)
    {
        wwd.removeRenderingListener(listener);
    }

    @Override
    public void addSelectListener(SelectListener listener)
    {
        inputHandler.addSelectListener(listener);
        wwd.addSelectListener(listener);
    }

    @Override
    public void removeSelectListener(SelectListener listener)
    {
        inputHandler.removeSelectListener(listener);
        wwd.removeSelectListener(listener);
    }

    @Override
    public void addPositionListener(PositionListener listener)
    {
        wwd.addPositionListener(listener);
    }

    @Override
    public void removePositionListener(PositionListener listener)
    {
        wwd.removePositionListener(listener);
    }

    @Override
    public void redraw()
    {
        synchronized (syncToken) {
            if (redrawThread != null) {
                sceneDirtyEvent.set();
            } else {
                updateDirtyFlag();
            }
        }
    }

    @Override
    public void redrawNow()
    {
        synchronized (syncToken) {
            if (redrawThread != null) {
                sceneDirtyEvent.set();
            } else {
                updateDirtyFlag();
            }
        }
    }

    @Override
    public Position getCurrentPosition()
    {
        verifyAccess("getCurrentPosition");
        return wwd.getCurrentPosition();
    }

    @Override
    public PickedObjectList getObjectsAtCurrentPosition()
    {
        verifyAccess("getObjectsAtCurrentPosition");
        SceneController sceneController = wwd.getSceneController();
        return sceneController != null ? sceneController.getPickedObjectList() : null;
    }

    @Override
    public PickedObjectList getObjectsInSelectionBox()
    {
        verifyAccess("getObjectsInSelectionBox");
        SceneController sceneController = wwd.getSceneController();
        return sceneController != null ? sceneController.getObjectsInPickRectangle() : null;
    }

    @Override
    public GpuResourceCache getGpuResourceCache()
    {
        verifyAccess("getGpuResourceCache");
        return wwd.getGpuResourceCache();
    }

    @Override
    public void setPerFrameStatisticsKeys(Set<String> keys)
    {
        verifyAccess("setPerFrameStatisticsKeys");
        wwd.setPerFrameStatisticsKeys(keys);
    }

    @Override
    public Collection<PerformanceStatistic> getPerFrameStatistics()
    {
        verifyAccess("getPerFrameStatistics");
        return wwd.getPerFrameStatistics();
    }

    @Override
    public void shutdown()
    {
        WorldWind.getDataFileStore().removePropertyChangeListener(wwd);
        wwd.shutdown();

        try
        {
            exitThreadRequested = true;
            sceneDirtyEvent.set();
            redrawThread.join();

            if (inputHandler != null) {
                inputHandler.dispose();
            }
        }
        catch (InterruptedException ignored)
        {
        }

        redrawThread = null;
        renderingEnabled = false;
    }

    @Override
    public void addRenderingExceptionListener(RenderingExceptionListener listener)
    {
        verifyAccess("addRenderingExceptionListener");
        wwd.addRenderingExceptionListener(listener);
    }

    @Override
    public void removeRenderingExceptionListener(RenderingExceptionListener listener)
    {
        verifyAccess("removeRenderingExceptionListener");
        wwd.removeRenderingExceptionListener(listener);
    }

    @Override
    public GLContext getContext()
    {
        verifyAccess("getContext");
        return wwd.getContext();
    }

    @Override
    public boolean isEnableGpuCacheReinitialization()
    {
        verifyAccess("isEnableGpuCacheReinitialization");
        return wwd.isEnableGpuCacheReinitialization();
    }

    @Override
    public void setEnableGpuCacheReinitialization(boolean enableGpuCacheReinitialization)
    {
        verifyAccess("setEnableGpuCacheReinitialization");
        wwd.setEnableGpuCacheReinitialization(enableGpuCacheReinitialization);
    }

    @Override
    public Object setValue(String key, Object value)
    {
        return wwd.setValue(key, value);
    }

    @Override
    public AVList setValues(AVList avList)
    {
        return wwd.setValues(avList);
    }

    @Override
    public Object getValue(String key)
    {
        return wwd.getValue(key);
    }

    @Override
    public Collection<Object> getValues()
    {
        return wwd.getValues();
    }

    @Override
    public String getStringValue(String key)
    {
        return wwd.getStringValue(key);
    }

    @Override
    public Set<Map.Entry<String, Object>> getEntries()
    {
        return wwd.getEntries();
    }

    @Override
    public boolean hasKey(String key)
    {
        return wwd.hasKey(key);
    }

    @Override
    public Object removeKey(String key)
    {
        return wwd.removeKey(key);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        wwd.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        wwd.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        wwd.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        wwd.removePropertyChangeListener(listener);
    }

    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        wwd.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
    {
        wwd.firePropertyChange(propertyChangeEvent);
    }

    @Override
    public AVList copy()
    {
        return wwd.copy();
    }

    @Override
    public AVList clearList()
    {
        return wwd.clearList();
    }

}
