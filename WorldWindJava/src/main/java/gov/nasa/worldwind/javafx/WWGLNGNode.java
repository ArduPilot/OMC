/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package gov.nasa.worldwind.javafx;

import com.jogamp.nativewindow.util.Dimension;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawableFactory;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.prism.Graphics;
import com.sun.prism.PixelFormat;
import com.sun.prism.Texture;
import java.lang.reflect.Field;
import java.util.concurrent.locks.Lock;

class WWGLNGNode extends NGNode {

    interface ContextChangedListener {
        void changed(GLSharedContext context);
    }

    private static Field ES2Texture_resourceField;
    private static Field ES2TextureResource_resourceField;
    private static Field ES2TextureData_texIDField;

    static {
        try {
            ES2Texture_resourceField =
                Class.forName("com.sun.prism.es2.ES2Texture").getSuperclass().getDeclaredField("resource");
            ES2Texture_resourceField.setAccessible(true);

            ES2TextureResource_resourceField =
                Class.forName("com.sun.prism.es2.ES2TextureResource")
                    .getSuperclass()
                    .getSuperclass()
                    .getDeclaredField("resource");
            ES2TextureResource_resourceField.setAccessible(true);

            ES2TextureData_texIDField = Class.forName("com.sun.prism.es2.ES2TextureData").getDeclaredField("texID");
            ES2TextureData_texIDField.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean initialized;
    private Lock lock;
    private Texture texture;
    private int targetTextureHandle;
    private int sourceTextureHandle;
    private boolean textureInvalidated;
    private GLHelper helper;
    private ContextChangedListener contextChangedListener;
    private volatile int frameNumber;

    void setContextChangedListener(ContextChangedListener listener) {
        contextChangedListener = listener;
    }

    int getFrameNumber() {
        return frameNumber;
    }

    // These fields must not be changed while renderContent is running
    synchronized void setFrameData(int textureHandle, Lock lock) {
        this.lock = lock;
        sourceTextureHandle = textureHandle;
        textureInvalidated = true;
    }

    @Override
    protected synchronized void renderContent(Graphics graphics) {
        ensureInitialized();

        if (textureInvalidated) {
            lock.lock();

            try {
                Dimension textureSize = helper.getTextureSize(sourceTextureHandle);
                int textureWidth = textureSize.getWidth();
                int textureHeight = textureSize.getHeight();

                if (texture == null
                        || textureWidth != texture.getPhysicalWidth()
                        || textureHeight != texture.getPhysicalHeight()) {
                    if (texture != null) {
                        texture.dispose();
                        texture = null;
                    }

                    if (textureWidth > 0 && textureHeight > 0) {
                        texture =
                            graphics.getResourceFactory()
                                .createTexture(
                                    PixelFormat.BYTE_BGRA_PRE,
                                    Texture.Usage.STATIC,
                                    Texture.WrapMode.CLAMP_NOT_NEEDED,
                                    textureWidth,
                                    textureHeight);

                        texture.makePermanent();
                        targetTextureHandle = getTextureHandle(texture);
                    }
                }

                helper.copyTexture(sourceTextureHandle, targetTextureHandle, textureWidth, textureHeight);
            } finally {
                lock.unlock();
            }

            textureInvalidated = false;
            ++frameNumber;
        }

        if (texture != null) {
            float width = texture.getPhysicalWidth();
            float height = texture.getPhysicalHeight();
            graphics.drawTextureRaw(texture, 0, 0, width, height, 0, 0, 1, -1);
        }
    }

    @Override
    protected boolean hasOverlappingContents() {
        return false;
    }

    @Override
    public void release() {
        if (helper != null) {
            helper.close();
            helper = null;
        }

        super.release();
    }

    private void ensureInitialized() {
        if (!initialized) {
            GLContext jfxContext = GLDrawableFactory.getDesktopFactory().createExternalGLContext();
            GLSharedContext wwContext = new GLSharedContext(jfxContext, false);
            helper = new GLHelper(this, jfxContext.getGL().getGL3());
            initialized = true;

            if (contextChangedListener != null) {
                contextChangedListener.changed(wwContext);
            }
        }
    }

    private int getTextureHandle(Texture texture) {
        try {
            Object resource0 = ES2Texture_resourceField.get(texture);
            Object resource1 = ES2TextureResource_resourceField.get(resource0);
            return (int)ES2TextureData_texIDField.get(resource1);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

}
