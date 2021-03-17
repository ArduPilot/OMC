/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package gov.nasa.worldwind.javafx;

import com.jogamp.nativewindow.util.Dimension;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;
import gov.nasa.worldwind.util.Logging;

class GLHelper implements AutoCloseable {

    private final Object owner;
    private final GL3 gl;
    private int fbo = -1;

    GLHelper(Object owner, GL3 gl) {
        this.owner = owner;
        this.gl = gl;
    }

    @Override
    public void close() {
        if (fbo != -1) {
            int[] param = new int[] {fbo};
            gl.glDeleteFramebuffers(1, param, 0);
            fbo = -1;
        }
    }

    void checkError(String methodName) {
        int errno = gl.glGetError();
        if (errno == GL.GL_NO_ERROR) {
            return;
        }

        String err = null;
        switch (errno) {
        case GL.GL_INVALID_ENUM:
            err = "GL_INVALID_ENUM";
            break;
        case GL.GL_INVALID_VALUE:
            err = "GL_INVALID_VALUE";
            break;
        case GL.GL_INVALID_OPERATION:
            err = "GL_INVALID_OPERATION";
            break;
        case 0x0503:
            err = "GL_STACK_OVERFLOW";
            break;
        case 0x0504:
            err = "GL_STACK_UNDERFLOW";
            break;
        case GL.GL_OUT_OF_MEMORY:
            err = "GL_OUT_OF_MEMORY";
            break;
        case GL.GL_INVALID_FRAMEBUFFER_OPERATION:
            err = "GL_INVALID_FRAMEBUFFER_OPERATION";
            break;
        }

        if (err == null) {
            err = "0x" + Integer.toHexString(errno);
        }

        Logging.logger().severe("[" + owner.getClass().getSimpleName() + "]." + methodName + " failed: " + err);
    }

    void checkFramebuffer(int target) {
        String err = null;
        switch (gl.glCheckFramebufferStatus(target)) {
        case GL.GL_FRAMEBUFFER_UNSUPPORTED:
            err = "GL_FRAMEBUFFER_UNSUPPORTED";
            break;
        case GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
            err = "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
            break;
        case GL.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
            err = "GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS";
            break;
        case GL.GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
            err = "GL_FRAMEBUFFER_INCOMPLETE_FORMATS";
            break;
        case GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
            err = "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
            break;
        case GL2.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
            err = "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER";
            break;
        case GL.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
            err = "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE";
            break;
        case GL2.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
            err = "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER";
            break;
        }

        if (err != null) {
            Logging.logger()
                .severe("[" + owner.getClass().getSimpleName() + "].glCheckFramebufferStatus() failed: " + err);
        }
    }

    Dimension getTextureSize(int textureHandle) {
        int[] param = new int[3];
        gl.glGetIntegerv(GL.GL_TEXTURE_BINDING_2D, param, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureHandle);
        gl.glGetTexLevelParameteriv(GL.GL_TEXTURE_2D, 0, GL2.GL_TEXTURE_WIDTH, param, 1);
        gl.glGetTexLevelParameteriv(GL.GL_TEXTURE_2D, 0, GL2.GL_TEXTURE_HEIGHT, param, 2);
        gl.glBindTexture(GL.GL_TEXTURE_2D, param[0]);
        return new Dimension(param[1], param[2]);
    }

    void copyTexture(int sourceTexture, int targetTexture, int width, int height) {
        int[] params = new int[3];
        if (fbo == -1) {
            gl.glGenFramebuffers(1, params, 1);
            fbo = params[1];
        }

        gl.glGetIntegerv(GL.GL_READ_FRAMEBUFFER_BINDING, params, 0);
        gl.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, fbo);
        gl.glFramebufferTexture(GL.GL_READ_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, sourceTexture, 0);
        checkError("glFramebufferTexture");

        gl.glGetIntegerv(GL.GL_TEXTURE_BINDING_2D, params, 2);
        gl.glBindTexture(GL.GL_TEXTURE_2D, targetTexture);
        gl.glCopyTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
        checkError("glCopyTexSubImage2D");

        gl.glBindTexture(GL.GL_TEXTURE_2D, params[2]);
        gl.glBindFramebuffer(GL.GL_READ_FRAMEBUFFER, params[0]);
        gl.glFlush();
    }

}
