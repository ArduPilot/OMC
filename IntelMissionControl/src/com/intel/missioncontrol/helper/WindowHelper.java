/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.helper;

import com.intel.missioncontrol.ui.DecoratedScene;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import de.saxsys.mvvmfx.ViewModel;
import gov.nasa.worldwind.WorldWindow;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class WindowHelper {

    @SuppressWarnings("UnusedReturnValue")
    private interface Dwmapi extends StdCallLibrary, WinUser, WinNT {
        Dwmapi INSTANCE = (Dwmapi)Native.loadLibrary("dwmapi", Dwmapi.class, W32APIOptions.DEFAULT_OPTIONS);

        HRESULT DwmSetWindowAttribute(HWND hwnd, DWORD dwAttribute, Pointer pvAttribute, DWORD cbAttribute);
    }

    public static class Accessor {
        public static void setPrimaryStage(Stage stage) {
            if (primaryStage != null) {
                throw new IllegalStateException("Primary stage can only be set once.");
            }

            stage.outputScaleXProperty()
                .addListener((observable, oldValue, newValue) -> ScaleHelper.setScaleFactor(newValue.doubleValue()));
            ScaleHelper.setScaleFactor(stage.getOutputScaleX());
            primaryStage = stage;
        }

        public static void setPrimaryWorldWindow(WorldWindow worldWindow) {
            if (primaryWorldWindow != null) {
                throw new IllegalStateException("Primary WorldWindow can only be set once.");
            }

            primaryWorldWindow = worldWindow;
        }

        public static void setPrimaryViewModel(ViewModel viewModel) {
            primaryViewModel = viewModel;
        }

    }

    private static Stage primaryStage;
    private static ViewModel primaryViewModel;
    private static WorldWindow primaryWorldWindow;

    public static void setCloaked(Window window, boolean cloaked) {
        if (window.isShowing()) {
            setCloakedInternal(window, cloaked, null);
        } else {
            EventHandler<? super WindowEvent> handler =
                new EventHandler<>() {
                    @Override
                    public void handle(WindowEvent event) {
                        setCloakedInternal(window, cloaked, this);
                    }
                };

            window.addEventHandler(WindowEvent.WINDOW_SHOWN, handler);
        }
    }

    private static void setCloakedInternal(Window window, boolean cloaked, EventHandler<? super WindowEvent> handler) {
        if (handler != null) {
            window.removeEventHandler(WindowEvent.WINDOW_SHOWN, handler);
        }

        com.sun.javafx.tk.TKStage peer = com.sun.javafx.stage.WindowHelper.getPeer(window);
        final WinDef.HWND hwnd = new WinDef.HWND(Pointer.createConstant(peer.getRawHandle()));
        IntByReference ref = new IntByReference(cloaked ? 1 : 0);
        Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, new WinDef.DWORD(13), ref.getPointer(), new WinDef.DWORD(8));
    }

    public static void closePrimaryStage() {
        primaryStage.fireEvent(new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    // TODO: Only here until code that still uses this is cleaned up.
    @Deprecated
    public static ViewModel getPrimaryViewModel() {
        return primaryViewModel;
    }

    public static Parent getRoot(Scene scene) {
        if (scene instanceof DecoratedScene) {
            DecoratedScene decoratedScene = (DecoratedScene)scene;
            return decoratedScene.getInternalRoot();
        }

        return scene.getRoot();
    }

    public static WorldWindow getPrimaryWorldWindow() {
        return primaryWorldWindow;
    }

}
