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

    private static final String ICON_COMPLETE = "/com/intel/missioncontrol/icons/icon_complete(fill=theme-green).svg";
    // TODO colors
    private static final String ICON_WARNING = "/com/intel/missioncontrol/icons/icon_warning(fill=black).svg";
    private static final String ICON_ERROR =
            "/com/intel/missioncontrol/icons/icon_warning(fill=theme-warning-color).svg";
    private static final String ICON_LOADING = "/com/intel/missioncontrol/icons/icon_progress.svg";

    @InjectViewModel
    private AutoCheckItemViewModel viewModel;

    private Image completeIcon;
    private Image warningIcon;
    private Image errorIcon;
    private Image loadingIcon;

    private Image getCompleteIcon() {
        if (completeIcon == null) {
            completeIcon = new Image(ICON_COMPLETE);
        }

        return completeIcon;
    }

    private Image getWarningIcon() {
        if (warningIcon == null) {
            warningIcon = new Image(ICON_WARNING);
        }

        return warningIcon;
    }

    private Image getErrorIcon() {
        if (errorIcon == null) {
            errorIcon = new Image(ICON_ERROR);
        }

        return errorIcon;
    }

    private Image getLoadingIcon() {
        if (loadingIcon == null) {
            loadingIcon = new Image(ICON_LOADING);
        }

        return loadingIcon;
    }

    public void initialize() {
        setSpacing(5);
        setAlignment(Pos.BASELINE_LEFT);
        setPadding(new Insets(5, 5, 5, 5));
        RotatingImageView rotatingImageView = new RotatingImageView();
        rotatingImageView.setFitWidth(16);
        rotatingImageView.setFitHeight(16);

        rotatingImageView
            .imageProperty()
            .bind(
                Bindings.createObjectBinding(
                    () -> {
                        AlertType alertType = viewModel.alertTypeProperty().getValue();
                        if (alertType == null) {
                            alertType = AlertType.LOADING;
                        }

                        switch (alertType) {
                        case COMPLETED:
                            return getCompleteIcon();
                        case WARNING:
                            return getWarningIcon();
                        case ERROR:
                            return getErrorIcon();
                        case LOADING:
                        default:
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

        getChildren().add(rotatingImageView);

        Label label = new Label();
        label.setWrapText(true);
        label.textProperty().bind(viewModel.messageStringProperty());
        // label.disableProperty().bind(viewModel.isInProgressProperty());
        getChildren().add(label);

        Pane pane = new Pane();
        HBox.setHgrow(pane, Priority.ALWAYS);
        getChildren().add(pane);

        // TODO
        //            if (!viewModel.isValidProperty().get() && viewModel.getFirstResolveActionCommand() != null) {
        //                Hyperlink hyperlink =
        //                    new Hyperlink(
        //                        languageHelper.getString(
        //
        // "com.intel.missioncontrol.ui.sidepane.flight.fly.checks.automatic.AutomaticChecksDialogView.howToFix"));
        //                hyperlink.textProperty().bind(viewModel.firstResolveActionTextProperty());
        //                hyperlink.setAlignment(Pos.CENTER);
        //                hyperlink.setPadding(new Insets(1, 5, 1, 1));
        //                hyperlink.setPrefWidth(70);
        //                hyperlink.setOnAction(event -> viewModel.getFirstResolveActionCommand().execute());
        //                label.disableProperty().bind(viewModel.isInProgressProperty());
        //
        //                getChildren().add(hyperlink);
        //
        // hyperlink.visibleProperty().bind(viewModel.numberOfResolveActionsProperty().greaterThan(0));
        //            }
    }

}
