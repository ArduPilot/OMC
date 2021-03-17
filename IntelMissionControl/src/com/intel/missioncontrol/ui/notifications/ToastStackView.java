/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.notifications;

import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.FluentViewLoader;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewTuple;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class ToastStackView extends ViewBase<ToastStackViewModel> {

    private static final double TOAST_SPACING = 16;

    static final Duration APPEAR_DURATION = Duration.millis(200);
    static final Duration DISAPPEAR_DURATION = Duration.millis(400);

    @InjectViewModel
    private ToastStackViewModel viewModel;

    @FXML
    private Pane itemsPane;

    private List<ViewTuple<ToastView, ToastViewModel>> toastTuples = new ArrayList<>();

    @Override
    public void initializeView() {
        super.initializeView();
        itemsPane.setMaxWidth(Pane.USE_PREF_SIZE);
        itemsPane.setMaxHeight(Pane.USE_PREF_SIZE);
        itemsPane.widthProperty().addListener(((obs, oldValue, newValue) -> widthChanged(newValue.doubleValue())));
        viewModel.toastsProperty().addListener(((obs, oldValue, newValue) -> toastsChanged(newValue)));
    }

    @Override
    protected Parent getRootNode() {
        return itemsPane;
    }

    @Override
    public ToastStackViewModel getViewModel() {
        return viewModel;
    }

    private void widthChanged(double width) {
        for (Node node : itemsPane.getChildren()) {
            Region region = (Region)node;
            region.setTranslateX((width - region.getWidth()) / 2);
        }
    }

    private void toastsChanged(ObservableList<ToastViewModel> viewModels) {
        for (ToastViewModel newToast : viewModels) {
            boolean found = false;
            for (ViewTuple<ToastView, ToastViewModel> viewTuple : toastTuples) {
                if (viewTuple.getViewModel() == newToast) {
                    found = true;
                    break;
                }
            }

            if (found) {
                continue;
            }

            ViewTuple<ToastView, ToastViewModel> newViewTuple =
                FluentViewLoader.fxmlView(ToastView.class).viewModel(newToast).load();
            toastTuples.add(newViewTuple);
            itemsPane.getChildren().add(0, newViewTuple.getView());
            final ToastViewModel viewModel = newViewTuple.getViewModel();
            newViewTuple
                .getViewModel()
                .statusProperty()
                .addListener(((observable, oldValue, newValue) -> toastStatusChanged(viewModel, newValue)));
        }

        itemsPane.applyCss();
        itemsPane.layout();
        arrangeToasts();
    }

    private void toastStatusChanged(ToastViewModel viewModel, ToastViewModel.Status status) {
        if (status == ToastViewModel.Status.HIDDEN) {
            ViewTuple<ToastView, ToastViewModel> viewTuple =
                toastTuples
                    .stream()
                    .filter(tuple -> tuple.getViewModel() == viewModel)
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);

            toastTuples.remove(viewTuple);
            itemsPane.getChildren().remove(viewTuple.getView());
        }
    }

    private void arrangeToasts() {
        double totalHeight = 0;
        int visibleToasts = 0;
        for (ViewTuple<ToastView, ToastViewModel> toastTuple : toastTuples) {
            if (isExcludedFromArrange(toastTuple)) {
                continue;
            }

            Region region = (Region)toastTuple.getView();
            totalHeight += region.getHeight();
            ++visibleToasts;
        }

        totalHeight += (visibleToasts - 1) * TOAST_SPACING;

        for (int i = toastTuples.size() - 1; i >= 0; --i) {
            ViewTuple<ToastView, ToastViewModel> toastTuple = toastTuples.get(i);
            if (isExcludedFromArrange(toastTuple)) {
                continue;
            }

            Region region = (Region)toastTuple.getView();
            if (isNotYetArranged(toastTuple)) {
                region.setTranslateY(itemsPane.getHeight());
                toastTuple.getViewModel().notifyArranged();
            }

            animateToY(region, itemsPane.getHeight() - totalHeight);
            totalHeight -= region.getHeight() + TOAST_SPACING;

            region.setTranslateX((itemsPane.getWidth() - region.getWidth()) / 2);
        }
    }

    private boolean isExcludedFromArrange(ViewTuple<ToastView, ToastViewModel> toastTuple) {
        ToastViewModel.Status status = toastTuple.getViewModel().statusProperty().get();
        return status == ToastViewModel.Status.DISAPPEARING || status == ToastViewModel.Status.HIDDEN;
    }

    private boolean isNotYetArranged(ViewTuple<ToastView, ToastViewModel> toastTuple) {
        ToastViewModel.Status status = toastTuple.getViewModel().statusProperty().get();
        return status == ToastViewModel.Status.NOT_YET_ARRANGED;
    }

    private void animateToY(Region region, double y) {
        if (region.getTranslateY() >= y) {
            region.setTranslateY(y);
        } else {
            TranslateTransition translate = new TranslateTransition();
            translate.setNode(region);
            translate.setFromY(region.getTranslateY());
            translate.setToY(y);
            translate.setDuration(DISAPPEAR_DURATION);
            translate.setInterpolator(Interpolator.EASE_BOTH);
            translate.play();
        }
    }

}
