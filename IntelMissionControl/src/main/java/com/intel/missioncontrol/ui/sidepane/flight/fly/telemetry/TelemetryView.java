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
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.ui.EnumConverter;
import com.intel.missioncontrol.ui.ViewBase;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javax.inject.Inject;

public class TelemetryView extends ViewBase<TelemetryViewModel> {

    private final EnumConverter<FlightSegment> flightSegmentStringConverter;
    private final EnumConverter<GnssState> gnssStateStringConverter;
    private final EnumConverter<AutopilotState> autopilotStateStringConverter;
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
    private Button oaDistanceButton;

    @InjectContext
    private Context context;

    @FXML
    private GridPane rootNode;

    @FXML
    private Label batteryLevel;

    @FXML
    private Label gnssQuality;

    @FXML
    private Label flightSegment;

    @FXML
    private Label altitudeAgl;

    @FXML
    private Label distanceToDrone;

    @FXML
    private Label autopilotState;

    @FXML
    private Label timeUntilLanding;

    @FXML
    private Label flightTime;

    @FXML
    private Label oaDistance;

    @Inject
    public TelemetryView(ILanguageHelper languageHelper, IDialogContextProvider dialogContextProvider) {
        this.dialogContextProvider = dialogContextProvider;
        this.flightSegmentStringConverter = new EnumConverter<>(languageHelper, FlightSegment.class);
        this.gnssStateStringConverter = new EnumConverter<>(languageHelper, GnssState.class);
        this.autopilotStateStringConverter = new EnumConverter<>(languageHelper, AutopilotState.class);
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
        batteryLevel.textProperty().bind(viewModel.batteryTextProperty());
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
                        batteryButton.getStyleClass().add("icon-battery-4");
                    } else if (remainingChargePercent >= 60) {
                        batteryButton.getStyleClass().add("icon-battery-3");
                    } else if (remainingChargePercent >= 40) {
                        batteryButton.getStyleClass().add("icon-battery-2");
                    } else if (remainingChargePercent >= 20) {
                        batteryButton.getStyleClass().add("icon-battery-1");
                    } else {
                        batteryButton.getStyleClass().add("icon-battery-empty");
                    }
                });

        // gnss
        gnssQuality
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
        flightSegment
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
        autopilotState
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
        viewModel.autoPilotStateProperty().addListener((observable, oldValue, newValue) -> updateAutopilotStateStyle());
        viewModel
            .autopilotStateTelemetryOldProperty()
            .addListener((observable, oldValue, newValue) -> updateAutopilotStateStyle());

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
        oaDistance.textProperty().bind(viewModel.oaDistanceTextProperty());
        viewModel
            .oaDistanceTelemetryOldProperty()
            .addListener((observable, oldValue, newValue) -> updateOaDistanceStyle());
        viewModel
            .oaDistanceAlertLevelProperty()
            .addListener((observable, oldValue, newValue) -> updateOaDistanceStyle());
        viewModel
            .obstacleAvoidanceModeProperty()
            .addListener((observable, oldValue, newValue) -> updateOaDistanceStyle());
        updateOaDistanceStyle();

        oaDistanceButton.disableProperty().bind(viewModel.hardwareOACapableProperty().not());
    }

    private void updateBatteryStyle() {
        BatteryAlertLevel alertLevel = viewModel.alertBatteryLevelProperty().getValue();
        boolean old = viewModel.batteryTelemetryOldProperty().get();

        batteryButton.getStyleClass().removeAll("prewarn", "critical");

        if (alertLevel == null) {
            alertLevel = BatteryAlertLevel.UNKNOWN;
        }

        switch (alertLevel) {
        case YELLOW:
            batteryButton.getStyleClass().add("prewarn");
            if (!old) {
                batteryButton.getStyleClass().removeAll("stale");
            } else {
                batteryButton.getStyleClass().add("stale");
            }

            break;
        case RED:
            batteryButton.getStyleClass().add("critical");
            if (!old) {
                batteryButton.getStyleClass().removeAll("stale");
            } else {
                batteryButton.getStyleClass().add("stale");
            }

            break;
        case UNKNOWN:
        default:
            if (!old) {
                batteryButton.getStyleClass().removeAll("stale");
            } else {
                batteryButton.getStyleClass().add("stale");
            }

            break;
        }
    }

    private void updateGnssStyle() {
        // TODO check if reconnected: old = null
        Quantity<Dimension.Percentage> gnssQuality = viewModel.gnssQualityProperty().getValue();
        boolean old = viewModel.gnssTelemetryOldProperty().get();

        int qualityLevel = gnssQuality != null ? gnssQuality.convertTo(Unit.PERCENTAGE).getValue().intValue() : -1;
        setGnssButtonStyle(old, qualityLevel);
    }

    private void setGnssButtonStyle(boolean old, int quality) {
        GnssState gnssState = viewModel.gnssStateProperty().getValue();
        gnssButton.getStyleClass().clear();
        gnssButton
            .getStyleClass()
            .removeAll(
                "icon-rtk-overlay",
                "telemetry-cell",
                "prewarn",
                "icon-gnss-1",
                "icon-gnss-2",
                "icon-gnss-3",
                "icon-gnss-4",
                "icon-gnss-5",
                "critical",
                "icon-gnss-lost");

        int qualityLevel;
        if (quality > 90) {
            qualityLevel = 5;
        } else if (quality > 70) {
            qualityLevel = 4;
        } else if (quality > 40) {
            qualityLevel = 3;
        } else if (quality > 6) {
            qualityLevel = 2;
        } else if (quality >= 0) {
            qualityLevel = 1;
        } else {
            qualityLevel = 0;
        }

        switch (qualityLevel) {
        case 1:
            gnssButton.getStyleClass().add("telemetry-cell");
            gnssButton.getStyleClass().add("critical");
            gnssButton.getStyleClass().add("icon-gnss-1");
            if (!old) {
                gnssButton.getStyleClass().removeAll("stale");
            } else {
                gnssButton.getStyleClass().add("stale");
            }

            break;
        case 2:
            gnssButton.getStyleClass().add("telemetry-cell");
            gnssButton.getStyleClass().add("prewarn");
            gnssButton.getStyleClass().add("icon-gnss-2");
            if (!old) {
                gnssButton.getStyleClass().removeAll("stale");
            } else {
                gnssButton.getStyleClass().add("stale");
            }

            break;
        case 3:
            gnssButton.getStyleClass().add("telemetry-cell");
            gnssButton.getStyleClass().add("icon-gnss-3");
            if (!old) {
                gnssButton.getStyleClass().removeAll("stale");
            } else {
                gnssButton.getStyleClass().add("stale");
            }

            break;
        case 4:
            gnssButton.getStyleClass().add("telemetry-cell");
            gnssButton.getStyleClass().add("icon-gnss-4");
            if (!old) {
                gnssButton.getStyleClass().removeAll("stale");
            } else {
                gnssButton.getStyleClass().add("stale");
            }

            break;
        case 5:
            gnssButton.getStyleClass().add("telemetry-cell");
            gnssButton.getStyleClass().add("icon-gnss-5");
            if (!old) {
                gnssButton.getStyleClass().removeAll("stale");
            } else {
                gnssButton.getStyleClass().add("stale");
            }

            break;
        default:
            gnssButton.getStyleClass().add("telemetry-cell");
            if (!old) {
                gnssButton.getStyleClass().removeAll("stale");
            } else {
                gnssButton.getStyleClass().add("stale");
            }

            break;
        }

        if (gnssState == null) gnssState = GnssState.UNKNOWN;

        switch (gnssState) {
        case RTK_FIXED:
        case RTK_FLOAT:
            gnssButton.getStyleClass().add("icon-rtk-overlay");
            break;
        }
    }

    private void updateFlightSegmentStyle() {
        FlightSegment flightSegment = viewModel.flightSegmentProperty().getValue();
        boolean old = viewModel.flightSegmentTelemetryOldProperty().get();

        if (flightSegment == null) {
            flightSegment = UNKNOWN;
        }

        flightSegmentButton.getStyleClass().clear();
        switch (flightSegment) {
        case ON_GROUND:
            flightSegmentButton.getStyleClass().add("icon-fp-ground");
            flightSegmentButton.getStyleClass().add("telemetry-cell");
            if (!old) {
                flightSegmentButton.getStyleClass().removeAll("stale");
            } else {
                flightSegmentButton.getStyleClass().add("stale");
            }

            break;
        case TAKEOFF:
            flightSegmentButton.getStyleClass().add("icon-fp-takeoff");
            flightSegmentButton.getStyleClass().add("telemetry-cell");
            if (!old) {
                flightSegmentButton.getStyleClass().removeAll("stale");
            } else {
                flightSegmentButton.getStyleClass().add("stale");
            }

            break;
        case HOLD:
            flightSegmentButton.getStyleClass().add("icon-fp-airborne");
            flightSegmentButton.getStyleClass().add("telemetry-cell");
            if (!old) {
                flightSegmentButton.getStyleClass().removeAll("stale");
            } else {
                flightSegmentButton.getStyleClass().add("stale");
            }

            break;
        case PLAN_RUNNING:
            flightSegmentButton.getStyleClass().add("icon-fp-mission");
            flightSegmentButton.getStyleClass().add("telemetry-cell");
            if (!old) {
                flightSegmentButton.getStyleClass().removeAll("stale");
            } else {
                flightSegmentButton.getStyleClass().add("stale");
            }

            break;
        case RETURN_TO_HOME:
            flightSegmentButton.getStyleClass().add("icon-fp-rth");
            flightSegmentButton.getStyleClass().add("telemetry-cell");
            if (!old) {
                flightSegmentButton.getStyleClass().removeAll("stale");
            } else {
                flightSegmentButton.getStyleClass().add("stale");
            }

            break;
        case LANDING:
            flightSegmentButton.getStyleClass().add("icon-fp-landing");
            flightSegmentButton.getStyleClass().add("telemetry-cell");
            if (!old) {
                flightSegmentButton.getStyleClass().removeAll("stale");
            } else {
                flightSegmentButton.getStyleClass().add("stale");
            }

            break;
        case UNKNOWN:
            flightSegmentButton.getStyleClass().add("telemetry-cell");
            if (!old) {
                flightSegmentButton.getStyleClass().removeAll("stale");
            } else {
                flightSegmentButton.getStyleClass().add("icon-warning");
                flightSegmentButton.getStyleClass().add("stale");
            }

            break;
        default:
            flightSegmentButton.getStyleClass().add("icon-fp-ground");
            flightSegmentButton.getStyleClass().add("telemetry-cell");
            if (!old) {
                flightSegmentButton.getStyleClass().removeAll("stale");
            } else {
                flightSegmentButton.getStyleClass().add("stale");
            }

            break;
        }
    }

    private void updateAutopilotStateStyle() {
        var autopilotState = viewModel.autoPilotStateProperty().getValue();
        boolean old = viewModel.autopilotStateTelemetryOldProperty().get();

        if (autopilotState == null) {
            autopilotState = AutopilotState.UNKNOWN;
        }

        autoPilotButton.getStyleClass().clear();
        switch (autopilotState) {
        case UNKNOWN:
            autoPilotButton.getStyleClass().add("icon-robot");
            autoPilotButton.getStyleClass().add("telemetry-cell");
            if (!old) {
                autoPilotButton.getStyleClass().removeAll("stale");
            } else {
                autoPilotButton.getStyleClass().add("stale");
            }

            break;
        case MANUAL:
            autoPilotButton.getStyleClass().add("icon-manual");
            autoPilotButton.getStyleClass().add("telemetry-cell");
            autoPilotButton.getStyleClass().add("prewarn");
            if (!old) {
                autoPilotButton.getStyleClass().removeAll("stale");
            } else {
                autoPilotButton.getStyleClass().add("stale");
            }

            break;
        case AUTOPILOT:
            autoPilotButton.getStyleClass().add("icon-robot");
            autoPilotButton.getStyleClass().add("telemetry-cell");
            if (!old) {
                autoPilotButton.getStyleClass().removeAll("stale");
            } else {
                autoPilotButton.getStyleClass().add("stale");
            }

            break;
        default:
            autoPilotButton.getStyleClass().add("icon-robot");
            autoPilotButton.getStyleClass().add("telemetry-cell");
            if (!old) {
                autoPilotButton.getStyleClass().removeAll("stale");
            } else {
                autoPilotButton.getStyleClass().add("stale");
            }

            break;
        }
    }

    private void updateOaDistanceStyle() {
        DistanceSensor.AlertLevel oaAlertLevel = viewModel.oaDistanceAlertLevelProperty().getValue();

        boolean old = viewModel.oaDistanceTelemetryOldProperty().get();

        oaDistanceButton
            .getStyleClass()
            .removeAll("icon-oa-off", "icon-oa-3", "icon-oa-2", "icon-oa-1", "icon-oa-0", "prewarn", "critical");
        oaDistanceButton.getStyleClass().add("telemetry-cell");

        switch (oaAlertLevel) {
        case LEVEL3:
            oaDistanceButton.getStyleClass().add("icon-oa-3");
            break;
        case LEVEL2:
            oaDistanceButton.getStyleClass().add("icon-oa-2");
            break;
        case LEVEL1_PREWARN:
            oaDistanceButton.getStyleClass().add("prewarn");
            oaDistanceButton.getStyleClass().add("icon-oa-1");
            break;
        case LEVEL0_CRITICAL:
            oaDistanceButton.getStyleClass().add("icon-oa-0");
            oaDistanceButton.getStyleClass().add("critical");
            break;
        case UNKNOWN:
        default:
            oaDistanceButton.getStyleClass().add("prewarn");
            oaDistanceButton.getStyleClass().add("icon-oa-off");
        }

        if (!old) {
            oaDistanceButton.getStyleClass().removeAll("stale");
        } else {
            oaDistanceButton.getStyleClass().add("stale");
        }
    }

    public void OnBatteryButtonClicked(@SuppressWarnings("unused") ActionEvent actionEvent) {
        viewModel.getBatteryButtonCommand().execute();
    }

    public void OnGnssButtonClicked(ActionEvent actionEvent) {
        viewModel.getShowTelemetryDetailDialogCommand().execute();
    }

    public BooleanProperty showTelemetryDetailDialogPossibleProperty() {
        return showTelemetryDetailDialogPossible;
    }

}
