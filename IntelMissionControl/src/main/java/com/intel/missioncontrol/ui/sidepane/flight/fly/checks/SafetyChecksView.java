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

    private static final String ICON_LOADING = "/com/intel/missioncontrol/icons/icon_progress.svg";
    private static final String ICON_COMPLETE_CLASS = "icon-complete";
    private static final String ICON_WARNING_CLASS = "icon-warning";
    private static final String WARNING_CLASS = "warning";
    private static final String CRITICAL_CLASS = "critical";

    @InjectViewModel
    private SafetyChecksViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    private Pane layoutRoot;

    @FXML
    private Pane autoCheckBox;

    @FXML
    private RotatingImageView autoCheckImageStatus;

    @FXML
    private Label autoCheckStatus;

    @FXML
    private Pane checkListBox;

    @FXML
    private ImageView checklistImageStatus;

    @FXML
    private Label checklistStatus;

    @FXML
    private Pane takeOffPosBox;

    @FXML
    private ImageView takeoffPositionImageStatus;

    @FXML
    private Label takeoffPositionStatus;

    @FXML
    private Button updateTakeoffPositionButton;

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
                        autoCheckBox.getStyleClass().removeAll(CRITICAL_CLASS, WARNING_CLASS, ICON_COMPLETE_CLASS);
                        autoCheckImageStatus.setVisible(false);
                        autoCheckImageStatus.setManaged(false);

                        switch (viewModel.autoCheckAlertTypeProperty().get()) {
                        case WARNING:
                            autoCheckBox.getStyleClass().addAll(ICON_WARNING_CLASS, WARNING_CLASS);
                            return null;
                        case ERROR:
                            autoCheckBox.getStyleClass().addAll(ICON_WARNING_CLASS, CRITICAL_CLASS);
                            return null;
                        case LOADING:
                            autoCheckImageStatus.setVisible(true);
                            autoCheckImageStatus.setManaged(true);
                            return getLoadingIcon();
                        case COMPLETED:
                            autoCheckBox.getStyleClass().add(ICON_COMPLETE_CLASS);
                            return null;
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
                        checkListBox.getStyleClass().removeAll(CRITICAL_CLASS, WARNING_CLASS, ICON_WARNING_CLASS, ICON_COMPLETE_CLASS);
                        checklistImageStatus.setVisible(false);
                        checklistImageStatus.setManaged(false);

                        switch (viewModel.manualCheckImageTypeProperty().get()) {
                        case WARNING:
                            checkListBox.getStyleClass().addAll(WARNING_CLASS, ICON_WARNING_CLASS);
                            return null;
                        case LOADING:
                            checklistImageStatus.setVisible(true);
                            checklistImageStatus.setManaged(true);
                            return getLoadingIcon();
                        case COMPLETED:
                            checkListBox.getStyleClass().add(ICON_COMPLETE_CLASS);
                            return null;
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
                        takeOffPosBox.getStyleClass().removeAll(CRITICAL_CLASS, WARNING_CLASS, ICON_WARNING_CLASS, ICON_COMPLETE_CLASS);
                        takeoffPositionImageStatus.setVisible(false);
                        takeoffPositionImageStatus.setManaged(false);

                        switch (viewModel.takeoffPositionImageTypeProperty().get()) {
                        case WARNING:
                            takeOffPosBox.getStyleClass().addAll(WARNING_CLASS, ICON_WARNING_CLASS);
                            return null;
                        case LOADING:
                            takeoffPositionImageStatus.setVisible(true);
                            takeoffPositionImageStatus.setManaged(true);
                            return getLoadingIcon();
                        case COMPLETED:
                            takeOffPosBox.getStyleClass().add(ICON_COMPLETE_CLASS);
                            return null;
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
