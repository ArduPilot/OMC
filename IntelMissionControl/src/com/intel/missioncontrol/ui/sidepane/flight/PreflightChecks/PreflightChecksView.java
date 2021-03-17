/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.PreflightChecks;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import javax.inject.Inject;

public class PreflightChecksView extends ViewBase<PreflightChecksViewModel> {

    private static final String ICON_COMPLETE = "/com/intel/missioncontrol/icons/icon_complete(fill=theme-green).svg";
    private static final String ICON_ALERT = "/com/intel/missioncontrol/icons/icon_warning(fill=theme-warning-color).svg";
    private static final String ICON_LOADING = "/com/intel/missioncontrol/icons/icon_progress.svg";
    private static final int LONG_PRESSED_TIMER = 1000; // millisecond

    @InjectViewModel
    private PreflightChecksViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    private Pane layoutRoot;

    @FXML
    private ImageView autoCheckImageStatus;

    @FXML
    private Label autoCheckStatus;

    @FXML
    private ImageView checklistImageStatus;

    @FXML
    private Label checklistStatus;

    @FXML
    private ImageView motorsAlertIcon;

    @FXML
    private Label motorLabel;

    @FXML
    private Button motorButton;

    @FXML
    private ProgressBar progressbar;

    private Image completeIcon;
    private Image alertIcon;
    private Image loadingIcon;
    private Timeline timeline = new Timeline();
    private RotateTransition rotation;

    private ILanguageHelper languageHelper;
    private IDialogContextProvider dialogContextProvider;

    @Inject
    public PreflightChecksView(ILanguageHelper languageHelper, IDialogContextProvider dialogContextProvider) {
        this.languageHelper = languageHelper;
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
                        switch (viewModel.autoCheckImageTypeProperty().get()) {
                        case ALERT:
                            return getAlertIcon();
                        case LOADING:
                            return getLoadingIcon();
                        case COMPLETED:
                            return getCompleteIcon();
                        case NONE:
                        default:
                            return null;
                        }
                    },
                    viewModel.autoCheckImageTypeProperty()));

        autoCheckStatus.textProperty().bind(viewModel.autoCheckStatusProperty());

        // manual check
        checklistImageStatus
            .imageProperty()
            .bind(
                Bindings.createObjectBinding(
                    () -> {
                        switch (viewModel.manualCheckImageTypeProperty().get()) {
                        case ALERT:
                            return getAlertIcon();
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

        // motors
        motorLabel.textProperty().bind(viewModel.motorsOnMessageProperty());
        //initMotorUI();
    }

    public void showViewLogDialog(ActionEvent actionEvent) {
        viewModel.getShowAutomaticChecksDialogCommand().execute();
    }

    public void showPopupChecklist(ActionEvent actionEvent) {
        viewModel.getShowFlightCheckListChecksDialogCommand().execute();
    }

    public void showEmergencyEdit(ActionEvent actionEvent) {
        viewModel.getEmergencyEditDialogCommand().execute();
    }

    private void initMotorUI() {
        motorsAlertIcon.setImage(getCompleteIcon());
        // TODO: bind with viewmodel when backend code is ready
        // motorLabel.textProperty().bind(viewModel.getMotorsOnMessageProperty());
        motorLabel.setText(
            languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.motorsNotRunning"));

        progressbar.getStyleClass().clear();
        progressbar.getStyleClass().setAll("holding-progress");
        progressbar.setProgress(0);
        motorButton.setText(
            languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.startMotors"));

        motorButton.addEventHandler(
            MouseEvent.ANY,
            new EventHandler<>() {
                long starttime = 0;

                @Override
                public void handle(MouseEvent event) {
                    if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
                        starttime = System.currentTimeMillis();
                        onMotorButtonPressed();
                    } else if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
                        if (System.currentTimeMillis() - starttime > LONG_PRESSED_TIMER) {
                            timeline.stop();
                            timeline.getKeyFrames().clear();
                            motorsAlertIcon.setImage(getLoadingIcon());
                            rotation.play();
                            motorButton.setText(
                                languageHelper.getString(
                                    "com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.stopMotors"));
                            motorButton.getStyleClass().clear();
                            motorButton.getStyleClass().addAll("preflight-button");
                            motorLabel.setText(
                                languageHelper.getString(
                                    "com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.motorsRunning"));
                            // viewModel.areMotorsOnProperty().set(true);
                            viewModel.getTurnOnMotorsCommand().execute();
                            // viewModel.currentIdlWorkflowStateProperty().set(IdlWorkflowState.MOTORS_ON);
                        } else {
                            double currenttime = timeline.getCurrentTime().toMillis();
                            timeline.stop();
                            timeline.getKeyFrames().clear();
                            animateProgressBack(currenttime, progressbar.getProgress());
                        }
                    }
                }
            });

        rotation = new RotateTransition(Duration.millis(LONG_PRESSED_TIMER), motorsAlertIcon);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        rotation.setCycleCount(Animation.INDEFINITE);

        rotation.statusProperty()
            .addListener(
                (obs, oldValue, newValue) -> {
                    if (newValue == Animation.Status.STOPPED) {
                        RotateTransition transition = new RotateTransition(Duration.millis(10), motorsAlertIcon);
                        transition.setFromAngle(motorsAlertIcon.getRotate());
                        transition.setToAngle(0);
                        transition.setCycleCount(1);
                        transition.setAutoReverse(true);
                        transition.play();
                    }
                });
    }

    private void onMotorButtonPressed() {
        double progress = progressbar.getProgress();
        if (motorButton
                .getText()
                .equals(
                    languageHelper.getString(
                        "com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.stopMotors"))) {
            progressbar.setProgress(0);
            timeline.getKeyFrames().clear();
            motorButton.setText(
                languageHelper.getString("com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.startMotors"));
            motorButton.getStyleClass().clear();
            motorButton.getStyleClass().add("longpress-button");
            rotation.stop();
            motorsAlertIcon.setImage(getCompleteIcon());
            motorLabel.setText(
                languageHelper.getString(
                    "com.intel.missioncontrol.ui.sidepane.flight.preflightChecks.motorsNotRunning"));
            // viewModel.areMotorsOnProperty().set(false);
            viewModel.getTurnOffMotorsCommand();
            // viewModel.currentIdlWorkflowStateProperty().set(IdlWorkflowState.MOTORS_OFF);
        } else if (progress == 0) {
            animateProgress();
        }
    }

    private void animateProgress() {
        KeyValue keyValue;
        KeyFrame keyFrame;

        keyValue = new KeyValue(progressbar.progressProperty(), 1);
        keyFrame = new KeyFrame(new Duration(LONG_PRESSED_TIMER), keyValue);

        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    private void animateProgressBack(double currenttime, double progress) {
        KeyValue keyValue;
        KeyFrame keyFrame;

        if (progress > 0 && progress <= 1) {
            keyValue = new KeyValue(progressbar.progressProperty(), 0);

            if (currenttime > 0) {
                keyFrame = new KeyFrame(new Duration(currenttime), keyValue);

                timeline.getKeyFrames().add(keyFrame);
                timeline.play();
            }
        }
    }

    private Image getCompleteIcon() {
        if (completeIcon == null) {
            try {
                completeIcon = new Image(ICON_COMPLETE);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        return completeIcon;
    }

    private Image getAlertIcon() {
        if (alertIcon == null) {
            alertIcon = new Image(ICON_ALERT);
        }

        return alertIcon;
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

}
