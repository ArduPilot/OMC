/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.checks.automatic;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.controls.RotatingImageView;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

// Class needs to be public, otherwise it can't be properly initialized.
public class AutoCheckItemView extends HBox implements JavaView<AutoCheckItemViewModel> {


    private static final String ICON_LOADING = "/com/intel/missioncontrol/icons/icon_progress.svg";
    private static final String ICON_COMPLETE_CLASS = "icon-complete";
    private static final String ICON_WARNING_CLASS = "icon-warning";
    private static final String WARNING_CLASS = "warning";
    private static final String CRITICAL_CLASS = "critical";

    @InjectViewModel
    private AutoCheckItemViewModel viewModel;

    private Image loadingIcon;


    private Image getLoadingIcon() {
        if (loadingIcon == null) {
            loadingIcon = new Image(ICON_LOADING);
        }
        return loadingIcon;
    }

    public void initialize() {
        getStyleClass().addAll("form-row");

        HBox imgBox = new HBox();
        imgBox.getStyleClass().add("set-back");
        getChildren().add(imgBox);

        RotatingImageView rotatingImageView = new RotatingImageView();
        rotatingImageView.setFitWidth(20);
        rotatingImageView.setFitHeight(20);
        rotatingImageView.setVisible(false);
        rotatingImageView.setManaged(false);

        rotatingImageView
            .imageProperty()
            .bind(
                Bindings.createObjectBinding(
                    () -> {
                        AlertType alertType = viewModel.alertTypeProperty().getValue();
                        if (alertType == null) {
                            alertType = AlertType.LOADING;
                        }

                        getStyleClass().removeAll(CRITICAL_CLASS, WARNING_CLASS, ICON_COMPLETE_CLASS, ICON_WARNING_CLASS);
                        rotatingImageView.setVisible(false);
                        rotatingImageView.setManaged(false);

                        switch (alertType) {
                        case COMPLETED:
                            getStyleClass().add(ICON_COMPLETE_CLASS);
                            return null;
                        case WARNING:
                            getStyleClass().addAll(WARNING_CLASS, ICON_WARNING_CLASS);
                            return null;
                        case ERROR:
                            getStyleClass().addAll(ICON_WARNING_CLASS, CRITICAL_CLASS);
                            return null;
                        case LOADING:
                        case NONE:
                        default:
                            rotatingImageView.setVisible(true);
                            rotatingImageView.setManaged(true);
                            return getLoadingIcon();
                        }
                    },
                    viewModel.alertTypeProperty()));

        rotatingImageView
            .isRotatingProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () -> {
                        AlertType alertType = viewModel.alertTypeProperty().getValue();
                        return (alertType == null || alertType == AlertType.LOADING);
                    },
                    viewModel.alertTypeProperty()));

        imgBox.getChildren().add(rotatingImageView);

        Label label = new Label();
        label.setWrapText(true);
        label.textProperty().bind(viewModel.messageStringProperty());
        getChildren().add(label);

    }

}
