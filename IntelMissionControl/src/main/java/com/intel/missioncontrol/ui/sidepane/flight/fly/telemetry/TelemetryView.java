/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import static com.intel.missioncontrol.drone.FlightSegment.UNKNOWN;

import com.intel.missioncontrol.drone.AutopilotState;
import com.intel.missioncontrol.drone.BatteryAlertLevel;
import com.intel.missioncontrol.drone.DistanceSensor;
import com.intel.missioncontrol.drone.FlightSegment;
import com.intel.missioncontrol.drone.GnssState;
import com.intel.missioncontrol.drone.IObstacleAvoidance;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.common.CssStyleRotation;
import com.intel.missioncontrol.ui.controls.Button;
import com.intel.missioncontrol.ui.dialogs.IDialogContextProvider;
import de.saxsys.mvvmfx.Context;
import de.saxsys.mvvmfx.InjectContext;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javax.inject.Inject;
import org.asyncfx.concurrent.Dispatcher;

public class TelemetryView extends ViewBase<TelemetryViewModel> {

    private final EnumConverter<FlightSegment> flightSegmentStringConverter;
    private final EnumConverter<GnssState> gnssStateStringConverter;
    private final EnumConverter<AutopilotState> autopilotStateStringConverter;
    private final ILanguageHelper languageHelper;
    private IDialogContextProvider dialogContextProvider;
    private BooleanProperty showTelemetryDetailDialogPossible = new SimpleBooleanProperty();

    @InjectViewModel
    private TelemetryViewModel viewModel;

    @FXML
    private Button batteryButton;

    @FXML
    private Button gnssButton;

    @FXML
    private Button flightSegmentButton;

    @FXML
    private Button altitudeAGLButton;

    @FXML
    private Button distanceToDroneButton;

    @FXML
    private Button autoPilotButton;

    @FXML
    private Button timeUntilLandingButton;

    @FXML
    private Button flightTimeButton;

    @FXML
    private Button obstacleAvoidanceButton;

    @InjectContext
    private Context context;

    @FXML
    private GridPane rootNode;

    @FXML
    private Label altitudeAgl;

    @FXML
    private Label distanceToDrone;

    @FXML
    private Label timeUntilLanding;

    @FXML
    private Label flightTime;

    @Inject
    public TelemetryView(ILanguageHelper languageHelper, IDialogContextProvider dialogContextProvider) {
        this.dialogContextProvider = dialogContextProvider;
        this.flightSegmentStringConverter = new EnumConverter<>(languageHelper, FlightSegment.class);
        this.gnssStateStringConverter = new EnumConverter<>(languageHelper, GnssState.class);
        this.autopilotStateStringConverter = new EnumConverter<>(languageHelper, AutopilotState.class);
        this.languageHelper = languageHelper;
    }

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    public TelemetryViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();
        dialogContextProvider.setContext(viewModel, context);

        // battery
        batteryButton.textProperty().bind(viewModel.batteryTextProperty());
        viewModel.alertBatteryLevelProperty().addListener((observable, oldValue, newValue) -> updateBatteryStyle());
        viewModel.batteryTelemetryOldProperty().addListener((observable, oldValue, newValue) -> updateBatteryStyle());
        viewModel
            .batteryRemainingChargeProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    int remainingChargePercent =
                        newValue != null ? newValue.convertTo(Unit.PERCENTAGE).getValue().intValue() : 0;
                    batteryButton
                        .getStyleClass()
                        .removeAll(
                            "icon-battery-4",
                            "icon-battery-3",
                            "icon-battery-2",
                            "icon-battery-1",
                            "icon-battery-empty");
                    if (remainingChargePercent >= 80) {
                        CssStyleRotation.stop(batteryButton);
                        batteryButton.getStyleClass().addAll("icon-battery-4");
                    } else if (remainingChargePercent >= 60) {
                        CssStyleRotation.stop(batteryButton);
                        batteryButton.getStyleClass().addAll("icon-battery-3");
                    } else if (remainingChargePercent >= 40) {
                        CssStyleRotation.stop(batteryButton);
                        batteryButton.getStyleClass().addAll("icon-battery-2");
                    } else if (remainingChargePercent >= 20) {
                        CssStyleRotation.stop(batteryButton);
                        batteryButton.getStyleClass().addAll("icon-battery-1");
                    } else {
                        CssStyleRotation.setCritical(batteryButton);
                        batteryButton.getStyleClass().addAll("icon-battery-empty");
                    }
                });

        // gnss
        gnssButton
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () -> {
                        var gnssState = viewModel.gnssStateProperty().getValue();
                        if (gnssState == null) {
                            gnssState = GnssState.UNKNOWN;
                        }

                        return gnssStateStringConverter.toString(gnssState);
                    },
                    viewModel.gnssStateProperty()));
        viewModel.gnssQualityProperty().addListener((observable, oldValue, newValue) -> updateGnssStyle());
        viewModel.gnssTelemetryOldProperty().addListener((observable, oldValue, newValue) -> updateGnssStyle());

        gnssButton.disableProperty().bind(showTelemetryDetailDialogPossible.not());
        showTelemetryDetailDialogPossibleProperty().unbind();
        showTelemetryDetailDialogPossibleProperty().bind(viewModel.telemetryDetailsAvailableProperty());

        // flight segment
        flightSegmentButton
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () -> {
                        FlightSegment flightSegment = viewModel.flightSegmentProperty().getValue();
                        if (flightSegment == null) {
                            flightSegment = FlightSegment.UNKNOWN;
                        }

                        return flightSegmentStringConverter.toString(flightSegment);
                    },
                    viewModel.flightSegmentProperty()));
        viewModel.flightSegmentProperty().addListener((observable, oldValue, newValue) -> updateFlightSegmentStyle());
        viewModel
            .flightSegmentTelemetryOldProperty()
            .addListener((observable, oldValue, newValue) -> updateFlightSegmentStyle());

        // altitude
        altitudeAgl.textProperty().bind(viewModel.altitudeAglTextProperty());
        viewModel
            .positionTelemetryOldProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue) { // telemetry is current
                        altitudeAGLButton.getStyleClass().removeAll("stale");
                    } else { // telemetry is old
                        altitudeAGLButton.getStyleClass().add("stale");
                    }
                });

        distanceToDrone.textProperty().bind(viewModel.distanceToDroneTextProperty());
        viewModel
            .positionTelemetryOldProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue) { // telemetry is current
                        distanceToDroneButton.getStyleClass().removeAll("stale");
                    } else { // telemetry is old
                        distanceToDroneButton.getStyleClass().add("stale");
                    }
                });

        // autopilot state
        updateAutoPilotButton();
        viewModel
            .linkboxAuthorizedProperty()
            .addListener(
                (observableValue, oldValue, newValue) ->
                    Dispatcher.platform().runLaterAsync(this::updateAutoPilotButton));

        timeUntilLanding.textProperty().bind(viewModel.timeUntilLandingTextProperty());
        viewModel
            .flightTimeTelemetryOldProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue) { // telemetry is current
                        timeUntilLandingButton.getStyleClass().removeAll("stale");
                    } else { // telemetry is old
                        timeUntilLandingButton.getStyleClass().add("stale");
                    }
                });

        // flight time
        flightTime.textProperty().bind(viewModel.flightTimeTextProperty());
        viewModel
            .flightTimeTelemetryOldProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue) { // telemetry is current
                        flightTimeButton.getStyleClass().removeAll("stale");
                    } else { // telemetry is old
                        flightTimeButton.getStyleClass().add("stale");
                    }
                });

        // oa distance
        obstacleAvoidanceButton.textProperty().bind(viewModel.oaDistanceTextProperty());
        viewModel
            .oaDistanceTelemetryOldProperty()
            .addListener((observable, oldValue, newValue) -> Dispatcher.platform().run(this::updateOaDistanceStyle));
        viewModel
            .oaDistanceAlertLevelProperty()
            .addListener((observable, oldValue, newValue) -> Dispatcher.platform().run(this::updateOaDistanceStyle));
        viewModel
            .obstacleAvoidanceModeProperty()
            .addListener((observable, oldValue, newValue) -> Dispatcher.platform().run(this::updateOaDistanceStyle));

        viewModel
            .hardwareOACapableProperty()
            .addListener((observable, oldValue, newValue) -> Dispatcher.platform().run(this::updateOaDistanceStyle));
        updateOaDistanceStyle();

        obstacleAvoidanceButton.disableProperty().bind(viewModel.hardwareOACapableProperty().not());
    }

    private void updateAutoPilotButton() {
        if (!viewModel.linkboxAuthorizedProperty().get()) {
            // LOCK THE BUTTON - Linkbox has not authorized
            autoPilotButton.textProperty().unbind();
            autoPilotButton.setText(languageHelper.getString(TelemetryView.class, "locked"));
        } else {
            autoPilotButton
                .textProperty()
                .bind(
                    Bindings.createStringBinding(
                        () -> {
                            var autopilotState = viewModel.autoPilotStateProperty().getValue();
                            if (autopilotState == null) {
                                autopilotState = AutopilotState.UNKNOWN;
                            }

                            return autopilotStateStringConverter.toString(autopilotState);
                        },
                        viewModel.autoPilotStateProperty()));
            viewModel
                .autoPilotStateProperty()
                .addListener((observable, oldValue, newValue) -> updateAutopilotStateStyle());
            viewModel
                .autopilotStateTelemetryOldProperty()
                .addListener((observable, oldValue, newValue) -> updateAutopilotStateStyle());
        }

        updateAutopilotStateStyle();
    }

    private void updateBatteryStyle() {
        if (viewModel.batteryTelemetryOldProperty().get()) {
            batteryButton.getStyleClass().add("stale");
        } else {
            batteryButton.getStyleClass().removeAll("stale");
        }

        batteryButton.getStyleClass().removeAll("prewarn");
        BatteryAlertLevel alertLevel = viewModel.alertBatteryLevelProperty().getValue();
        if (alertLevel == null) {
            alertLevel = BatteryAlertLevel.UNKNOWN;
        }

        switch (alertLevel) {
        case YELLOW:
            CssStyleRotation.stop(batteryButton);
            batteryButton.getStyleClass().addAll("prewarn");
            break;
        case RED:
            CssStyleRotation.setCritical(batteryButton);
            break;
        case UNKNOWN:
        default:
            CssStyleRotation.stop(batteryButton);
            break;
        }
    }

    private void updateGnssStyle() {
        if (viewModel.gnssTelemetryOldProperty().get()) {
            gnssButton.getStyleClass().add("stale");
        } else {
            gnssButton.getStyleClass().removeAll("stale");
        }

        gnssButton
            .getStyleClass()
            .removeAll("icon-rtk-overlay", "prewarn", "icon-gnss-1", "icon-gnss-2", "icon-gnss-lost");
        GnssState gnssState = viewModel.gnssStateProperty().getValue();
        if (gnssState == null) {
            gnssState = GnssState.UNKNOWN;
        }

        switch (gnssState) {
        case RTK_FIXED:
            CssStyleRotation.stop(gnssButton);
            gnssButton.getStyleClass().addAll("icon-rtk-fixed");
            break;
        case RTK_FLOAT:
            CssStyleRotation.stop(gnssButton);
            gnssButton.getStyleClass().addAll("prewarn", "icon-rtk-float");
            break;
        case UNKNOWN:
        default:
            // TODO check if reconnected: old = null
            Quantity<Dimension.Percentage> gnssQuality = viewModel.gnssQualityProperty().getValue();
            int quality = gnssQuality != null ? gnssQuality.convertTo(Unit.PERCENTAGE).getValue().intValue() : -1;
            int qualityLevel;
            if (quality > 70) {
                qualityLevel = 3;
            } else if (quality > 40) {
                qualityLevel = 2;
            } else if (quality >= 0) {
                qualityLevel = 1;
            } else {
                qualityLevel = 0;
            }

            switch (qualityLevel) {
            case 1:
                CssStyleRotation.setCritical(gnssButton);
                gnssButton.getStyleClass().addAll("icon-gnss-lost");
                break;
            case 2:
                CssStyleRotation.stop(gnssButton);
                gnssButton.getStyleClass().addAll("icon-gnss-1", "prewarn");
                break;
            case 3:
                CssStyleRotation.stop(gnssButton);
                gnssButton.getStyleClass().addAll("icon-gnss-2");
                break;
            default:
                CssStyleRotation.stop(gnssButton);
                gnssButton.getStyleClass().addAll("icon-gnss-lost");
                break;
            }

            break;
        }
    }

    private void updateFlightSegmentStyle() {
        if (viewModel.flightSegmentTelemetryOldProperty().get()) {
            flightSegmentButton.getStyleClass().add("stale");
        } else {
            flightSegmentButton.getStyleClass().removeAll("stale");
        }

        flightSegmentButton
            .getStyleClass()
            .removeAll(
                "icon-fp-ground",
                "icon-fp-takeoff",
                "icon-fp-airborne",
                "icon-fp-mission",
                "icon-fp-rth",
                "icon-fp-landing");
        FlightSegment flightSegment = viewModel.flightSegmentProperty().getValue();
        if (flightSegment == null) {
            flightSegment = UNKNOWN;
        }

        switch (flightSegment) {
        case ON_GROUND:
            flightSegmentButton.getStyleClass().addAll("icon-fp-ground");
            break;
        case TAKEOFF:
            flightSegmentButton.getStyleClass().addAll("icon-fp-takeoff");
            break;
        case HOLD:
            flightSegmentButton.getStyleClass().addAll("icon-fp-airborne");
            break;
        case PLAN_RUNNING:
            flightSegmentButton.getStyleClass().addAll("icon-fp-mission");
            break;
        case RETURN_TO_HOME:
            flightSegmentButton.getStyleClass().addAll("icon-fp-rth");
            break;
        case LANDING:
            flightSegmentButton.getStyleClass().addAll("icon-fp-landing");
            break;
        case UNKNOWN:
            break;
        default:
            flightSegmentButton.getStyleClass().addAll("icon-fp-ground");
            break;
        }
    }

    private void updateAutopilotStateStyle() {
        if (viewModel.autopilotStateTelemetryOldProperty().get()) {
            autoPilotButton.getStyleClass().add("stale");
        } else {
            autoPilotButton.getStyleClass().remove("stale");
        }

        autoPilotButton
            .getStyleClass()
            .removeAll("icon-robot", "icon-manual", "prewarn", "icon-linkbox-locked", "critical");
        if (!viewModel.linkboxAuthorizedProperty().get()) {
            autoPilotButton.getStyleClass().addAll("icon-linkbox-locked", "telemetry-cell", "critical");
        } else {
            var autopilotState = viewModel.autoPilotStateProperty().getValue();
            if (autopilotState == null) {
                autopilotState = AutopilotState.UNKNOWN;
            }

            switch (autopilotState) {
            case UNKNOWN:
                autoPilotButton.getStyleClass().addAll("icon-robot");
                break;
            case MANUAL:
                autoPilotButton.getStyleClass().addAll("icon-manual", "prewarn");
                break;
            case AUTOPILOT:
                autoPilotButton.getStyleClass().addAll("icon-robot");
                break;
            default:
                autoPilotButton.getStyleClass().addAll("icon-robot");
                break;
            }
        }
    }

    private void updateOaDistanceStyle() {
        obstacleAvoidanceButton
            .getStyleClass()
            .removeAll("icon-oa-off", "icon-oa-3", "icon-oa-2", "icon-oa-1", "icon-oa-0", "prewarn", "stale");

        boolean hardwareOaCapable = viewModel.hardwareOACapableProperty().get();
        IObstacleAvoidance.Mode oaMode = viewModel.obstacleAvoidanceModeProperty().getValue();

        // TODO what happens to the button if hardware not OA capable? And why is oaMode == null not treated as
        // "unknown"?
        if (hardwareOaCapable && oaMode != null) {
            switch (oaMode) {
            case DISABLED:
                CssStyleRotation.stop(obstacleAvoidanceButton);
                obstacleAvoidanceButton.getStyleClass().addAll("prewarn", "icon-oa-off");
                break;
            case NOT_AVAILABLE:
            case UNKNOWN:
            default:
                CssStyleRotation.stop(obstacleAvoidanceButton);
                obstacleAvoidanceButton.getStyleClass().addAll("icon-oa-off", "stale");
                break;
            case ENABLED:
                DistanceSensor.AlertLevel oaAlertLevel = viewModel.oaDistanceAlertLevelProperty().getValue();
                switch (oaAlertLevel) {
                case LEVEL3:
                    CssStyleRotation.stop(obstacleAvoidanceButton);
                    obstacleAvoidanceButton.getStyleClass().addAll("icon-oa-3");
                    break;
                case LEVEL2:
                    CssStyleRotation.stop(obstacleAvoidanceButton);
                    obstacleAvoidanceButton.getStyleClass().addAll("icon-oa-2");
                    break;
                case LEVEL1_PREWARN:
                    CssStyleRotation.stop(obstacleAvoidanceButton);
                    obstacleAvoidanceButton.getStyleClass().addAll("prewarn", "icon-oa-1");
                    break;
                case LEVEL0_CRITICAL:
                    CssStyleRotation.setCritical(obstacleAvoidanceButton);
                    obstacleAvoidanceButton.getStyleClass().addAll("icon-oa-0");
                    break;
                case UNKNOWN:
                default:
                    CssStyleRotation.stop(obstacleAvoidanceButton);
                    obstacleAvoidanceButton.getStyleClass().addAll("prewarn", "icon-oa-off");
                    break;
                }

                break;
            }
        }

        //        boolean old = viewModel.oaDistanceTelemetryOldProperty().get();
        //        if (!old) {
        //            obstacleAvoidanceButton.getStyleClass().removeAll("stale");
        //        } else {
        //            obstacleAvoidanceButton.getStyleClass().add("stale");
        //        }
    }

    public void OnBatteryButtonClicked(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getBatteryButtonCommand().execute();
    }

    public void OnGnssButtonClicked(ActionEvent actionEvent) {
        viewModel.getShowTelemetryDetailDialogCommand().execute();
    }

    public void OnAutoPilotButtonClicked(ActionEvent actionEvent) {
        if (!viewModel.linkboxAuthorizedProperty().get()) {
            viewModel.getShowUAVLockedDialogCommand().execute();
        }
    }

    public BooleanProperty showTelemetryDetailDialogPossibleProperty() {
        return showTelemetryDetailDialogPossible;
    }

}
