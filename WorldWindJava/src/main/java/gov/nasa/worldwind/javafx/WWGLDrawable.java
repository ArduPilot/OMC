/*
 * Copyright (C) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.javafx;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.texture.TextureIO;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.event.InputHandler;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.DebugGL;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.PerformanceStatistic;
import gov.nasa.worldwind.util.ScheduledTaskService;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import javax.swing.event.EventListenerList;

class WWGLDrawable extends WWObjectImpl implements WorldWindow {

    private static final int SWAP_CHAIN_SIZE = 3;

    interface FrameRenderedHandler {
        void frameRendered(int textureHandle, Lock lock);
    }

    private static class Framebuffer {
        int handle;
        int colorTexture;
        int depthRenderbuffer;
        final ReentrantLock lock = new ReentrantLock();
    }

    // Default time in milliseconds that the view must remain unchanged before
    // the {@link View#VIEW_STOPPED} message is sent.
    private static final long VIEW_STOP_TIME = 1000;

    private final ScheduledTaskService scheduledTaskService;
    private final EventListenerList eventListeners = new EventListenerList();
    private final Framebuffer[] framebuffers = new Framebuffer[SWAP_CHAIN_SIZE];

    private final PropertyChangeListener sceneControllerChanged =
        event -> {
            if (AVKey.MODEL.equals(event.getPropertyName())) {
                Model oldModel = (Model)event.getOldValue();
                Model newModel = (Model)event.getNewValue();

                if (oldModel != null && oldModel != newModel) {
                    WWContext.getInstance().release(oldModel);
                }

                if (newModel != null && oldModel != newModel) {
                    WWContext.getInstance().addRef(newModel);
                }
            }

            callRedrawListeners(0);
        };

    private SceneController sceneController;
    private GpuResourceCache gpuResourceCache;
    private boolean shuttingDown = false;
    private long lastViewID;
    private ScheduledFuture viewRefreshTask;
    private GLSharedContext sharedContext;
    private GLHelper glHelper;
    private FrameRenderedHandler frameRenderedHandler;
    private boolean enableGpuCacheReinitialization = true;
    private boolean initialized;
    private int requestedWidth;
    private int requestedHeight;
    private int currentWidth;
    private int currentHeight;
    private int currentFramebufferIndex;
    private int frameNumber;

    WWGLDrawable(ScheduledTaskService scheduledTaskService, int width, int height) {
        this.scheduledTaskService = scheduledTaskService;
        this.requestedWidth = width;
        this.requestedHeight = height;

        for (int i = 0; i < framebuffers.length; ++i) {
            framebuffers[i] = new Framebuffer();
        }
    }

    synchronized void setSharedContext(GLSharedContext sharedContext) {
        this.sharedContext = sharedContext;
    }

    public void setSize(int width, int height) {
        this.requestedWidth = width;
        this.requestedHeight = height;
    }

    void setFrameRenderedHandler(FrameRenderedHandler handler) {
        this.frameRenderedHandler = handler;
    }

    public GpuResourceCache getGpuResourceCache() {
        return this.gpuResourceCache;
    }

    public void setGpuResourceCache(GpuResourceCache gpuResourceCache) {
        this.gpuResourceCache = gpuResourceCache;

        if (this.sceneController != null) {
            this.sceneController.setGpuResourceCache(this.gpuResourceCache);
        }
    }

    void initGpuResourceCache(GpuResourceCache cache) {
        if (cache == null) {
            String msg = Logging.getMessage("nullValue.GpuResourceCacheIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.setGpuResourceCache(cache);
    }

    @Override
    public synchronized GLContext getContext() {
        return sharedContext != null ? sharedContext.getContext() : null;
    }

    @Override
    public void setModel(Model model) {
        // model can be null, that's ok - it indicates no model.
        if (this.sceneController != null) {
            this.sceneController.setModel(model);
        }
    }

    @Override
    public Model getModel() {
        return this.sceneController != null ? this.sceneController.getModel() : null;
    }

    @Override
    public void setView(View view) {
        View oldView = getView();
        if (oldView != null) {
            oldView.getViewInputHandler().setWorldWindow(null);
        }

        if (view != null) {
            view.getViewInputHandler().setWorldWindow(this);
        }

        // view can be null, that's ok - it indicates no view.
        if (this.sceneController != null) {
            this.sceneController.setView(view);
        }
    }

    @Override
    public View getView() {
        return this.sceneController != null ? this.sceneController.getView() : null;
    }

    @Override
    public void setModelAndView(Model model, View view) {
        if (this.sceneController != null) {
            this.sceneController.setModel(model);
            this.sceneController.setView(view);
        }
    }

    @Override
    public SceneController getSceneController() {
        return this.sceneController;
    }

    @Override
    public void setSceneController(SceneController sc) {
        if (this.sceneController != null) {
            this.sceneController.setGpuResourceCache(null);
            this.sceneController.removePropertyChangeListener(sceneControllerChanged);
        }

        if (sc != null) {
            sc.setGpuResourceCache(gpuResourceCache);
            sc.addPropertyChangeListener(sceneControllerChanged);
        }

        this.sceneController = sc;
    }

    @Override
    public InputHandler getInputHandler() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInputHandler(InputHandler inputHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEnableGpuCacheReinitialization() {
        return enableGpuCacheReinitialization;
    }

    @Override
    public void setEnableGpuCacheReinitialization(boolean enableGpuCacheReinitialization) {
        this.enableGpuCacheReinitialization = enableGpuCacheReinitialization;
    }

    @Override
    public void setPerFrameStatisticsKeys(Set<String> keys) {
        if (this.sceneController != null) {
            this.sceneController.setPerFrameStatisticsKeys(keys);
        }
    }

    @Override
    public Collection<PerformanceStatistic> getPerFrameStatistics() {
        if (this.sceneController == null || this.sceneController.getPerFrameStatistics() == null) {
            return new ArrayList<>(0);
        }

        return this.sceneController.getPerFrameStatistics();
    }

    @Override
    public Position getCurrentPosition() {
        if (this.sceneController == null) {
            return null;
        }

        PickedObjectList pol = this.getSceneController().getPickedObjectList();
        if (pol == null || pol.size() < 1) {
            return null;
        }

        Position p = null;
        PickedObject top = pol.getTopPickedObject();
        if (top != null && top.hasPosition()) {
            p = top.getPosition();
        } else if (pol.getTerrainObject() != null) {
            p = pol.getTerrainObject().getPosition();
        }

        return p;
    }

    @Override
    public PickedObjectList getObjectsInSelectionBox() {
        return new PickedObjectList();
    }

    @Override
    public PickedObjectList getObjectsAtCurrentPosition() {
        return new PickedObjectList();
    }

    public void addRedrawListener(RedrawListener listener) {
        synchronized (eventListeners) {
            this.eventListeners.add(RedrawListener.class, listener);
        }
    }

    @Override
    public void addRenderingListener(RenderingListener listener) {
        synchronized (eventListeners) {
            this.eventListeners.add(RenderingListener.class, listener);
        }
    }

    @Override
    public void removeRenderingListener(RenderingListener listener) {
        synchronized (eventListeners) {
            this.eventListeners.remove(RenderingListener.class, listener);
        }
    }

    @Override
    public void addPositionListener(PositionListener listener) {
        synchronized (eventListeners) {
            this.eventListeners.add(PositionListener.class, listener);
        }
    }

    @Override
    public void removePositionListener(PositionListener listener) {
        synchronized (eventListeners) {
            this.eventListeners.remove(PositionListener.class, listener);
        }
    }

    @Override
    public void addSelectListener(SelectListener listener) {
        synchronized (eventListeners) {
            this.eventListeners.add(SelectListener.class, listener);
        }
    }

    @Override
    public void removeSelectListener(SelectListener listener) {
        synchronized (eventListeners) {
            this.eventListeners.remove(SelectListener.class, listener);
        }
    }

    @Override
    public void addRenderingExceptionListener(RenderingExceptionListener listener) {
        synchronized (eventListeners) {
            this.eventListeners.add(RenderingExceptionListener.class, listener);
        }
    }

    @Override
    public void removeRenderingExceptionListener(RenderingExceptionListener listener) {
        synchronized (eventListeners) {
            this.eventListeners.remove(RenderingExceptionListener.class, listener);
        }
    }

    @Override
    public synchronized void redraw() {
        if (getModel() != null && getView() != null) {
            if (sharedContext != null) {
                try {
                    renderFrame(sharedContext);
                } catch (GLException e) {
                    if (!shuttingDown) {
                        e.printStackTrace();
                        //reconfigureAutoDrawable(jfxContext);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void redrawNow() {
        if (getModel() != null && getView() != null) {
            if (sharedContext != null) {
                try {
                    renderFrame(sharedContext);
                } catch (GLException e) {
                    e.printStackTrace();
                    //reconfigureAutoDrawable(jfxContext);
                }
            }
        }
    }

    @Override
    public void shutdown() {
        shuttingDown = true;
    }

    private void doShutdown(GLSharedContext context) {
        context.makeCurrent();

        GL2 gl = context.getGL().getGL2();
        for (Framebuffer framebuffer : framebuffers) {
            gl.glDeleteTextures(1, new int[] {framebuffer.colorTexture}, 0);
            gl.glDeleteRenderbuffers(1, new int[] {framebuffer.depthRenderbuffer}, 0);
            gl.glDeleteFramebuffers(1, new int[] {framebuffer.handle}, 0);
        }


        Model model = getModel();
        if (model != null) {
            WWContext.getInstance().release(model);
        }

        // Clear the texture cache
        if (this.getGpuResourceCache() != null) {
            this.getGpuResourceCache().clear();
        }

        SceneController sc = this.getSceneController();
        if (sc != null) {
            try {
                sc.dispose();
            } catch (GLException ignored) {
            }
        }
    }

    private void renderFrame(GLSharedContext context) {
        if (requestedWidth == 0 || requestedHeight == 0) {
            return;
        }

        TextureIO.setTexRectEnabled(false);

        try {
            callRenderingListeners(new RenderingEvent(this, RenderingEvent.BEFORE_RENDERING));
        } catch (Exception e) {
            Logging.logger()
                .log(
                    Level.SEVERE,
                    Logging.getMessage("WorldWindowGLAutoDrawable.ExceptionDuringGLEventListenerDisplay"),
                    e);
        }

        SceneController sc = getSceneController();
        if (sc == null) {
            String msg = Logging.getMessage("WorldWindowGLCanvas.ScnCntrllerNullOnRepaint");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        Position positionAtStart = null;
        PickedObject selectionAtStart = null;
        PickedObjectList boxSelectionAtStart = null;

        final Framebuffer framebuffer = framebuffers[currentFramebufferIndex];
        framebuffer.lock.lock();

        try {
            context.makeCurrent();

            // Performing shutdown here in order to do so with a current GL context for GL resource disposal.
            if (shuttingDown) {
                try {
                    doShutdown(context);
                } catch (Exception e) {
                    Logging.logger()
                        .log(
                            Level.SEVERE,
                            Logging.getMessage("WorldWindowGLCanvas.ExceptionWhileShuttingDownWorldWindow"),
                            e);
                }

                return;
            }

            boolean recreateFramebuffer = currentWidth != requestedWidth || currentHeight != requestedHeight;

            DebugGL gl = (DebugGL) sc.getDrawContext().getGL();
            if (!gl.isInitialized()) {
                recreateFramebuffer = true;
                gl.setGL2(context.getGL().getGL2());

                // Setting this property will cause all invocations of OpenGL functions to be verified to occur
                // on the specified thread.
                //
                // gl.setVerifyThread(Thread.currentThread());
            }

            if (glHelper == null) {
                glHelper = new GLHelper(this, gl.getGL3());
            }

            if (recreateFramebuffer) {
                currentWidth = requestedWidth;
                currentHeight = requestedHeight;

                // Since we want to render to an offscreen surface, we need to set up a frame buffer with two
                // attachments:
                //
                // 1) a texture to receive our color data
                // 2) a combined depth+stencil render buffer
                //
                // We are using a texture instead of a render buffer to contain our pixels, since we need to make
                // this texture accessible to the JavaFX OpenGL context later.
                //
                if (!initialized) {
                    int[] params = new int[SWAP_CHAIN_SIZE];

                    gl.glGenFramebuffers(SWAP_CHAIN_SIZE, params, 0);
                    glHelper.checkError("glGenFramebuffers");
                    for (int i = 0; i < SWAP_CHAIN_SIZE; ++i) {
                        framebuffers[i].handle = params[i];
                    }

                    gl.glGenRenderbuffers(SWAP_CHAIN_SIZE, params, 0);
                    glHelper.checkError("glGenRenderbuffers");
                    for (int i = 0; i < SWAP_CHAIN_SIZE; ++i) {
                        framebuffers[i].depthRenderbuffer = params[i];
                    }

                    gl.glGenTextures(SWAP_CHAIN_SIZE, params, 0);
                    glHelper.checkError("glGenTextures");
                    for (int i = 0; i < SWAP_CHAIN_SIZE; ++i) {
                        framebuffers[i].colorTexture = params[i];
                    }

                    initialized = true;
                }

                for (Framebuffer fb : framebuffers) {
                    // Initialize the texture that will contain our color pixels with the current width and height.
                    //
                    gl.glBindTexture(GL.GL_TEXTURE_2D, fb.colorTexture);
                    gl.glTexImage2D(
                        GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, currentWidth, currentHeight, 0, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, null);
                    glHelper.checkError(
                        "glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, "
                            + currentWidth
                            + ", "
                            + currentHeight
                            + ", 0, GL_BGRA, GL_UNSIGNED_BYTE, null)");
                    gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

                    // Initialize the render buffer that will contain depth/stencil pixels with the current
                    // width and height.
                    //
                    gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, fb.depthRenderbuffer);
                    gl.glRenderbufferStorage(GL2.GL_RENDERBUFFER, GL2.GL_DEPTH24_STENCIL8, currentWidth, currentHeight);
                    glHelper.checkError(
                        "glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, "
                            + currentWidth
                            + ", "
                            + currentHeight
                            + ")");
                    gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, 0);

                    // Bind our framebuffer and attach the previously created texture and render buffer.
                    //
                    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, fb.handle);
                    gl.glFramebufferTexture2D(
                        GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D, fb.colorTexture, 0);
                    glHelper.checkError(
                        "glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, "
                            + fb.colorTexture
                            + ", 0)");
                    gl.glFramebufferRenderbuffer(
                        GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_STENCIL_ATTACHMENT, GL2.GL_RENDERBUFFER, fb.depthRenderbuffer);
                    glHelper.checkError(
                        "glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, "
                            + fb.depthRenderbuffer
                            + ")");

                    // By now, we should have a valid framebuffer.
                    glHelper.checkFramebuffer(GL.GL_FRAMEBUFFER);
                }
            }

            gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, framebuffer.handle);
            gl.glViewport(0, 0, currentWidth, currentHeight);

            // Determine if the view has changed since the last frame.
            checkForViewChange();

            positionAtStart = getCurrentPosition();
            selectionAtStart = getCurrentSelection();
            boxSelectionAtStart = getCurrentBoxSelection();

            gl.resetCounters();
            final int redrawDelay = sc.repaint();
            if (redrawDelay > 0) {
                callRedrawListeners(redrawDelay);
            }

            try {
                callRenderingListeners(new RenderingEvent(this, RenderingEvent.BEFORE_BUFFER_SWAP));
            } catch (Exception e) {
                Logging.logger()
                    .log(
                        Level.SEVERE,
                        Logging.getMessage("WorldWindowGLAutoDrawable.ExceptionDuringGLEventListenerDisplay"),
                        e);
            }

            int readPixelsCount = gl.getCounter(DebugGL.GLFunc.glReadPixels);
            int drawCalls =
                gl.getCounter(DebugGL.GLFunc.glDrawArrays)
                    + gl.getCounter(DebugGL.GLFunc.glDrawArraysInstanced)
                    + gl.getCounter(DebugGL.GLFunc.glDrawArraysInstancedBaseInstance)
                    + gl.getCounter(DebugGL.GLFunc.glDrawBuffer)
                    + gl.getCounter(DebugGL.GLFunc.glDrawBuffers)
                    + gl.getCounter(DebugGL.GLFunc.glDrawElements)
                    + gl.getCounter(DebugGL.GLFunc.glDrawElementsInstanced)
                    + gl.getCounter(DebugGL.GLFunc.glDrawElementsInstancedBaseInstance)
                    + gl.getCounter(DebugGL.GLFunc.glDrawElementsInstancedBaseVertexBaseInstance)
                    + gl.getCounter(DebugGL.GLFunc.glDrawRangeElements)
                    + gl.getCounter(DebugGL.GLFunc.glDrawTransformFeedback)
                    + gl.getCounter(DebugGL.GLFunc.glDrawTransformFeedbackStream)
                    + gl.getCounter(DebugGL.GLFunc.glMultiDrawArrays)
                    + gl.getCounter(DebugGL.GLFunc.glMultiDrawElements);

            sc.getDrawContext().setPerFrameStatistic(
                PerformanceStatistic.FRAME_TIME, PerformanceStatistic.FRAME_TIME, sc.getFrameTime());
            sc.getDrawContext().setPerFrameStatistic(
                PerformanceStatistic.FRAME_RATE, PerformanceStatistic.FRAME_RATE, sc.getFramesPerSecond());
            sc.getDrawContext().setPerFrameStatistic(
                PerformanceStatistic.READ_PIXELS_COUNT, PerformanceStatistic.READ_PIXELS_COUNT, readPixelsCount);
            sc.getDrawContext().setPerFrameStatistic(
                PerformanceStatistic.DRAW_CALLS, PerformanceStatistic.DRAW_CALLS, drawCalls);
            sc.getDrawContext().setPerFrameStatistic(
                PerformanceStatistic.FRAMES_RENDERED, PerformanceStatistic.FRAMES_RENDERED, ++frameNumber);

            FrameRenderedHandler handler = frameRenderedHandler;
            if (handler != null) {
                handler.frameRendered(framebuffer.colorTexture, framebuffer.lock);
            }

            gl.glFinish();

            currentFramebufferIndex++;
            if (currentFramebufferIndex == SWAP_CHAIN_SIZE) {
                currentFramebufferIndex = 0;
            }
        } catch (Exception e) {
            Logging.logger()
                .log(Level.SEVERE, Logging.getMessage("WorldWindowGLCanvas.ExceptionAttemptingRepaintWorldWindow"), e);
        } finally {
            context.release();
            framebuffer.lock.unlock();
        }

        // Dispatch the rendering exceptions accumulated by the SceneController during this frame to our
        // RenderingExceptionListeners.
        Iterable<Throwable> renderingExceptions = sc.getRenderingExceptions();
        if (renderingExceptions != null) {
            for (Throwable t : renderingExceptions) {
                if (t != null) {
                    callRenderingExceptionListeners(t);
                }
            }
        }

        // Position and selection notification occurs only on triggering conditions, not same-state conditions:
        // start == null, end == null: nothing selected -- don't notify
        // start == null, end != null: something now selected -- notify
        // start != null, end == null: something was selected but no longer is -- notify
        // start != null, end != null, start != end: something new was selected -- notify
        // start != null, end != null, start == end: same thing is selected -- don't notify

        Position positionAtEnd = getCurrentPosition();
        if (positionAtStart != null || positionAtEnd != null) {
            // call the listener if both are not null or positions are the same
            if (positionAtStart != null && positionAtEnd != null) {
                if (!positionAtStart.equals(positionAtEnd)) {
                    callPositionListeners(
                        new PositionEvent(this, sc.getPickPoint(), positionAtStart, positionAtEnd));
                }
            } else {
                callPositionListeners(
                    new PositionEvent(this, sc.getPickPoint(), positionAtStart, positionAtEnd));
            }
        }

        PickedObject selectionAtEnd = getCurrentSelection();
        if (selectionAtStart != null || selectionAtEnd != null) {
            callSelectListeners(
                new SelectEvent(this, SelectEvent.ROLLOVER, sc.getPickPoint(), sc.getPickedObjectList()));
        }

        PickedObjectList boxSelectionAtEnd = getCurrentBoxSelection();
        if (boxSelectionAtStart != null || boxSelectionAtEnd != null) {
            callSelectListeners(
                new SelectEvent(
                    this, SelectEvent.BOX_ROLLOVER, sc.getPickRectangle(), sc.getObjectsInPickRectangle()));
        }

        try {
            callRenderingListeners(new RenderingEvent(this, RenderingEvent.AFTER_BUFFER_SWAP));
        } catch (Exception e) {
            Logging.logger()
                .log(
                    Level.SEVERE,
                    Logging.getMessage("WorldWindowGLAutoDrawable.ExceptionDuringGLEventListenerDisplay"),
                    e);
        }
    }

    private void callRedrawListeners(int millis) {
        synchronized (eventListeners) {
            for (RedrawListener listener : this.eventListeners.getListeners(RedrawListener.class)) {
                listener.redrawRequested(millis);
            }
        }
    }

    private void callRenderingListeners(RenderingEvent event) {
        synchronized (eventListeners) {
            for (RenderingListener listener : this.eventListeners.getListeners(RenderingListener.class)) {
                listener.stageChanged(event);
            }
        }
    }

    private void callPositionListeners(final PositionEvent event) {
        synchronized (eventListeners) {
            for (PositionListener listener : eventListeners.getListeners(PositionListener.class)) {
                listener.moved(event);
            }
        }
    }

    private void callSelectListeners(final SelectEvent event) {
        synchronized (eventListeners) {
            for (SelectListener listener : eventListeners.getListeners(SelectListener.class)) {
                listener.selected(event);
            }
        }
    }

    private void callRenderingExceptionListeners(final Throwable exception) {
        synchronized (eventListeners) {
            for (RenderingExceptionListener listener : eventListeners.getListeners(RenderingExceptionListener.class)) {
                listener.exceptionThrown(exception);
            }
        }
    }

    private PickedObject getCurrentSelection() {
        if (this.sceneController == null) {
            return null;
        }

        PickedObjectList pol = this.getSceneController().getPickedObjectList();
        if (pol == null || pol.size() < 1) {
            return null;
        }

        PickedObject top = pol.getTopPickedObject();
        return top.isTerrain() ? null : top;
    }

    private PickedObjectList getCurrentBoxSelection() {
        if (this.sceneController == null) {
            return null;
        }

        PickedObjectList pol = this.sceneController.getObjectsInPickRectangle();
        return pol != null && pol.size() > 0 ? pol : null;
    }

    /**
     * Determine if the view has changed since the previous frame. If the view has changed, schedule a task that will
     * send a {@link View#VIEW_STOPPED} to the Model if the view does not change for {@link #VIEW_STOP_TIME}
     * milliseconds.
     */
    private void checkForViewChange() {
        long viewId = this.getView().getViewStateID();

        // Determine if the view has changed since the previous frame.
        if (viewId != this.lastViewID) {
            // View has changed, capture the new viewStateID
            this.lastViewID = viewId;

            // Cancel the previous view stop task and schedule a new one because the view has changed.
            this.scheduleViewStopTask(VIEW_STOP_TIME);
        }
    }

    private void scheduleViewStopTask(long delay) {
        Runnable viewStoppedTask =
            new Runnable() {
                @Override
                public void run() {
                    onMessage(new Message(View.VIEW_STOPPED, WWGLDrawable.this));
                }
            };

        // Cancel the previous view stop task
        if (this.viewRefreshTask != null) {
            this.viewRefreshTask.cancel(false);
        }

        // Schedule the task for execution in delay milliseconds
        if (scheduledTaskService != null) {
            this.viewRefreshTask = scheduledTaskService.addScheduledTask(viewStoppedTask, delay, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onMessage(Message msg) {
        Model model = this.getModel();
        if (model != null) {
            model.onMessage(msg);
        }
    }

    @SuppressWarnings({"UnusedParameters"})
    private void reinitialize(GLAutoDrawable glAutoDrawable) {
        // Clear the gpu resource cache if the window is reinitializing, most likely with a new gl hardware context.
        if (this.getGpuResourceCache() != null) {
            this.getGpuResourceCache().clear();
        }

        this.getSceneController().reinitialize();
    }

}
