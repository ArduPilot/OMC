/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.notifications;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.Button;
import com.intel.missioncontrol.ui.controls.CheckBox;
import com.intel.missioncontrol.ui.controls.Hyperlink;
import de.saxsys.mvvmfx.InjectViewModel;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class ToastView extends ViewBase<ToastViewModel> {

    private static Map<ToastType, String> backgroundStyles;
    private static Map<ToastType, String> iconImages;

    static {
        backgroundStyles = new HashMap<>();
        backgroundStyles.put(ToastType.INFO, "-fx-background-color: theme-toast-info-background-color");
        backgroundStyles.put(ToastType.ALERT, "-fx-background-color: theme-toast-alert-background-color");

        iconImages = new HashMap<>();
        iconImages.put(ToastType.INFO, "/com/intel/missioncontrol/icons/icon_warning-notice(fill=theme-white).svg");
        iconImages.put(ToastType.ALERT, "/com/intel/missioncontrol/icons/icon_warning(fill=theme-white).svg");
    }

    @InjectViewModel
    private ToastViewModel viewModel;

    @FXML
    private Region layoutRoot;

    @FXML
    private Pane imageContainer;

    @FXML
    private ImageView imageView;

    @FXML
    private Label textLabel;

    @FXML
    private Hyperlink actionLink;

    @FXML
    private CheckBox actionCheckBox;

    @FXML
    private Button closeButton;

    private ChangeListener<ToastViewModel.Status> statusListener =
        (observable, oldValue, newValue) -> {
            if (newValue == ToastViewModel.Status.APPEARING) {
                playAppearAnimation();
            } else if (newValue == ToastViewModel.Status.DISAPPEARING) {
                playDisappearAnimation();
            }
        };

    @Override
    public void initializeView() {
        super.initializeView();

        viewModel.statusProperty().addListener(statusListener);
        layoutRoot.setStyle(layoutRoot.getStyle() + ";" + backgroundStyles.get(viewModel.getToastType()));
        layoutRoot.visibleProperty().bind(viewModel.statusProperty().isNotEqualTo(ToastViewModel.Status.HIDDEN));
        textLabel.textProperty().bind(viewModel.textProperty());

        actionLink.textProperty().bind(viewModel.actionTextProperty());
        actionLink
            .visibleProperty()
            .bind(viewModel.actionTextProperty().isNotEmpty().and(viewModel.isCheckableActionProperty().not()));
        actionLink.managedProperty().bind(actionLink.visibleProperty());
        actionLink.setCommand(viewModel.getActionCommand());

        actionCheckBox.textProperty().bind(viewModel.actionTextProperty());
        actionCheckBox
            .visibleProperty()
            .bind(viewModel.actionTextProperty().isNotEmpty().and(viewModel.isCheckableActionProperty()));
        actionCheckBox.managedProperty().bind(actionCheckBox.visibleProperty());
        actionCheckBox.setCommand(viewModel.getActionCommand());

        closeButton.visibleProperty().bind(viewModel.isCloseableProperty());
        closeButton.setCommand(viewModel.getCloseCommand());

        imageContainer.visibleProperty().bind(viewModel.showIconProperty());
        imageContainer.managedProperty().bind(viewModel.showIconProperty());

        if (viewModel.showIconProperty().get()) {
            imageView.setImage(new Image(iconImages.get(viewModel.getToastType())));
        }

        playAppearAnimation();
    }

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    public ToastViewModel getViewModel() {
        return viewModel;
    }

    private void playAppearAnimation() {
        FadeTransition fade = new FadeTransition();
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setNode(layoutRoot);
        fade.setDuration(ToastStackView.APPEAR_DURATION);
        fade.setInterpolator(Interpolator.LINEAR);
        fade.setOnFinished(event -> viewModel.notifyStatusTransitionFinished());
        fade.play();
    }

    private void playDisappearAnimation() {
        FadeTransition fade = new FadeTransition();
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setNode(layoutRoot);
        fade.setDuration(ToastStackView.DISAPPEAR_DURATION);
        fade.setInterpolator(Interpolator.LINEAR);
        fade.setOnFinished(event -> viewModel.notifyStatusTransitionFinished());
        fade.play();

        TranslateTransition translate = new TranslateTransition();
        translate.setNode(layoutRoot);
        translate.setFromY(layoutRoot.getTranslateY());
        translate.setToY(layoutRoot.getTranslateY() + layoutRoot.getHeight());
        translate.setDuration(ToastStackView.DISAPPEAR_DURATION);
        translate.setInterpolator(Interpolator.EASE_BOTH);
        translate.play();
    }

}
