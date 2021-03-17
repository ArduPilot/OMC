/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.checks;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.Button;
import com.intel.missioncontrol.ui.controls.RotatingImageView;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javax.inject.Inject;

public class SafetyChecksView extends ViewBase<SafetyChecksViewModel> {

    private static final String ICON_COMPLETE = "/com/intel/missioncontrol/icons/icon_complete(fill=theme-green).svg";
    // TODO: colors
    private static final String ICON_WARNING = "/com/intel/missioncontrol/icons/icon_warning(fill=black).svg";
    private static final String ICON_ERROR =
        "/com/intel/missioncontrol/icons/icon_warning(fill=theme-warning-color).svg";
    private static final String ICON_LOADING = "/com/intel/missioncontrol/icons/icon_progress.svg";

    @InjectViewModel
    private SafetyChecksViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    private Pane layoutRoot;

    @FXML
    private RotatingImageView autoCheckImageStatus;

    @FXML
    private Label autoCheckStatus;

    @FXML
    private ImageView checklistImageStatus;

    @FXML
    private Label checklistStatus;

    @FXML
    private ImageView takeoffPositionImageStatus;

    @FXML
    private Label takeoffPositionStatus;

    @FXML
    private Button updateTakeoffPositionButton;

    private Image completeIcon;
    private Image warningIcon;
    private Image errorIcon;
    private Image loadingIcon;

    private IDialogContextProvider dialogContextProvider;

    @Inject
    public SafetyChecksView(IDialogContextProvider dialogContextProvider) {
        this.dialogContextProvider = dialogContextProvider;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        dialogContextProvider.setContext(viewModel, context);

        // auto check
        autoCheckImageStatus
            .imageProperty()
            .bind(
                Bindings.createObjectBinding(
                    () -> {
                        switch (viewModel.autoCheckAlertTypeProperty().get()) {
                        case WARNING:
                            return getWarningIcon();
                        case ERROR:
                            return getErrorIcon();
                        case LOADING:
                            return getLoadingIcon();
                        case COMPLETED:
                            return getCompleteIcon();
                        case NONE:
                        default:
                            return null;
                        }
                    },
                    viewModel.autoCheckAlertTypeProperty()));

        autoCheckImageStatus
            .isRotatingProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () -> {
                        AlertType alertType = viewModel.autoCheckAlertTypeProperty().getValue();
                        return (alertType == null || alertType == AlertType.LOADING);
                    },
                    viewModel.autoCheckAlertTypeProperty()));

        autoCheckStatus.textProperty().bind(viewModel.autoCheckStatusStringProperty());

        // manual check
        checklistImageStatus
            .imageProperty()
            .bind(
                Bindings.createObjectBinding(
                    () -> {
                        switch (viewModel.manualCheckImageTypeProperty().get()) {
                        case WARNING:
                            return getWarningIcon();
                        case LOADING:
                            return getLoadingIcon();
                        case COMPLETED:
                            return getCompleteIcon();
                        case NONE:
                        default:
                            return null;
                        }
                    },
                    viewModel.manualCheckImageTypeProperty()));
        checklistStatus.textProperty().bind(viewModel.manualCheckStatusProperty());

        updateTakeoffPositionButton
            .disableProperty()
            .bind(viewModel.getUpdateTakeoffPositionCommand().notExecutableProperty());
        // TODO highlighting
        takeoffPositionStatus.textProperty().bind(viewModel.takeoffPositionStatusProperty());
        checklistStatus.textProperty().bind(viewModel.manualCheckStatusProperty());
        takeoffPositionImageStatus
            .imageProperty()
            .bind(
                Bindings.createObjectBinding(
                    () -> {
                        viewModel.getUpdateTakeoffPositionImageStatusString.execute();
                        switch (viewModel.takeoffPositionImageTypeProperty().get()) {
                        case WARNING:
                            return getWarningIcon();
                        case LOADING:
                            return getLoadingIcon();
                        case COMPLETED:
                            return getCompleteIcon();
                        case NONE:
                        default:
                            return null;
                        }
                    },
                    viewModel.takeoffPositionImageTypeProperty(),
                    viewModel.getUpdateTakeoffPositionCommand().notExecutableProperty(),
                    viewModel.flightSegmentProperty()));
    }

    public void showViewLogDialog(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getShowAutomaticChecksDialogCommand().execute();
    }

    public void showPopupChecklist(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getShowFlightCheckListChecksDialogCommand().execute();
    }

    private Image getCompleteIcon() {
        if (completeIcon == null) {
            try {
                completeIcon = new Image(ICON_COMPLETE);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    public void updateTakeoffPosition(ActionEvent actionEvent) {
        viewModel.getUpdateTakeoffPositionCommand().execute();
    }

}
