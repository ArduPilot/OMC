/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package gov.nasa.worldwind.javafx;

import static com.sun.jna.platform.win32.WinGDI.PFD_MAIN_PLANE;
import static com.sun.jna.platform.win32.WinGDI.PFD_SUPPORT_OPENGL;
import static com.sun.jna.platform.win32.WinGDI.PFD_TYPE_RGBA;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLException;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.OpenGL32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import gov.nasa.worldwind.util.Logging;

/**
 * Prism-ES2 creates an OpenGL context by calling wglCreateContext, while JOGL prefers to create a context by using the
 * WGL_ARB_create_context extension. In order to facilitate context sharing with the JavaFX context, we create an OpenGL
 * context via wglCreateContext here, and then share it with wglShareLists.
 */
class GLSharedContext {

    private interface OpenGL32Ex extends StdCallLibrary, WinUser, WinNT {
        OpenGL32Ex INSTANCE = Native.loadLibrary("opengl32", OpenGL32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean wglShareLists(HGLRC context1, HGLRC context2);
    }

    private final GLContext externalContext;
    private Thread initialThread;

    GLSharedContext(GLContext shareWith, boolean createDevice) {
        WinDef.HDC hdc;

        if (createDevice) {
            WinDef.HWND hwnd =
                User32.INSTANCE.CreateWindowEx(
                    0, "STATIC", "DUMMY", 0, 0, 0, 64, 64, User32.HWND_MESSAGE, null, null, null);
            if (hwnd == null || Pointer.nativeValue(hwnd.getPointer()) == 0) {
                throw new RuntimeException(
                    "CreateWindowEx failed: " + getErrorString(Kernel32.INSTANCE.GetLastError()));
            }

            hdc = User32.INSTANCE.GetDC(hwnd);
            if (hdc == null || Pointer.nativeValue(hdc.getPointer()) == 0) {
                throw new RuntimeException("GetDC failed: " + getErrorString(Kernel32.INSTANCE.GetLastError()));
            }

            WinGDI.PIXELFORMATDESCRIPTOR.ByReference pfd = new WinGDI.PIXELFORMATDESCRIPTOR.ByReference();
            pfd.dwFlags = PFD_SUPPORT_OPENGL | PFD_TYPE_RGBA;
            pfd.iLayerType = PFD_MAIN_PLANE;
            pfd.cDepthBits = 32;
            pfd.cColorBits = 32;
            int pixelFormat = GDI32.INSTANCE.ChoosePixelFormat(hdc, pfd);
            if (pixelFormat == 0) {
                throw new RuntimeException(
                    "ChoosePixelFormat failed: " + getErrorString(Kernel32.INSTANCE.GetLastError()));
            }

            if (!GDI32.INSTANCE.SetPixelFormat(hdc, pixelFormat, pfd)) {
                throw new RuntimeException(
                    "SetPixelFormat failed: " + getErrorString(Kernel32.INSTANCE.GetLastError()));
            }
        } else {
            hdc = new WinDef.HDC(new Pointer(shareWith.getGLDrawable().getNativeSurface().getSurfaceHandle()));
        }

        WinDef.HGLRC newContext = OpenGL32.INSTANCE.wglCreateContext(hdc);
        if (newContext == null || Pointer.nativeValue(newContext.getPointer()) == 0) {
            throw new RuntimeException("wglCreateContext failed: " + getErrorString(Kernel32.INSTANCE.GetLastError()));
        }

        if (!OpenGL32Ex.INSTANCE.wglShareLists(new WinDef.HGLRC(new Pointer(shareWith.getHandle())), newContext)) {
            throw new RuntimeException("wglShareLists failed: " + getErrorString(Kernel32.INSTANCE.GetLastError()));
        }

        WinDef.HGLRC currentContext = OpenGL32.INSTANCE.wglGetCurrentContext();
        if (!OpenGL32.INSTANCE.wglMakeCurrent(hdc, newContext)) {
            throw new RuntimeException("wglMakeCurrent failed: " + getErrorString(Kernel32.INSTANCE.GetLastError()));
        }

        externalContext = GLDrawableFactory.getDesktopFactory().createExternalGLContext();

        long jfxSurfaceHandle = shareWith.getGLDrawable().getNativeSurface().getSurfaceHandle();
        WinDef.HDC jfxHdc = new WinDef.HDC(new Pointer(jfxSurfaceHandle));

        if (!OpenGL32.INSTANCE.wglMakeCurrent(jfxHdc, currentContext)) {
            throw new RuntimeException("wglMakeCurrent failed: " + getErrorString(Kernel32.INSTANCE.GetLastError()));
        }
    }

    GLContext getContext() {
        return externalContext;
    }

    void makeCurrent() throws GLException {
        if (initialThread == null) {
            initialThread = Thread.currentThread();
        } else if (initialThread != Thread.currentThread()) {
            throw new IllegalStateException("Attempting to make context current on unexpected thread.");
        }

        long hdc = externalContext.getGLDrawable().getNativeSurface().getSurfaceHandle();
        long hglrc = externalContext.getHandle();

        // wglMakeCurrent sometimes fails with ERROR_TRANSFORM_NOT_SUPPORTED.
        // It seems that trying again will succeed in almost all cases. Why is that happening?
        //
        int count = 0;
        boolean current = false;
        while (!current && count < 3) {
            ++count;
            current =
                OpenGL32.INSTANCE.wglMakeCurrent(
                    new WinDef.HDC(new Pointer(hdc)), new WinDef.HGLRC(new Pointer(hglrc)));
        }

        if (!current) {
            Logging.logger()
                .warning(
                    "wglMakeCurrent("
                        + hdc
                        + ", "
                        + hglrc
                        + ") failed: "
                        + getErrorString(Kernel32.INSTANCE.GetLastError()));
        }

        externalContext.makeCurrent();
        externalContext.getGL().setSwapInterval(0);
    }

    void release() throws GLException {
        if (!OpenGL32.INSTANCE.wglMakeCurrent(new WinDef.HDC(Pointer.NULL), new WinDef.HGLRC(Pointer.NULL))) {
            Logging.logger()
                .warning("wglMakeCurrent(0, 0) failed: " + getErrorString(Kernel32.INSTANCE.GetLastError()));
        }

        externalContext.release();
    }

    GL getGL() {
        return externalContext.getGL();
    }

    private String getErrorString(int errno) {
        return Kernel32Util.formatMessage(errno) + " [0x" + Integer.toHexString(errno).toUpperCase() + "]";
    }

}
