/*
 * Copyright (C) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.javafx;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.TextureIO;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.BasicGpuResourceCache;
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
import gov.nasa.worldwind.exception.WWAbsentRequirementException;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.DebugGL;
import gov.nasa.worldwind.util.BasicGLCapabilitiesChooser;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.PerformanceStatistic;
import gov.nasa.worldwind.util.ScheduledTaskService;
import java.beans.PropertyChangeListener;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.event.EventListenerList;

class WWOffscreenDrawable extends WWObjectImpl implements WorldWindow {
    interface FrameRenderedHandler {
        void frameRendered(ByteBuffer pixels, int width, int height);
    }

    // Default time in milliseconds that the view must remain unchanged before
    // the {@link View#VIEW_STOPPED} message is sent.
    private static final long VIEW_STOP_TIME = 1000;
    private static final long START_TIMESTAMP = System.nanoTime();
    private static final Map<Model, Integer> modelReferenceCount = new HashMap<>();
    private static WWOffscreenDrawable currentInstance;

    private final ScheduledTaskService scheduledTaskService;
    private final EventListenerList eventListeners = new EventListenerList();

    private final PropertyChangeListener sceneControllerChanged =
        event -> {
            if (AVKey.MODEL.equals(event.getPropertyName())) {
                Model oldModel = (Model)event.getOldValue();
                Model newModel = (Model)event.getNewValue();

                synchronized (autoDrawableMutex) {
                    if (oldModel != null && oldModel != newModel) {
                        Integer count = modelReferenceCount.get(oldModel);
                        modelReferenceCount.put(oldModel, count != null ? count - 1 : 0);
                    }

                    if (newModel != null && oldModel != newModel) {
                        Integer count = modelReferenceCount.get(newModel);
                        modelReferenceCount.put(newModel, count != null ? count + 1 : 1);
                    }
                }
            }

            callRedrawListeners(0);
        };

    private final GLEventListener glEventListener =
        new GLEventListener() {
            @Override
            public void init(GLAutoDrawable glAutoDrawable) {
                WWOffscreenDrawable.this.initFrame(glAutoDrawable);
            }

            @Override
            public void dispose(GLAutoDrawable glAutoDrawable) {}

            @Override
            public void display(GLAutoDrawable glAutoDrawable) {
                if (currentInstance == WWOffscreenDrawable.this) {
                    WWOffscreenDrawable.this.renderFrame(glAutoDrawable);
                }
            }

            @Override
            public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {}
        };

    private boolean firstInit = true;
    private SceneController sceneController;
    private GpuResourceCache gpuResourceCache;
    private boolean shuttingDown = false;
    private long lastViewID;
    private ScheduledFuture viewRefreshTask;
    private GLAutoDrawable lastDrawable;
    private FrameRenderedHandler frameRenderedHandler;
    private boolean enableGpuCacheReinitialization = true;
    private int requestedWidth;
    private int requestedHeight;
    private int currentWidth;
    private int currentHeight;
    private int framebuffer;
    private int colorRenderbuffer;
    private int depthRenderbuffer;
    private int pixelbuffer;

    private static void checkError(String methodName, GL2 gl) {
        int errno = gl.glGetError();
        String err = null;
        switch (errno) {
        case GL.GL_INVALID_ENUM:
            err = "GL_INVALID_ENUM";
            break;
        case GL.GL_INVALID_OPERATION:
            err = "GL_INVALID_OPERATION";
            break;
        case GL.GL_INVALID_VALUE:
            err = "GL_INVALID_VALUE";
            break;
        }

        if (errno != 0) {
            err = Integer.toHexString(errno);
        }

        if (err != null) {
            Logging.logger().severe("[" + WWOffscreenDrawable.class.getSimpleName() + "]." + methodName + " -> " + err);
        }
    }

    private static void checkFramebuffer(GL2 gl) {
        String err = null;
        switch (gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER)) {
        case GL2.GL_FRAMEBUFFER_UNSUPPORTED:
            err = "GL_FRAMEBUFFER_UNSUPPORTED";
            break;
        case GL2.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
            err = "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
            break;
        case GL2.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
            err = "GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS";
            break;
        case GL2.GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
            err = "GL_FRAMEBUFFER_INCOMPLETE_FORMATS";
            break;
        case GL2.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
            err = "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
            break;
        case GL2.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
            err = "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER";
            break;
        case GL2.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
            err = "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE";
            break;
        case GL2.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
            err = "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER";
            break;
        }

        if (err != null) {
            Logging.logger()
                .severe("[" + WWOffscreenDrawable.class.getSimpleName() + "].glCheckFramebufferStatus -> " + err);
        }
    }

    WWOffscreenDrawable(ScheduledTaskService scheduledTaskService, int width, int height) {
        this.scheduledTaskService = scheduledTaskService;
        this.requestedWidth = width;
        this.requestedHeight = height;
        createAutoDrawable(glEventListener);
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
    public GLContext getContext() {
        return autoDrawable.getContext();
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
    public void redraw() {
        if (getModel() != null && getView() != null) {
            synchronized (autoDrawableMutex) {
                currentInstance = this;

                try {
                    if (shuttingDown) {
                        doShutdown(autoDrawable);
                    } else {
                        autoDrawable.display();
                    }
                } catch (GLException e) {
                    if (!shuttingDown) {
                        reconfigureAutoDrawable();
                    }
                }
            }
        }
    }

    @Override
    public void redrawNow() {
        if (getModel() != null && getView() != null) {
            synchronized (autoDrawableMutex) {
                currentInstance = this;

                try {
                    autoDrawable.display();
                } catch (GLException e) {
                    reconfigureAutoDrawable();
                }
            }
        }
    }

    @Override
    public void shutdown() {
        shuttingDown = true;
    }

    public void shutdownNow() {
        shuttingDown = true;

        synchronized (autoDrawableMutex) {
            currentInstance = this;
            doShutdown(autoDrawable);
        }
    }

    private void doShutdown(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glDeleteBuffers(1, new int[] {pixelbuffer}, 0);
        gl.glDeleteRenderbuffers(2, new int[] {colorRenderbuffer, depthRenderbuffer}, 0);
        gl.glDeleteFramebuffers(1, new int[] {framebuffer}, 0);

        autoDrawable.removeGLEventListener(glEventListener);

        Model model = getModel();
        if (model != null) {
            Integer count = modelReferenceCount.get(getModel());
            modelReferenceCount.put(model, count != null ? count - 1 : 0);

            if (modelReferenceCount.get(model) == 0) {
                // Dispose all the layers //  TODO: Need per-window dispose for layers
                LayerList layers = model.getLayers();
                if (layers != null) {
                    for (Layer layer : layers) {
                        try {
                            layer.dispose();
                        } catch (Exception e) {
                            Logging.logger()
                                .log(
                                    java.util.logging.Level.SEVERE,
                                    Logging.getMessage("WorldWindowGLCanvas.ExceptionWhileShuttingDownWorldWindow"),
                                    e);
                        }
                    }
                }
            }
        }

        if (releaseAutoDrawable()) {
            // Clear the texture cache
            if (this.getGpuResourceCache() != null) {
                this.getGpuResourceCache().clear();
            }
        }

        SceneController sc = this.getSceneController();
        if (sc != null) {
            try {
                sc.dispose();
            } catch (GLException ignored) {
            }
        }
    }

    private void initFrame(GLAutoDrawable glAutoDrawable) {
        if (!isGLContextCompatible(glAutoDrawable.getContext())) {
            String msg =
                Logging.getMessage("WorldWindowGLAutoDrawable.IncompatibleGLContext", glAutoDrawable.getContext());
            callRenderingExceptionListeners(new WWAbsentRequirementException(msg));
        }

        for (String funcName : getRequiredOglFunctions()) {
            if (!glAutoDrawable.getGL().isFunctionAvailable(funcName)) {
                //noinspection ThrowableInstanceNeverThrown
                callRenderingExceptionListeners(new WWAbsentRequirementException(funcName + " not available"));
            }
        }

        for (String extName : getRequiredOglExtensions()) {
            if (!glAutoDrawable.getGL().isExtensionAvailable(extName)) {
                //noinspection ThrowableInstanceNeverThrown
                callRenderingExceptionListeners(new WWAbsentRequirementException(extName + " not available"));
            }
        }

        if (firstInit) {
            firstInit = false;
        } else if (enableGpuCacheReinitialization) {
            reinitialize(glAutoDrawable);
        }

        GL2 gl2 = autoDrawable.getGL().getGL2();
        int[] temp = new int[2];

        gl2.glGenBuffers(1, temp, 0);
        pixelbuffer = temp[0];

        gl2.glGenFramebuffers(1, temp, 0);
        framebuffer = temp[0];

        gl2.glGenRenderbuffers(2, temp, 0);
        colorRenderbuffer = temp[0];
        depthRenderbuffer = temp[1];

        // Disables use of the OpenGL extension GL_ARB_texture_rectangle by JOGL's Texture creation utility.
        //
        // Between version 1.1.1 and version 2.x, JOGL modified its texture creation utility to favor
        // GL_ARB_texture_rectangle over GL_ARB_texture_non_power_of_two on Mac OS X machines with ATI
        // graphics cards. See
        // the following URL for details on the texture rectangle extension:
        // http://www.opengl.org/registry/specs/ARB/texture_rectangle.txt
        //
        // There are two problems with favoring texture rectangle for non power of two textures:
        // 1) As of November 2012, we cannot find any evidence that the GL_ARB_texture_non_power_of_two
        // extension is
        //    problematic on Mac OS X machines with ATI graphics cards. The texture rectangle extension is
        // more limiting
        //    than the NPOT extension, and therefore not preferred.
        // 2) World Wind assumes that a texture's target is always GL_TEXTURE_2D, and therefore incorrectly
        // displays
        //    textures with the target GL_TEXTURE_RECTANGLE.
        TextureIO.setTexRectEnabled(false);
    }

    private void renderFrame(GLAutoDrawable glAutoDrawable) {
        // Performing shutdown here in order to do so with a current GL context for GL resource disposal.
        if (shuttingDown) {
            try {
                doShutdown(glAutoDrawable);
            } catch (Exception e) {
                Logging.logger()
                    .log(
                        Level.SEVERE,
                        Logging.getMessage("WorldWindowGLCanvas.ExceptionWhileShuttingDownWorldWindow"),
                        e);
            }

            return;
        }

        if (requestedWidth == 0 || requestedHeight == 0) {
            return;
        }

        try {
            SceneController sc = getSceneController();
            if (sc == null) {
                String msg = Logging.getMessage("WorldWindowGLCanvas.ScnCntrllerNullOnRepaint");
                Logging.logger().severe(msg);
                throw new IllegalStateException(msg);
            }

            boolean recreateFramebuffer = currentWidth != requestedWidth || currentHeight != requestedHeight;

            DebugGL gl = (DebugGL)sc.getDrawContext().getGL();
            if (!gl.isInitialized() || lastDrawable != autoDrawable) {
                lastDrawable = autoDrawable;
                recreateFramebuffer = true;
                gl.setGL2(autoDrawable.getGL().getGL2());

                // Setting this property will cause all invocations of OpenGL functions to be verified to occur
                // on the specified thread.
                //
                // gl.setVerifyThread(Thread.currentThread());
            }

            if (recreateFramebuffer) {
                currentWidth = requestedWidth;
                currentHeight = requestedHeight;

                // Since we want to render to an offscreen surface, we need to set up some things:
                //
                // 1. A frame buffer, as well as two attached render buffers:
                //   - a color render buffer
                //   - a combined depth+stencil render buffer
                //
                // 2. A pixel buffer to contain our rendered image, which we can then use to download our pixels.

                gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, pixelbuffer);
                gl.glBufferData(GL2.GL_PIXEL_PACK_BUFFER, currentWidth * currentHeight * 4, null, GL2.GL_DYNAMIC_READ);
                checkError(
                    "glBufferData(GL_PIXEL_PACK_BUFFER, "
                        + (currentWidth * currentHeight * 4)
                        + ", null, GL_DYNAMIC_READ)",
                    gl);

                gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, 0);

                gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, colorRenderbuffer);
                gl.glRenderbufferStorage(GL2.GL_RENDERBUFFER, GL.GL_RGBA, currentWidth, currentHeight);
                checkError(
                    "glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA, " + currentWidth + ", " + currentHeight + ")", gl);

                gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, depthRenderbuffer);
                gl.glRenderbufferStorage(GL2.GL_RENDERBUFFER, GL2.GL_DEPTH24_STENCIL8, currentWidth, currentHeight);
                checkError(
                    "glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, "
                        + currentWidth
                        + ", "
                        + currentHeight
                        + ")",
                    gl);

                gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, 0);

                gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, framebuffer);
                gl.glFramebufferRenderbuffer(
                    GL2.GL_FRAMEBUFFER, GL2.GL_COLOR_ATTACHMENT0, GL2.GL_RENDERBUFFER, colorRenderbuffer);
                checkError(
                    "glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, "
                        + colorRenderbuffer
                        + ")",
                    gl);

                gl.glFramebufferRenderbuffer(
                    GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_STENCIL_ATTACHMENT, GL2.GL_RENDERBUFFER, depthRenderbuffer);
                checkError(
                    "glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, "
                        + depthRenderbuffer
                        + ")",
                    gl);

                checkFramebuffer(gl);
            }

            gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, framebuffer);
            gl.glViewport(0, 0, currentWidth, currentHeight);

            // Determine if the view has changed since the last frame.
            checkForViewChange();

            Position positionAtStart = getCurrentPosition();
            PickedObject selectionAtStart = getCurrentSelection();
            PickedObjectList boxSelectionAtStart = getCurrentBoxSelection();

            try {
                callRenderingListeners(new RenderingEvent(autoDrawable, RenderingEvent.BEFORE_RENDERING));
            } catch (Exception e) {
                Logging.logger()
                    .log(
                        Level.SEVERE,
                        Logging.getMessage("WorldWindowGLAutoDrawable.ExceptionDuringGLEventListenerDisplay"),
                        e);
            }

            gl.resetCounters();
            final int redrawDelay = sc.repaint();
            if (redrawDelay > 0) {
                callRedrawListeners(redrawDelay);
            }

            // If a pixel buffer is bound, glReadPixels will return immediately while the pixels are transferred
            // asynchronously into the pixel buffer. We can do other stuff in the meantime, until we're ready to
            // map the pixel buffer later.
            gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, pixelbuffer);
            checkError("glBindBuffer", gl);

            gl.glReadPixels(0, 0, currentWidth, currentHeight, GL2.GL_BGRA, GL.GL_UNSIGNED_BYTE, 0);
            checkError("glReadPixels", gl);

            try {
                callRenderingListeners(new RenderingEvent(autoDrawable, RenderingEvent.BEFORE_BUFFER_SWAP));
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

            setValue(PerformanceStatistic.FRAME_TIME, sc.getFrameTime());
            setValue(PerformanceStatistic.FRAME_RATE, sc.getFramesPerSecond());
            setValue(PerformanceStatistic.READ_PIXELS_COUNT, readPixelsCount);
            setValue(PerformanceStatistic.DRAW_CALLS, drawCalls);

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
                            new PositionEvent(autoDrawable, sc.getPickPoint(), positionAtStart, positionAtEnd));
                    }
                } else {
                    callPositionListeners(
                        new PositionEvent(autoDrawable, sc.getPickPoint(), positionAtStart, positionAtEnd));
                }
            }

            PickedObject selectionAtEnd = getCurrentSelection();
            if (selectionAtStart != null || selectionAtEnd != null) {
                callSelectListeners(
                    new SelectEvent(autoDrawable, SelectEvent.ROLLOVER, sc.getPickPoint(), sc.getPickedObjectList()));
            }

            PickedObjectList boxSelectionAtEnd = getCurrentBoxSelection();
            if (boxSelectionAtStart != null || boxSelectionAtEnd != null) {
                callSelectListeners(
                    new SelectEvent(
                        autoDrawable, SelectEvent.BOX_ROLLOVER, sc.getPickRectangle(), sc.getObjectsInPickRectangle()));
            }

            FrameRenderedHandler handler = frameRenderedHandler;
            if (handler != null) {
                // This method will block until the asynchronous operation that was started by glReadPixels
                // has completed. After we're finished, we need to unmap the pixel buffer.
                //
                ByteBuffer buffer = gl.glMapBuffer(GL2.GL_PIXEL_PACK_BUFFER, GL2.GL_READ_ONLY);
                checkError("glMapBuffer", gl);

                try {
                    handler.frameRendered(buffer, currentWidth, currentHeight);
                } finally {
                    gl.glUnmapBuffer(GL2.GL_PIXEL_PACK_BUFFER);
                    checkError("glUnmapBuffer", gl);
                }
            }

            gl.glBindBuffer(GL2.GL_PIXEL_PACK_BUFFER, 0);

            try {
                callRenderingListeners(new RenderingEvent(autoDrawable, RenderingEvent.AFTER_BUFFER_SWAP));
            } catch (Exception e) {
                Logging.logger()
                    .log(
                        Level.SEVERE,
                        Logging.getMessage("WorldWindowGLAutoDrawable.ExceptionDuringGLEventListenerDisplay"),
                        e);
            }
        } catch (Exception e) {
            Logging.logger()
                .log(Level.SEVERE, Logging.getMessage("WorldWindowGLCanvas.ExceptionAttemptingRepaintWorldWindow"), e);
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
                    onMessage(new Message(View.VIEW_STOPPED, WWOffscreenDrawable.this));
                }
            };

        // Cancel the previous view stop task
        if (this.viewRefreshTask != null) {
            this.viewRefreshTask.cancel(false);
        }

        // Schedule the task for execution in delay milliseconds
        this.viewRefreshTask = scheduledTaskService.addScheduledTask(viewStoppedTask, delay, TimeUnit.MILLISECONDS);
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

    private boolean isGLContextCompatible(GLContext context) {
        return context != null && context.isGL2();
    }

    private String[] getRequiredOglFunctions() {
        return new String[] {"glActiveTexture", "glClientActiveTexture"};
    }

    private String[] getRequiredOglExtensions() {
        return new String[] {};
    }

    private static final long FALLBACK_TEXTURE_CACHE_SIZE = 60000000;

    private static GpuResourceCache sharedResourceCache;

    static GpuResourceCache getSharedGpuResourceCache() {
        if (sharedResourceCache != null) {
            return sharedResourceCache;
        }

        long cacheSize = Configuration.getLongValue(AVKey.TEXTURE_CACHE_SIZE, FALLBACK_TEXTURE_CACHE_SIZE);
        return sharedResourceCache = new BasicGpuResourceCache((long)(0.8 * cacheSize), cacheSize);
    }

    private static final Object autoDrawableMutex = new Object();
    private static int refCount;
    private static GLAutoDrawable autoDrawable;

    private static void reconfigureAutoDrawable() {
        synchronized (autoDrawableMutex) {
            Logging.logger().info("Reconfiguring OpenGL surface...");

            List<GLEventListener> eventListenerList = new ArrayList<>();
            for (int i = 0; i < autoDrawable.getGLEventListenerCount(); ++i) {
                eventListenerList.add(autoDrawable.getGLEventListener(i));
            }

            GLProfile profile = Configuration.getMaxCompatibleGLProfile();
            GLCapabilitiesImmutable caps = autoDrawable.getChosenGLCapabilities();

            try {
                autoDrawable.destroy();
            } catch (GLException ignored) {
            }

            autoDrawable =
                GLDrawableFactory.getFactory(profile)
                    .createDummyAutoDrawable(null, true, caps, new BasicGLCapabilitiesChooser());
            autoDrawable.setAutoSwapBufferMode(false);

            for (GLEventListener listener : eventListenerList) {
                autoDrawable.addGLEventListener(listener);
                autoDrawable.setGLEventListenerInitState(listener, true);
            }
        }
    }

    private static void createAutoDrawable(GLEventListener listener) {
        synchronized (autoDrawableMutex) {
            if (autoDrawable == null) {
                GLProfile profile = Configuration.getMaxCompatibleGLProfile();
                GLCapabilities caps = Configuration.getRequiredGLCapabilities();
                caps.setOnscreen(false);
                caps.setDoubleBuffered(false);
                caps.setHardwareAccelerated(true);

                autoDrawable =
                    GLDrawableFactory.getFactory(profile)
                        .createDummyAutoDrawable(null, true, caps, new BasicGLCapabilitiesChooser());
                autoDrawable.setAutoSwapBufferMode(false);
            }

            autoDrawable.addGLEventListener(listener);
            ++refCount;
        }
    }

    private static boolean releaseAutoDrawable() {
        synchronized (autoDrawableMutex) {
            if (--refCount == 0) {
                try {
                    if (!autoDrawable.getContext().isCurrent()) {
                        autoDrawable.getContext().makeCurrent();
                    }

                    autoDrawable.getContext().release();
                    autoDrawable.destroy();
                } catch (GLException e) {
                    Logging.logger().warning(e.getMessage());
                }

                autoDrawable = null;
                return true;
            }

            return false;
        }
    }

}
