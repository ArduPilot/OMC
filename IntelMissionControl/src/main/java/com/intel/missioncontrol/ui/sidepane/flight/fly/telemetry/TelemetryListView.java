/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import com.intel.missioncontrol.drone.BatteryAlertLevel;
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
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javax.inject.Inject;

public class TelemetryListView extends ViewBase<TelemetryListViewModel> {

    @FXML
    public VBox batteryVBox;

    @FXML
    public VBox gnssVBox;

    @FXML
    public VBox altitudeAGLVBox;

    @FXML
    public VBox flightSegmentVBox;

    @FXML
    public VBox distanceToDroneVBox;

    @FXML
    public Label latitude;

    @FXML
    public Label longitude;

    @FXML
    public VBox gnss2VBox;

    private IDialogContextProvider dialogContextProvider;
    private final EnumConverter<FlightSegment> flightSegmentStringConverter;
    private final EnumConverter<GnssState> gnssStateStringConverter;
    private BooleanProperty showTelemetryDetailDialogPossible = new SimpleBooleanProperty();

    @InjectViewModel
    private TelemetryListViewModel viewModel;

    @InjectContext
    private Context context;

    @FXML
    public Label remainingChargePercentLabel;

    @FXML
    public Label gnssQuality;

    @FXML
    private Label gnssStatus;

    @FXML
    public Label gnssSatellites;

    @FXML
    private GridPane rootNode;

    @FXML
    private Label batteryLevel;

    @FXML
    private Label flightSegment;

    @FXML
    private Label altitudeAgl;

    @FXML
    private Label distanceToDrone;

    @Inject
    public TelemetryListView(ILanguageHelper languageHelper, IDialogContextProvider dialogContextProvider) {
        this.dialogContextProvider = dialogContextProvider;
        this.flightSegmentStringConverter = new EnumConverter<>(languageHelper, FlightSegment.class);
        this.gnssStateStringConverter = new EnumConverter<>(languageHelper, GnssState.class);
    }

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    protected ViewModel getViewModel() {
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

        // gnss
        gnssQuality.textProperty().bind(viewModel.gnssQualityTextProperty());
        gnssStatus
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

        gnssSatellites
            .textProperty()
            .bind(
                Bindings.createStringBinding(
                    () -> {
                        int gnssNumberOfSatellites = viewModel.gnssNumberOfSatellitesProperty().getValue().intValue();
                        if (gnssNumberOfSatellites < 0) {
                            return "--";
                        }

                        return String.valueOf(gnssNumberOfSatellites);
                    },
                    viewModel.gnssNumberOfSatellitesProperty()));

        viewModel.gnssQualityProperty().addListener((observable, oldValue, newValue) -> updateGnssStyle());
        viewModel.gnssTelemetryOldProperty().addListener((observable, oldValue, newValue) -> updateGnssStyle());

        latitude.textProperty().bind(viewModel.latitudeTextProperty());
        longitude.textProperty().bind(viewModel.longitudeTextProperty());

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
                        altitudeAGLVBox.getStyleClass().removeAll("stale");
                    } else { // telemetry is old
                        altitudeAGLVBox.getStyleClass().add("stale");
                    }
                });

        // TODO distanceToDrone
        distanceToDrone.textProperty().bind(viewModel.distanceToDroneTextProperty());
        viewModel
            .positionTelemetryOldProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (!newValue) { // telemetry is current
                        distanceToDroneVBox.getStyleClass().removeAll("stale");
                    } else { // telemetry is old
                        distanceToDroneVBox.getStyleClass().add("stale");
                    }
                });
    }

    private void updateBatteryStyle() {
        BatteryAlertLevel alertLevel = viewModel.alertBatteryLevelProperty().getValue();
        boolean old = viewModel.batteryTelemetryOldProperty().get();

        batteryVBox.getStyleClass().removeAll("prewarn", "critical");
        if (!old) {
            batteryVBox.getStyleClass().removeAll("stale");
        } else {
            batteryVBox.getStyleClass().add("stale");
        }

        switch (alertLevel) {
        case YELLOW:
            batteryVBox.getStyleClass().add("prewarn");
            break;
        case RED:
            batteryVBox.getStyleClass().add("critical");
            break;
        case UNKNOWN:
        default:
            break;
        }
    }

    private void updateGnssStyle() {
        // TODO check if reconnected: old = null
        Quantity<Dimension.Percentage> gnssQuality = viewModel.gnssQualityProperty().getValue();
        boolean old = viewModel.gnssTelemetryOldProperty().get();

        int qualityLevel = gnssQuality != null ? gnssQuality.convertTo(Unit.PERCENTAGE).getValue().intValue() : -1;
        setGnssStyle(old, qualityLevel, gnssQuality);
    }

    private void setGnssStyle(boolean old, int quality, Quantity<Dimension.Percentage> gnssQuality) {
        gnssVBox.getStyleClass().removeAll("prewarn", "critical");
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

        if (!old) {
            gnssVBox.getStyleClass().removeAll("stale");
        } else {
            gnssVBox.getStyleClass().add("stale");
        }

        switch (qualityLevel) {
        case 1:
            gnssVBox.getStyleClass().add("critical");
            break;
        case 2:
            gnssVBox.getStyleClass().add("prewarn");
            break;
        case 3:
        case 4:
        case 5:
            break;
        default:
            gnssVBox.getStyleClass().add("critical");
            break;
        }

        gnss2VBox.getStyleClass().clear();
        gnss2VBox.getStyleClass().addAll(gnssVBox.getStyleClass());
    }

    private void updateFlightSegmentStyle() {
        boolean old = viewModel.flightSegmentTelemetryOldProperty().get();
        if (!old) {
            flightSegmentVBox.getStyleClass().removeAll("stale");
        } else {
            flightSegmentVBox.getStyleClass().add("stale");
        }
    }

    public BooleanProperty showTelemetryDetailDialogPossibleProperty() {
        return showTelemetryDetailDialogPossible;
    }

}
