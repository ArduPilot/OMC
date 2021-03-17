/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import static com.sun.jna.platform.win32.BaseTSD.DWORD_PTR;
import static com.sun.jna.platform.win32.WinDef.BOOLByReference;
import static com.sun.jna.platform.win32.WinDef.DWORDByReference;
import static com.sun.jna.platform.win32.WinDef.HWND;
import static com.sun.jna.platform.win32.WinDef.LRESULT;
import static com.sun.jna.platform.win32.WinDef.RECT;
import static com.sun.jna.platform.win32.WinDef.UINT;
import static com.sun.jna.platform.win32.WinDef.UINT_PTR;
import static com.sun.jna.platform.win32.WinDef.WPARAM;
import static com.sun.jna.platform.win32.WinNT.HRESULT;
import static com.sun.jna.platform.win32.WinUser.GWL_STYLE;
import static com.sun.jna.platform.win32.WinUser.HMONITOR;
import static com.sun.jna.platform.win32.WinUser.MONITORINFO;
import static com.sun.jna.platform.win32.WinUser.WM_DESTROY;
import static com.sun.jna.platform.win32.WinUser.WS_CAPTION;
import static com.sun.jna.platform.win32.WinUser.WS_MAXIMIZEBOX;
import static com.sun.jna.platform.win32.WinUser.WS_MINIMIZEBOX;
import static com.sun.jna.platform.win32.WinUser.WS_THICKFRAME;

import com.sun.javafx.geom.PickRay;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.input.PickResultChooser;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class DecoratedScene extends Scene {

    private static final PseudoClass INVERTED = PseudoClass.getPseudoClass("inverted");
    private static final String STYLESHEET = "/com/intel/missioncontrol/ui/DecoratedScene.css";
    private static final double SHADOW_WIDTH = 16;

    private static class Icons {
        final ImageView minimizeWhite;
        final ImageView minimizeBlack;
        final ImageView maximizeWhite;
        final ImageView maximizeBlack;
        final ImageView restoreWhite;
        final ImageView restoreBlack;
        final ImageView closeWhite;
        final ImageView closeBlack;

        @SuppressWarnings("ConstantConditions")
        Icons(double iconSize) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            minimizeWhite =
                new ImageView(
                    classLoader
                        .getResource("/com/intel/missioncontrol/icons/icon_window-minimize(fill=white).svg")
                        .toExternalForm());
            minimizeWhite.setFitWidth(iconSize);
            minimizeWhite.setFitHeight(iconSize);

            minimizeBlack =
                new ImageView(
                    classLoader
                        .getResource("/com/intel/missioncontrol/icons/icon_window-minimize(fill=black).svg")
                        .toExternalForm());
            minimizeBlack.setFitWidth(iconSize);
            minimizeBlack.setFitHeight(iconSize);

            maximizeWhite =
                new ImageView(
                    classLoader
                        .getResource("/com/intel/missioncontrol/icons/icon_window-maximize(fill=white).svg")
                        .toExternalForm());
            maximizeWhite.setFitWidth(iconSize);
            maximizeWhite.setFitHeight(iconSize);

            maximizeBlack =
                new ImageView(
                    classLoader
                        .getResource("/com/intel/missioncontrol/icons/icon_window-maximize(fill=black).svg")
                        .toExternalForm());
            maximizeBlack.setFitWidth(iconSize);
            maximizeBlack.setFitHeight(iconSize);

            restoreWhite =
                new ImageView(
                    classLoader
                        .getResource("/com/intel/missioncontrol/icons/icon_window-restore(fill=white).svg")
                        .toExternalForm());
            restoreWhite.setFitWidth(iconSize);
            restoreWhite.setFitHeight(iconSize);

            restoreBlack =
                new ImageView(
                    classLoader
                        .getResource("/com/intel/missioncontrol/icons/icon_window-restore(fill=black).svg")
                        .toExternalForm());
            restoreBlack.setFitWidth(iconSize);
            restoreBlack.setFitHeight(iconSize);

            closeWhite =
                new ImageView(
                    classLoader
                        .getResource("/com/intel/missioncontrol/icons/icon_window-close(fill=white).svg")
                        .toExternalForm());
            closeWhite.setFitWidth(iconSize);
            closeWhite.setFitHeight(iconSize);

            closeBlack =
                new ImageView(
                    classLoader
                        .getResource("/com/intel/missioncontrol/icons/icon_window-close(fill=black).svg")
                        .toExternalForm());
            closeBlack.setFitWidth(iconSize);
            closeBlack.setFitHeight(iconSize);
        }
    }

    private static class WindowMetrics {
        final double frameWidthScaled;
        final double iconSizeUnscaled;
        final double titleBarHeightUnscaled;

        WindowMetrics(double dpiFactor) {
            UINT dpi = new UINT((int)(dpiFactor * 96));
            double frameWidthScaled, iconSizeUnscaled, titleBarHeightUnscaled;

            // GetSystemMetricsForDpi is only available in Windows versions later than Windows 10, 1607.
            // If unavailable, fall back to using GetSystemMetrics.
            try {
                iconSizeUnscaled = (User32Ex.INSTANCE.GetSystemMetricsForDpi(User32.SM_CYSMSIZE, dpi) / 2) / dpiFactor;

                titleBarHeightUnscaled =
                    (User32Ex.INSTANCE.GetSystemMetricsForDpi(User32.SM_CYCAPTION, dpi)
                            + User32Ex.INSTANCE.GetSystemMetricsForDpi(User32.SM_CYFRAME, dpi)
                            + User32Ex.INSTANCE.GetSystemMetricsForDpi(User32.SM_CXPADDEDBORDER, dpi))
                        / dpiFactor;
            } catch (UnsatisfiedLinkError e) {
                iconSizeUnscaled = (User32.INSTANCE.GetSystemMetrics(User32.SM_CYSMSIZE) / 2) / dpiFactor;

                titleBarHeightUnscaled =
                    (User32.INSTANCE.GetSystemMetrics(User32.SM_CYCAPTION)
                            + User32.INSTANCE.GetSystemMetrics(User32.SM_CYFRAME)
                            + User32.INSTANCE.GetSystemMetrics(User32.SM_CXPADDEDBORDER))
                        / dpiFactor;
            }

            frameWidthScaled =
                User32.INSTANCE.GetSystemMetrics(User32.SM_CXFRAME)
                    + User32.INSTANCE.GetSystemMetrics(User32.SM_CXPADDEDBORDER);

            this.frameWidthScaled = frameWidthScaled;
            this.iconSizeUnscaled = iconSizeUnscaled;
            this.titleBarHeightUnscaled = titleBarHeightUnscaled;
        }
    }

    private enum WindowRegion {
        NONE,
        TITLE,
        NORTH,
        NORTH_EAST,
        EAST,
        SOUTH_EAST,
        SOUTH,
        SOUTH_WEST,
        WEST,
        NORTH_WEST;

        public Cursor toCursor() {
            switch (this) {
            case NORTH:
                return Cursor.N_RESIZE;
            case NORTH_EAST:
                return Cursor.NE_RESIZE;
            case EAST:
                return Cursor.E_RESIZE;
            case SOUTH_EAST:
                return Cursor.SE_RESIZE;
            case SOUTH:
                return Cursor.S_RESIZE;
            case SOUTH_WEST:
                return Cursor.SW_RESIZE;
            case WEST:
                return Cursor.W_RESIZE;
            case NORTH_WEST:
                return Cursor.NW_RESIZE;
            default:
                return Cursor.DEFAULT;
            }
        }
    }

    private enum DockedState {
        NONE,
        LEFT,
        RIGHT,
        TOP
    }

    private class DragContext {
        final Point2D startDrag;
        final WindowRegion windowRegion;
        final Rectangle2D initialWindowRect;

        DragContext(MouseEvent event, boolean titleBar) {
            startDrag = new Point2D(event.getScreenX(), event.getScreenY());
            windowRegion = titleBar ? WindowRegion.TITLE : getWindowRegion(event.getX(), event.getY());
            Window window = getWindow();
            initialWindowRect = new Rectangle2D(window.getX(), window.getY(), window.getWidth(), window.getHeight());
        }
    }

    private interface User32Ex extends StdCallLibrary, WinUser, WinNT {
        User32Ex INSTANCE = (User32Ex)Native.loadLibrary("user32", User32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        DWORD GetSysColor(int nIndex);

        int GetSystemMetricsForDpi(int nIndex, UINT dpi);
    }

    private interface UxTheme extends StdCallLibrary, WinUser, WinNT {
        UxTheme INSTANCE = (UxTheme)Native.loadLibrary("uxtheme", UxTheme.class, W32APIOptions.DEFAULT_OPTIONS);

        HRESULT SetWindowTheme(HWND hwnd, String subAppName, String subIdList);
    }

    @SuppressWarnings("UnusedReturnValue")
    private interface Dwmapi extends StdCallLibrary, WinUser, WinNT {
        Dwmapi INSTANCE = (Dwmapi)Native.loadLibrary("dwmapi", Dwmapi.class, W32APIOptions.DEFAULT_OPTIONS);

        HRESULT DwmGetColorizationColor(DWORDByReference colorization, BOOLByReference opaqueBlend);
    }

    @SuppressWarnings("UnusedReturnValue")
    private interface Comctl32 extends StdCallLibrary, WinUser, WinNT {
        Comctl32 INSTANCE = (Comctl32)Native.loadLibrary("comctl32", Comctl32.class, W32APIOptions.DEFAULT_OPTIONS);

        interface SUBCLASSPROC extends StdCallCallback {
            LRESULT callback(
                    HWND hwnd, UINT msg, WPARAM wparam, Pointer lparam, UINT_PTR subclass, DWORD_PTR dwRefData);
        }

        boolean SetWindowSubclass(HWND hwnd, SUBCLASSPROC pfnSubclass, UINT_PTR subclass, DWORD_PTR dwRefData);

        boolean RemoveWindowSubclass(HWND hwnd, SUBCLASSPROC pfnSubclass, UINT_PTR subclass);

        LRESULT DefSubclassProc(HWND hwnd, UINT msg, WPARAM wparam, Pointer lparam);
    }

    private static final int WM_WINDOWPOSCHANGED = 0x0047;
    private static final int WM_DWMCOLORIZATIONCOLORCHANGED = 0x0320;
    private static final int WM_NCDESTROY = 0x0082;
    private static final int WM_NCCALCSIZE = 0x0083;
    private static final int WM_NCHITTEST = 0x0084;
    private static final int HTCLIENT = 1;
    private static final int HTCAPTION = 2;
    private static final int SWP_FRAMECHANGED = 0x0020;
    private static final int SWP_NOSIZE = 0x0001;
    private static final int SWP_NOMOVE = 0x0002;

    private final ObjectProperty<Node> customContent =
        new SimpleObjectProperty<>() {
            @Override
            protected void invalidated() {
                super.invalidated();
                Node node = get();
                node.setPickOnBounds(false);
                StackPane.setAlignment(node, Pos.CENTER_LEFT);
                titleBarContent.getChildren().clear();
                titleBarContent.getChildren().add(node);
            }
        };

    private final Comctl32.SUBCLASSPROC messageLoopRef = this::messageLoop; // store a strong ref to prevent GC
    private final Pane content;
    private final AnchorPane shadowWindowFrame;
    private final Pane appIconPane;
    private final StackPane titleBarContent;
    private final HBox titleBar;
    private final DropShadow dropShadow;
    private final VBox windowFrame;
    private final Button iconifyButton = new Button();
    private final Button maximizeRestoreButton = new Button();
    private final Button closeButton = new Button();
    private boolean isMaximized;
    private boolean isInvertedColor;
    private double currentBorderWidth;
    private double dpiFactor;
    private Icons icons;
    private WindowMetrics windowMetrics;
    private RECT windowRect;
    private DragContext dragContext;
    private DockedState dockedState;

    public DecoratedScene(Pane content) {
        super(new AnchorPane());
        this.content = content;
        content.setSnapToPixel(true);
        shadowWindowFrame = (AnchorPane)getRoot();
        shadowWindowFrame.minWidthProperty().bind(widthProperty());
        shadowWindowFrame.maxWidthProperty().bind(widthProperty());
        shadowWindowFrame.minHeightProperty().bind(heightProperty());
        shadowWindowFrame.maxHeightProperty().bind(heightProperty());
        shadowWindowFrame.setSnapToPixel(true);
        titleBar = new HBox();
        titleBar.setSnapToPixel(true);
        appIconPane = new Pane();
        titleBarContent = new StackPane();
        windowFrame = new VBox();
        windowFrame.setSnapToPixel(true);
        dropShadow = new DropShadow();
        windowProperty().addListener(this::windowChanged);
        setFill(null);
    }

    public ObjectProperty<Node> customContentProperty() {
        return customContent;
    }

    public Node getCustomContent() {
        return customContent.get();
    }

    public void setCustomContent(Node node) {
        customContent.set(node);
    }

    public Parent getInternalRoot() {
        return (Parent)windowFrame.getChildren().get(1);
    }

    private void prepareWindow() {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final Window window = getWindow();
        final HWND hwnd = new HWND(getWindowPointer(window));

        windowMetrics = new WindowMetrics(window.getOutputScaleX());
        icons = new Icons(windowMetrics.iconSizeUnscaled);
        dpiFactor = window.getOutputScaleX();
        window.focusedProperty().addListener(((observable, oldValue, newValue) -> updateTitleBarColor()));
        window.outputScaleXProperty()
            .addListener(((observable, oldValue, newValue) -> dpiFactor = newValue.doubleValue()));

        shadowWindowFrame.setBackground(
            new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        shadowWindowFrame.addEventFilter(MouseEvent.MOUSE_MOVED, this::mouseMoved);
        shadowWindowFrame.addEventFilter(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        shadowWindowFrame.addEventFilter(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        shadowWindowFrame.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::mouseDragged);

        titleBar.setPrefHeight(windowMetrics.titleBarHeightUnscaled);
        titleBar.setMinHeight(windowMetrics.titleBarHeightUnscaled);
        titleBar.setMaxHeight(windowMetrics.titleBarHeightUnscaled);
        titleBar.getStylesheets().add(contextClassLoader.getResource(STYLESHEET).toExternalForm());

        appIconPane.prefWidthProperty().bind(appIconPane.heightProperty());

        iconifyButton.prefHeightProperty().bind(titleBar.heightProperty());
        iconifyButton.setOnAction(this::iconifyClicked);
        iconifyButton.setFocusTraversable(false);

        maximizeRestoreButton.prefHeightProperty().bind(titleBar.heightProperty());
        maximizeRestoreButton.setOnAction(this::maximizeRestoreClicked);
        maximizeRestoreButton.setFocusTraversable(false);

        closeButton.setId("closeButton");
        closeButton.prefHeightProperty().bind(titleBar.heightProperty());
        closeButton.setFocusTraversable(false);
        closeButton.setOnAction(event -> window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST)));

        updateIconColor();
        updateTitleBarColor();

        titleBar.getChildren().add(appIconPane);
        titleBar.getChildren().add(titleBarContent);
        titleBar.getChildren().add(iconifyButton);
        titleBar.getChildren().add(maximizeRestoreButton);
        titleBar.getChildren().add(closeButton);

        titleBarContent.setPickOnBounds(false);
        HBox.setHgrow(appIconPane, Priority.NEVER);
        HBox.setHgrow(titleBarContent, Priority.ALWAYS);
        HBox.setHgrow(iconifyButton, Priority.NEVER);
        HBox.setHgrow(maximizeRestoreButton, Priority.NEVER);
        HBox.setHgrow(closeButton, Priority.NEVER);

        AnchorPane clippingContainer = new AnchorPane();
        Rectangle rect = new Rectangle();
        rect.widthProperty().bind(clippingContainer.widthProperty());
        rect.heightProperty().bind(clippingContainer.heightProperty());
        clippingContainer.getChildren().add(content);
        content.setClip(rect);

        DoubleBinding contentHeight = windowFrame.heightProperty().subtract(titleBar.getPrefHeight());
        content.minWidthProperty().bind(windowFrame.widthProperty());
        content.maxWidthProperty().bind(windowFrame.widthProperty());
        content.minHeightProperty().bind(contentHeight);
        content.maxHeightProperty().bind(contentHeight);

        dropShadow.setBlurType(BlurType.GAUSSIAN);
        dropShadow.setColor(new Color(0, 0, 0, 0.5));
        dropShadow.setRadius(SHADOW_WIDTH);

        windowFrame.setEffect(dropShadow);
        windowFrame.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        windowFrame.getChildren().add(titleBar);
        windowFrame.getChildren().add(clippingContainer);

        shadowWindowFrame.getChildren().add(windowFrame);

        // This removes the default window theme, which gives us non-rounded top-left and top-right corners and fixes
        // some issues with window animations.
        check(UxTheme.INSTANCE.SetWindowTheme(hwnd, "", ""), "SetWindowTheme");

        // JavaFX provides us with a borderless transparent window, which doesn't have window animations. We need to add
        // back some window styles to re-enable window animations.
        long oldStyle = User32.INSTANCE.GetWindowLongPtr(hwnd, GWL_STYLE).longValue();
        long newStyle = oldStyle | WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX | WS_CAPTION;
        check(User32.INSTANCE.SetWindowLongPtr(hwnd, GWL_STYLE, new Pointer(newStyle)), "SetWindowLongPtr");

        // Subclass the original JavaFX window so we can listen in to the message loop.
        check(
            Comctl32.INSTANCE.SetWindowSubclass(hwnd, messageLoopRef, new UINT_PTR(1), new DWORD_PTR(0)),
            "SetWindowSubclass");

        // Since we changed the window frame, we need to tell Windows to update our window.
        check(
            User32.INSTANCE.SetWindowPos(hwnd, null, 0, 0, 0, 0, SWP_FRAMECHANGED | SWP_NOSIZE | SWP_NOMOVE),
            "SetWindowPos");
    }

    private LRESULT messageLoop(
            HWND hwnd, UINT msg, WPARAM wparam, Pointer lparam, UINT_PTR subclassId, DWORD_PTR dwRefData) {
        switch (msg.intValue()) {
        case WM_WINDOWPOSCHANGED:
            updateShadowBorder(hwnd);
            break;
        case WM_DWMCOLORIZATIONCOLORCHANGED:
            int color = wparam.intValue();
            double r = ((color & 0x00ff0000) >> 16) / 255.0f;
            double g = ((color & 0x0000ff00) >> 8) / 255.0f;
            double b = (color & 0x000000ff) / 255.0f;
            setTitleBarColor(new Color(r, g, b, 1.0));
            return new LRESULT(0);
        case WM_NCCALCSIZE:
            // Handling this message removes the window decorations that we manually added to enable window
            // animations. This effectively leaves us with a borderless window again, but this time it supports
            // window animations.
            return new LRESULT(0);
        case WM_NCHITTEST:
            // Since we don't have non-client window decorations, we need to manually determine whether a click
            // should be interpreted as a non-client area click. This gives us window dragging functionality and
            // enables Aero snap and Aero peek.
            long value = Pointer.nativeValue(lparam);
            int x = (int)(short)(value & 0xffff);
            int y = (int)(short)(value >> 16);
            if (getWindowRegionAbs(x, y) == WindowRegion.TITLE) {
                return new LRESULT(HTCAPTION);
            } else {
                return new LRESULT(HTCLIENT);
            }
        case WM_NCDESTROY:
            Comctl32.INSTANCE.RemoveWindowSubclass(hwnd, messageLoopRef, subclassId);
            break;
        case WM_DESTROY:
            User32.INSTANCE.PostQuitMessage(0);
            break;
        }

        return Comctl32.INSTANCE.DefSubclassProc(hwnd, msg, wparam, lparam);
    }

    // TODO:
    // This method gives us the wrong title bar color if the user has enabled "Automatically pick an accent color
    // from my background" in Windows settings.
    private void updateTitleBarColor() {
        int color;
        if (getWindow().isFocused()) {
            DWORDByReference colorization = new DWORDByReference();
            BOOLByReference opaqueBlend = new BOOLByReference();
            Dwmapi.INSTANCE.DwmGetColorizationColor(colorization, opaqueBlend);
            color = colorization.getValue().intValue();
        } else {
            color = User32Ex.INSTANCE.GetSysColor(11).intValue();
        }

        double r = ((color & 0x00ff0000) >> 16) / 255.0f;
        double g = ((color & 0x0000ff00) >> 8) / 255.0f;
        double b = (color & 0x000000ff) / 255.0f;
        setTitleBarColor(new Color(r, g, b, 1.0));
        updateIconColor();
    }

    private void setTitleBarColor(Color color) {
        double cr1 = getContrastRatio(Color.WHITE, color);
        double cr2 = getContrastRatio(Color.BLACK, color);
        isInvertedColor = cr1 > cr2;

        Node customContent = getCustomContent();
        if (customContent != null) {
            customContent.pseudoClassStateChanged(INVERTED, isInvertedColor);
        }

        titleBar.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    private void updateIconColor() {
        if (icons == null) {
            return;
        }

        boolean maximized = ((Stage)getWindow()).isMaximized();
        if (maximized) {
            maximizeRestoreButton.setGraphic(isInvertedColor ? icons.restoreWhite : icons.restoreBlack);
        } else {
            maximizeRestoreButton.setGraphic(isInvertedColor ? icons.maximizeWhite : icons.maximizeBlack);
        }

        iconifyButton.setGraphic(isInvertedColor ? icons.minimizeWhite : icons.minimizeBlack);
        closeButton.setGraphic(isInvertedColor ? icons.closeWhite : icons.closeBlack);
    }

    private double getRelativeLuminance(Color color) {
        double r = color.getRed() <= 0.03928 ? color.getRed() / 12.92 : Math.pow((color.getRed() + 0.055) / 1.055, 2.4);
        double g =
            color.getGreen() <= 0.03928 ? color.getGreen() / 12.92 : Math.pow((color.getGreen() + 0.055) / 1.055, 2.4);
        double b =
            color.getBlue() <= 0.03928 ? color.getBlue() / 12.92 : Math.pow((color.getBlue() + 0.055) / 1.055, 2.4);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private double getContrastRatio(Color color1, Color color2) {
        double L1 = getRelativeLuminance(color1);
        double L2 = getRelativeLuminance(color2);
        if (L1 > L2) {
            return (L1 + 0.05) / (L2 + 0.05);
        }

        return (L2 + 0.05) / (L1 + 0.05);
    }

    private void updateShadowBorder() {
        updateShadowBorder(new HWND(getWindowPointer(getWindow())));
    }

    private void updateShadowBorder(HWND hwnd) {
        HMONITOR monitor = User32.INSTANCE.MonitorFromWindow(hwnd, 0);
        if (monitor == null) {
            return;
        }

        MONITORINFO monitorinfo = new MONITORINFO();
        if (!User32.INSTANCE.GetMonitorInfo(monitor, monitorinfo).booleanValue()) {
            return;
        }

        RECT windowRect = new RECT();
        if (!User32.INSTANCE.GetWindowRect(hwnd, windowRect)) {
            return;
        }

        this.windowRect = windowRect;

        dockedState = getDockedState(windowRect, monitorinfo.rcWork);
        if (dockedState == DockedState.TOP) {
            // We add padding here because a maximized borderless window actually extends beyond the edge of the screen:
            // https://blogs.msdn.microsoft.com/oldnewthing/20120326-00/?p=8003
            currentBorderWidth = windowMetrics.frameWidthScaled / dpiFactor;
            AnchorPane.setLeftAnchor(windowFrame, currentBorderWidth);
            AnchorPane.setTopAnchor(windowFrame, currentBorderWidth);
            AnchorPane.setRightAnchor(windowFrame, currentBorderWidth);
            AnchorPane.setBottomAnchor(windowFrame, currentBorderWidth);
            windowFrame.setEffect(null);
        } else if (dockedState == DockedState.NONE) {
            currentBorderWidth = SHADOW_WIDTH;
            // shadowWindowFrame.setPadding(new Insets(SHADOW_WIDTH));
            AnchorPane.setLeftAnchor(windowFrame, SHADOW_WIDTH);
            AnchorPane.setTopAnchor(windowFrame, SHADOW_WIDTH);
            AnchorPane.setRightAnchor(windowFrame, SHADOW_WIDTH);
            AnchorPane.setBottomAnchor(windowFrame, SHADOW_WIDTH);
            windowFrame.setEffect(dropShadow);
        } else {
            currentBorderWidth = 0;
            AnchorPane.setLeftAnchor(windowFrame, 0.0);
            AnchorPane.setTopAnchor(windowFrame, 0.0);
            AnchorPane.setRightAnchor(windowFrame, 0.0);
            AnchorPane.setBottomAnchor(windowFrame, 0.0);
            windowFrame.setEffect(null);
        }
    }

    private DockedState getDockedState(RECT window, RECT workArea) {
        int frameWidth = (int)windowMetrics.frameWidthScaled;
        if (workArea.top == window.top + frameWidth
                && workArea.bottom == window.bottom - frameWidth
                && workArea.left == window.left + frameWidth
                && workArea.right == window.right - frameWidth) {
            return DockedState.TOP;
        }

        if (workArea.top == window.top && workArea.bottom == window.bottom && workArea.left == window.left) {
            return DockedState.LEFT;
        } else if (workArea.top == window.top && workArea.bottom == window.bottom && workArea.right == window.right) {
            return DockedState.RIGHT;
        }

        return DockedState.NONE;
    }

    private void maximizeRestoreClicked(ActionEvent event) {
        Window window = getWindow();
        if (!(window instanceof Stage)) {
            return;
        }

        Stage stage = (Stage)window;
        stage.setMaximized(!stage.isMaximized());
    }

    private void iconifyClicked(ActionEvent event) {
        Window window = getWindow();
        if (!(window instanceof Stage)) {
            return;
        }

        Stage stage = (Stage)window;
        stage.setIconified(true);
    }

    private void windowChanged(Observable observable) {
        Window window = getWindow();
        if (!(window instanceof Stage)) {
            return;
        }

        Stage stage = (Stage)window;
        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> prepareWindow());
        stage.maximizedProperty().addListener(o -> maximizedChanged());

        if (!stage.getIcons().isEmpty()) {
            Image icon = stage.getIcons().get(0);
            appIconPane.setBackground(
                new Background(
                    new BackgroundImage(
                        icon,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(0.5, 0.5, true, true, false, false))));
        }
    }

    private void maximizedChanged() {
        if (windowMetrics == null) {
            return;
        }

        isMaximized = ((Stage)getWindow()).isMaximized();
        updateIconColor();
        updateShadowBorder();
    }

    private void check(HRESULT hr, String funcName) {
        if (hr == null || hr.intValue() < 0) {
            throw new RuntimeException(funcName + " failed with error " + (hr != null ? hr.intValue() : "n/a"));
        }
    }

    private void check(Pointer p, String funcName) {
        if (p == null || Pointer.nativeValue(p) == 0) {
            throw new RuntimeException(funcName + " failed with error " + Kernel32.INSTANCE.GetLastError());
        }
    }

    private void check(boolean b, String funcName) {
        if (!b) {
            throw new RuntimeException(funcName + " failed with error " + Kernel32.INSTANCE.GetLastError());
        }
    }

    private static Pointer getWindowPointer(javafx.stage.Window window) {
        com.sun.javafx.tk.TKStage peer = com.sun.javafx.stage.WindowHelper.getPeer(window);
        return Pointer.createConstant(peer.getRawHandle());
    }

    private void mouseMoved(MouseEvent event) {
        if (!isMaximized) {
            setCursor(getWindowRegion(event.getX(), event.getY()).toCursor());
        } else {
            setCursor(Cursor.DEFAULT);
        }
    }

    private void mousePressed(MouseEvent event) {
        if (isMaximized) {
            return;
        }

        if (getWindowRegion(event.getX(), event.getY()) != WindowRegion.NONE) {
            dragContext = new DragContext(event, false);
        } else {
            dragContext = null;
        }
    }

    private void mouseReleased(MouseEvent event) {
        dragContext = null;
    }

    private void mouseDragged(MouseEvent event) {
        if (dragContext != null && dragContext.windowRegion != WindowRegion.TITLE) {
            double dx = event.getScreenX() - dragContext.startDrag.getX();
            double dy = event.getScreenY() - dragContext.startDrag.getY();
            resizeWindow(dx, dy, dragContext.windowRegion, dragContext.initialWindowRect);
        }
    }

    private WindowRegion getWindowRegionAbs(double x, double y) {
        Window window = getWindow();
        return getWindowRegion(
            (x - windowRect.left) / window.getOutputScaleX(), (y - windowRect.top) / window.getOutputScaleY());
    }

    private WindowRegion getWindowRegion(double x, double y) {
        if (x <= currentBorderWidth) {
            if (y < currentBorderWidth) {
                return WindowRegion.NORTH_WEST;
            } else if (y > getWindow().getHeight() - currentBorderWidth) {
                return WindowRegion.SOUTH_WEST;
            } else {
                return WindowRegion.WEST;
            }
        } else if (x >= getWindow().getWidth() - currentBorderWidth) {
            if (y < currentBorderWidth) {
                return WindowRegion.NORTH_EAST;
            } else if (y > getWindow().getHeight() - currentBorderWidth) {
                return WindowRegion.SOUTH_EAST;
            } else {
                return WindowRegion.EAST;
            }
        } else {
            if (y < currentBorderWidth - 1) {
                return WindowRegion.NORTH;
            } else if (y > getWindow().getHeight() - currentBorderWidth) {
                return WindowRegion.SOUTH;
            } else if (y < currentBorderWidth + titleBar.getHeight()) {
                PickResultChooser pickResultChooser = new PickResultChooser();
                NodeHelper.pickNode(
                    titleBar,
                    new PickRay(x - currentBorderWidth, y - currentBorderWidth + 1, 1, 0, Double.POSITIVE_INFINITY),
                    pickResultChooser);
                if (pickResultChooser.getIntersectedNode() == titleBar) {
                    return WindowRegion.TITLE;
                }
            }
        }

        return WindowRegion.NONE;
    }

    private void resizeWindow(double dx, double dy, WindowRegion windowRegion, Rectangle2D initialRect) {
        Window window = getWindow();

        double minWidth = 0;
        double minHeight = 0;
        if (window instanceof Stage) {
            minWidth = ((Stage)window).getMinWidth();
            minHeight = ((Stage)window).getMinHeight();
        }

        if (windowRegion == WindowRegion.NORTH_EAST
                || windowRegion == WindowRegion.EAST
                || windowRegion == WindowRegion.SOUTH_EAST) {
            double newWidth = initialRect.getWidth() + dx;
            if (newWidth >= minWidth) {
                window.setWidth(newWidth);
            }
        } else if (windowRegion == WindowRegion.NORTH_WEST
                || windowRegion == WindowRegion.WEST
                || windowRegion == WindowRegion.SOUTH_WEST) {
            double newWidth = initialRect.getWidth() - dx;
            if (newWidth >= minWidth) {
                window.setX(initialRect.getMinX() + dx);
                window.setWidth(newWidth);
            }
        }

        if (windowRegion == WindowRegion.SOUTH_WEST
                || windowRegion == WindowRegion.SOUTH
                || windowRegion == WindowRegion.SOUTH_EAST) {
            double newHeight = initialRect.getHeight() + dy;
            if (newHeight >= minHeight) {
                window.setHeight(newHeight);
            }
        } else if (windowRegion == WindowRegion.NORTH_WEST
                || windowRegion == WindowRegion.NORTH
                || windowRegion == WindowRegion.NORTH_EAST) {
            double newHeight = initialRect.getHeight() - dy;
            if (newHeight >= minHeight) {
                window.setY(initialRect.getMinY() + dy);
                window.setHeight(newHeight);
            }
        }
    }

}
