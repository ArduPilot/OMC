/*
 * Copyright (C) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.javafx;

import com.jogamp.opengl.GLContext;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.sg.prism.NGNode;
import gov.nasa.worldwind.AbstractSceneController;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.event.InputHandler;
import gov.nasa.worldwind.event.NoOpInputHandler;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.PerformanceStatistic;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class WWGLNode extends Node implements WorldWindow, EventSink {

    private final DoubleProperty width = new SimpleDoubleProperty(this, "width");
    private final DoubleProperty height = new SimpleDoubleProperty(this, "height");
    private final WWGLDrawable mainDrawable;
    private final WWGLDrawable imageDrawable;
    private final Deque<WorkItem> sinkQueue = new ArrayDeque<>();
    private final AtomicBoolean redrawRequested = new AtomicBoolean();
    private final Thread redrawThread;
    private final ResetEvent sceneDirtyEvent = new ResetEvent();
    private volatile boolean exitThreadRequested;
    private boolean renderingEnabled;
    private boolean deferFlag;
    private int displayedFrames;

    private Model cachedModel;
    private View cachedView;
    private InputHandler inputHandler;
    private SceneController sceneController;

    @SuppressWarnings("FieldCanBeLocal")
    private final EventHandler<Event> requestFocusHandler =
        event -> {
            if (!isFocused()) {
                requestFocus();
            }
        };

    static {
        WWGLNodeHelper.setAccessor(new WWGLNodeHelper.WWGLNodeAccessor() {
            @Override
            public NGNode doCreatePeer(Node node) {
                return ((WWGLNode)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds bounds, BaseTransform tx) {
                return ((WWGLNode)node).doComputeGeomBounds(tx);
            }

            @Override
            public boolean doComputeContains(Node node, double x, double y) {
                return ((WWGLNode)node).doComputeContains(x, y);
            }

            @Override
            public void doMarkDirty(Node node, DirtyBits dirtyBits) {
            }
        });
    }

    public WWGLNode() {
        WWGLNodeHelper.initHelper(this);

        imageDrawable = new WWGLDrawable(null, 0, 0);
        imageDrawable.initGpuResourceCache(WWContext.getInstance().getSharedGpuResourceCache());
        imageDrawable.setSceneController(new AbstractSceneController() {
            @Override
            protected void doRepaint(DrawContext dc) {
                this.applyView(dc);
                this.createTerrain(dc);
                this.preRender(dc);
                this.clearFrame(dc);
                this.draw(dc);
            }
        });

        mainDrawable = new WWGLDrawable(WorldWind.getScheduledTaskService(), 0, 0);
        mainDrawable.initGpuResourceCache(WWContext.getInstance().getSharedGpuResourceCache());
        mainDrawable.addRedrawListener(this::redrawRequested);
        mainDrawable.setFrameRenderedHandler(this::onFrameRendered);

        setSceneController((SceneController)WorldWind.createConfigurationComponent(AVKey.SCENE_CONTROLLER_CLASS_NAME));
        setInputHandler(new JfxInputHandler(false, false));

        WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, mainDrawable);

        // Set up to initiate a repaint whenever a file is retrieved and added to the local file store.
        WorldWind.getDataFileStore().addPropertyChangeListener(mainDrawable);

        addEventHandler(MouseEvent.MOUSE_PRESSED, requestFocusHandler);
        addEventHandler(TouchEvent.TOUCH_PRESSED, requestFocusHandler);
        addEventHandler(WindowEvent.WINDOW_HIDING, event -> shutdown());
        sceneProperty().addListener(this::onSceneChanged);
        width.addListener(this::onSizeChanged);
        height.addListener(this::onSizeChanged);

        redrawThread =
            new Thread("WWGLNode render thread") {
                @Override
                public void run() {
                    try {
                        while (!exitThreadRequested) {
                            sceneDirtyEvent.await();
                            sceneDirtyEvent.reset();

                            processSinkQueue();

                            // This loop could run many times, so don't forget to process the event queue.
                            while (redrawRequested.compareAndSet(true, false)) {
                                mainDrawable.redraw();
                                processSinkQueue();
                            }
                        }
                    } catch (InterruptedException e) {
                        // let the thread terminate
                    }
                }
            };

        redrawThread.setDaemon(true);
    }

    public int getDisplayedFrames() {
        return displayedFrames;
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public double getWidth() {
        return width.get();
    }

    public void setWidth(double width) {
        this.width.set(width);
    }

    public double getHeight() {
        return height.get();
    }

    public void setHeight(double height) {
        this.height.set(height);
    }

    public Thread getRenderThread() {
        return redrawThread;
    }

    public void startRendering() {
        verifyAccess("startRendering", true);

        if (!renderingEnabled) {
            renderingEnabled = true;
            redrawThread.start();
        }
    }

    public void snapshot(int width, int height, Consumer<Image> snapshot) {
        snapshot(width, height, getModel(), getView(), snapshot);
    }

    public void snapshot(int width, int height, Model model, Consumer<Image> snapshot) {
        snapshot(width, height, model, getView(), snapshot);
    }

    public void snapshot(int width, int height, Model model, View view, Consumer<Image> snapshot) {
        accept(() -> {
            Model mainModel = getModel();
            View mainView = getView();

            try {
                imageDrawable.setFrameRenderedHandler(new WWGLDrawable.FrameRenderedHandler() {
                    @Override
                    public void frameRendered(int textureHandle, Lock lock) {
                        GLHelper helper = new GLHelper(WWGLNode.this, imageDrawable.getContext().getGL().getGL3());
                        snapshot.accept(helper.getTextureImage(textureHandle));
                    }
                });

                imageDrawable.setModelAndView(model, view);
                imageDrawable.setSize(width, height);
                imageDrawable.redrawNow();
            } finally {
                imageDrawable.setModelAndView(null, null);
                mainDrawable.setModelAndView(mainModel, mainView);
            }
        }, false);
    }

    @SuppressWarnings("unused")
    private void onSceneChanged(ObservableValue<? extends Scene> observable, Scene oldScene, Scene newScene) {
        if (newScene == null) {
            shutdown();
        }
    }

    @SuppressWarnings("unused")
    private void onSizeChanged(ObservableValue<? extends Number> observable, Number oldSize, Number newSize) {
        NodeHelper.markDirty(WWGLNode.this, DirtyBits.NODE_FORCE_SYNC);
        NodeHelper.geomChanged(WWGLNode.this);
        accept(() -> mainDrawable.setSize((int) width.get(), (int) height.get()), false);
    }

    private void redrawRequested(int millis) {
        if (millis == 0) {
            redraw();
        } else {
            new JfxTimer(Duration.millis(millis), this::redraw).start();
        }
    }

    private void onFrameRendered(int texture, Lock lock) {
        Platform.runLater(
            () -> {
                WWGLNGNode ngNode = WWGLNodeHelper.getPeer(this);
                ngNode.setFrameData(texture, lock);
                ngNode.markDirty();
                NodeHelper.markDirty(this, DirtyBits.NODE_FORCE_SYNC);
                displayedFrames = ngNode.getFrameNumber();
            });
    }

    private void verifyAccess(String methodName, boolean fxApplicationThread) {
        if (fxApplicationThread) {
            if (!Platform.isFxApplicationThread()) {
                throw new RuntimeException(
                    getClass().getName() + "." + methodName + "() can only be called on the JavaFX application thread.");
            }
        } else if (Thread.currentThread() != redrawThread) {
            throw new RuntimeException(
                getClass().getName() + "." + methodName + "() can only be called on the rendering thread.");
        }
    }

    @Override
    public void accept(Runnable runnable, boolean preemptable) {
        if (Thread.currentThread() == redrawThread) {
            runnable.run();
        } else {
            synchronized (sinkQueue) {
                sinkQueue.add(new WorkItem(runnable, preemptable));
            }

            markDirty(false);
        }
    }

    @Override
    public void acceptAndWait(Runnable runnable) {
        if (Thread.currentThread() == redrawThread) {
            runnable.run();
        } else {
            var awaitableRunnable = new AwaitableRunnable(runnable);

            synchronized (sinkQueue) {
                sinkQueue.add(new WorkItem(awaitableRunnable, false));
            }

            markDirty(false);
            awaitableRunnable.await();
        }
    }

    @Override
    public void defer(Runnable runnable, boolean preemptable) {
        synchronized (sinkQueue) {
            sinkQueue.addFirst(new WorkItem(runnable, preemptable));
        }

        deferFlag = true;
        markDirty(true);
    }

    @Override
    public void setModel(Model model) {
        cachedModel = model;
        accept(() -> mainDrawable.setModel(model), false);
    }

    @Override
    public Model getModel() {
        return cachedModel;
    }

    @Override
    public synchronized void setView(View view) {
        cachedView = view;
        accept(() -> mainDrawable.setView(view), false);
    }

    @Override
    public View getView() {
        return cachedView;
    }

    @Override
    public synchronized void setModelAndView(Model model, View view) {
        cachedModel = model;
        cachedView = view;
        accept(
            () -> {
                mainDrawable.setModel(model);
                mainDrawable.setView(view);
            }, false);
    }

    @Override
    public SceneController getSceneController() {
        return sceneController;
    }

    @Override
    public void setSceneController(SceneController sceneController) {
        this.sceneController = sceneController;
        accept(() -> mainDrawable.setSceneController(sceneController), false);
    }

    @Override
    public InputHandler getInputHandler() {
        return inputHandler;
    }

    @Override
    public void setInputHandler(InputHandler inputHandler) {
        if (this.inputHandler != null) {
            if (this.inputHandler instanceof JfxInputHandler) {
                ((JfxInputHandler)this.inputHandler).setEventSink(null);
            }

            this.inputHandler.setEventSource(null);
        }

        if (inputHandler != null) {
            if (inputHandler instanceof JfxInputHandler) {
                ((JfxInputHandler)inputHandler).setEventSink(this);
            }

            this.inputHandler = inputHandler;
            inputHandler.setEventSource(this);
        } else {
            this.inputHandler = new NoOpInputHandler();
        }
    }

    @Override
    public void addRenderingListener(RenderingListener listener) {
        mainDrawable.addRenderingListener(listener);
    }

    @Override
    public void removeRenderingListener(RenderingListener listener) {
        mainDrawable.removeRenderingListener(listener);
    }

    @Override
    public void addSelectListener(SelectListener listener) {
        inputHandler.addSelectListener(listener);
        mainDrawable.addSelectListener(listener);
    }

    @Override
    public void removeSelectListener(SelectListener listener) {
        inputHandler.removeSelectListener(listener);
        mainDrawable.removeSelectListener(listener);
    }

    @Override
    public void addPositionListener(PositionListener listener) {
        mainDrawable.addPositionListener(listener);
    }

    @Override
    public void removePositionListener(PositionListener listener) {
        mainDrawable.removePositionListener(listener);
    }

    @Override
    public void redraw() {
        markDirty(true);
    }

    @Override
    public void redrawNow() {
        markDirty(true);
    }

    @Override
    public Position getCurrentPosition() {
        verifyAccess("getCurrentPosition", false);
        return mainDrawable.getCurrentPosition();
    }

    @Override
    public PickedObjectList getObjectsAtCurrentPosition() {
        verifyAccess("getObjectsAtCurrentPosition", false);
        SceneController sceneController = mainDrawable.getSceneController();
        return sceneController != null ? sceneController.getPickedObjectList() : null;
    }

    @Override
    public PickedObjectList getObjectsInSelectionBox() {
        verifyAccess("getObjectsInSelectionBox", false);
        SceneController sceneController = mainDrawable.getSceneController();
        return sceneController != null ? sceneController.getObjectsInPickRectangle() : null;
    }

    @Override
    public GpuResourceCache getGpuResourceCache() {
        verifyAccess("getGpuResourceCache", false);
        return mainDrawable.getGpuResourceCache();
    }

    @Override
    public void setPerFrameStatisticsKeys(Set<String> keys) {
        verifyAccess("setPerFrameStatisticsKeys", false);
        mainDrawable.setPerFrameStatisticsKeys(keys);
    }

    @Override
    public Collection<PerformanceStatistic> getPerFrameStatistics() {
        verifyAccess("getPerFrameStatistics", false);
        return mainDrawable.getPerFrameStatistics();
    }

    @Override
    public void shutdown() {
        WorldWind.getDataFileStore().removePropertyChangeListener(mainDrawable);
        mainDrawable.shutdown();

        try {
            exitThreadRequested = true;
            markDirty(true);
            redrawThread.join();

            if (inputHandler != null) {
                inputHandler.dispose();
            }
        } catch (InterruptedException ignored) {
        }

        renderingEnabled = false;
    }

    @Override
    public void addRenderingExceptionListener(RenderingExceptionListener listener) {
        verifyAccess("addRenderingExceptionListener", false);
        mainDrawable.addRenderingExceptionListener(listener);
    }

    @Override
    public void removeRenderingExceptionListener(RenderingExceptionListener listener) {
        verifyAccess("removeRenderingExceptionListener", false);
        mainDrawable.removeRenderingExceptionListener(listener);
    }

    @Override
    public GLContext getContext() {
        verifyAccess("getContext", false);
        return mainDrawable.getContext();
    }

    @Override
    public boolean isEnableGpuCacheReinitialization() {
        verifyAccess("isEnableGpuCacheReinitialization", false);
        return mainDrawable.isEnableGpuCacheReinitialization();
    }

    @Override
    public void setEnableGpuCacheReinitialization(boolean enableGpuCacheReinitialization) {
        verifyAccess("setEnableGpuCacheReinitialization", false);
        mainDrawable.setEnableGpuCacheReinitialization(enableGpuCacheReinitialization);
    }

    @Override
    public Object setValue(String key, Object value) {
        return mainDrawable.setValue(key, value);
    }

    @Override
    public AVList setValues(AVList avList) {
        return mainDrawable.setValues(avList);
    }

    @Override
    public Object getValue(String key) {
        return mainDrawable.getValue(key);
    }

    @Override
    public Collection<Object> getValues() {
        return mainDrawable.getValues();
    }

    @Override
    public String getStringValue(String key) {
        return mainDrawable.getStringValue(key);
    }

    @Override
    public Set<Map.Entry<String, Object>> getEntries() {
        return mainDrawable.getEntries();
    }

    @Override
    public boolean hasKey(String key) {
        return mainDrawable.hasKey(key);
    }

    @Override
    public Object removeKey(String key) {
        return mainDrawable.removeKey(key);
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        mainDrawable.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        mainDrawable.removePropertyChangeListener(propertyName, listener);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        mainDrawable.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        mainDrawable.removePropertyChangeListener(listener);
    }

    @Override
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        mainDrawable.firePropertyChange(propertyName, oldValue, newValue);
    }

    @Override
    public void firePropertyChange(PropertyChangeEvent propertyChangeEvent) {
        mainDrawable.firePropertyChange(propertyChangeEvent);
    }

    @Override
    public AVList copy() {
        return mainDrawable.copy();
    }

    @Override
    public AVList clearList() {
        return mainDrawable.clearList();
    }

    private void processSinkQueue() {
        List<WorkItem> list;

        synchronized (sinkQueue) {
            list = new ArrayList<>(sinkQueue);
            sinkQueue.clear();
        }

        deferFlag = false;

        for (int i = 0; i < list.size(); ++i) {
            if (deferFlag) {
                synchronized (sinkQueue) {
                    for (int j = i; j < list.size(); ++j) {
                        WorkItem item = list.get(j);
                        if (item.preemptable) {
                            sinkQueue.add(list.get(j));
                        }
                    }
                }

                for (int j = i; j < list.size(); ++j) {
                    WorkItem item = list.get(j);
                    if (!item.preemptable) {
                        item.runnable.run();
                    }
                }

                return;
            } else {
                list.get(i).runnable.run();
            }
        }
    }

    private void markDirty(boolean redraw) {
        if (redraw) {
            redrawRequested.set(redraw);
        }

        if (Thread.currentThread() != redrawThread) {
            sceneDirtyEvent.set();
        }
    }

    private NGNode doCreatePeer() {
        WWGLNGNode ngNode = new WWGLNGNode();
        ngNode.setContextChangedListener(new WWGLNGNode.ContextChangedListener() {
            @Override
            public void changed(GLSharedContext context) {
                mainDrawable.setSharedContext(context);
                imageDrawable.setSharedContext(context);
            }
        });

        return ngNode;
    }

    private BaseBounds doComputeGeomBounds(BaseTransform baseTransform) {
        BaseBounds baseBounds = new RectBounds(0, 0, (float) width.get(), (float) height.get());
        baseBounds = baseTransform.transform(baseBounds, baseBounds);
        return baseBounds;
    }

    private boolean doComputeContains(double x, double y) {
        double w = width.get();
        double h = height.get();
        return w > 0 && h > 0 && x >= 0 && y >= 0 && x < w && y < h;
    }

    private class WorkItem {
        final Runnable runnable;
        final boolean preemptable;

        WorkItem(Runnable runnable, boolean preemptable) {
            this.runnable = runnable;
            this.preemptable = preemptable;
        }
    }

}
