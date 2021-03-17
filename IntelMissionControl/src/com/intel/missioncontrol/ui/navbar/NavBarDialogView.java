/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar;

import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.ui.Animations;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import de.saxsys.mvvmfx.ViewModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;

/**
 * Base class for navbar dialogs that can be summoned and dismissed. For navbar dialogs that contain a tab menu, see
 * {@link NavBarMenuView}.
 */
public abstract class NavBarDialogView<TViewModel extends DialogViewModel> extends ViewBase<TViewModel> {

    private enum VisibilityState {
        HIDDEN,
        SUMMONING,
        DISMISSING,
        VISIBLE
    }

    private final BooleanProperty visible = new SimpleBooleanProperty();
    private final List<NavBarDialogView<? extends DialogViewModel>> otherDialogs = new ArrayList<>();

    private VisibilityState visibilityState = VisibilityState.HIDDEN;
    private Background layoutRootBackground;

    public BooleanProperty visibleProperty() {
        return this.visible;
    }

    protected abstract Parent getTransformRoot();

    protected abstract DialogViewModel getViewModel();

    @Override
    protected void initializeView() {
        super.initializeView();
        Expect.notNull(getTransformRoot(), "getTransformRoot()");

        getRootNode().setVisible(false);

        this.visible.addListener(
            ((observable, oldValue, newValue) -> {
                if (newValue) {
                    visibilityState = VisibilityState.SUMMONING;
                } else {
                    visibilityState = VisibilityState.DISMISSING;
                }

                Platform.runLater(this::visibilityChanged);
            }));

        getRootNode()
            .addEventHandler(
                KeyEvent.KEY_RELEASED,
                event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        getViewModel().getCloseCommand().execute();
                    }
                });
    }

    private void visibilityChanged() {
        boolean summoning = visibilityState == VisibilityState.SUMMONING;
        Animations.translateLeftToRight(getTransformRoot(), !summoning);

        boolean fadeLayoutRoot = true;
        for (NavBarDialogView<? extends ViewModel> view : otherDialogs) {
            boolean viewIsAnimating =
                view.visibilityState == VisibilityState.SUMMONING || view.visibilityState == VisibilityState.DISMISSING;

            if (view.hasDarkBackground() && viewIsAnimating) {
                fadeLayoutRoot = false;
                break;
            }
        }

        Pane rootPane = (Pane)getRootNode();
        if (summoning) {
            if (fadeLayoutRoot) {
                Animations.fade(rootPane, false, () -> visibilityState = VisibilityState.VISIBLE);
            } else {
                rootPane.setOpacity(1);
                rootPane.setVisible(true);
                Animations.fade(getTransformRoot(), false, () -> visibilityState = VisibilityState.VISIBLE);
            }
        } else {
            if (fadeLayoutRoot) {
                Animations.fade(rootPane, true, () -> visibilityState = VisibilityState.HIDDEN);
            } else {
                removeDarkBackground();
                Animations.fade(
                    getTransformRoot(),
                    true,
                    () -> {
                        rootPane.setVisible(false);
                        addDarkBackground();
                        getTransformRoot().setOpacity(1);
                        getTransformRoot().setVisible(true);
                        visibilityState = VisibilityState.HIDDEN;
                    });
            }
        }
    }

    private boolean hasDarkBackground() {
        return layoutRootBackground != null || ((Pane)getRootNode()).getBackground() != null;
    }

    private void removeDarkBackground() {
        Pane rootPane = (Pane)getRootNode();
        layoutRootBackground = rootPane.getBackground();
        rootPane.setBackground(null);
    }

    private void addDarkBackground() {
        ((Pane)getRootNode()).setBackground(layoutRootBackground);
    }

    private void setOtherDialogs(List<NavBarDialogView<? extends DialogViewModel>> otherDialogs) {
        this.otherDialogs.clear();
        this.otherDialogs.addAll(otherDialogs);
    }

    @SafeVarargs
    public static void group(NavBarDialogView<? extends DialogViewModel>... views) {
        List<NavBarDialogView<? extends DialogViewModel>> viewList = Arrays.asList(views);
        for (NavBarDialogView<? extends DialogViewModel> view : views) {
            view.setOtherDialogs(viewList.stream().filter(item -> item != view).collect(Collectors.toList()));
        }
    }

}
