/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import com.google.inject.Inject;
import com.intel.missioncontrol.drone.AutopilotState;
import com.intel.missioncontrol.drone.BatteryAlertLevel;
import com.intel.missioncontrol.drone.FlightSegment;
import com.intel.missioncontrol.drone.GnssState;
import com.intel.missioncontrol.drone.IBattery;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.IGnssInfo;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.property.UIAsyncQuantityProperty;
import com.intel.missioncontrol.measure.property.UIQuantityPropertyMetadata;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import de.saxsys.mvvmfx.InjectScope;
import gov.nasa.worldwind.geom.Position;
import java.time.Duration;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;

public class TelemetryListViewModel extends ViewModelBase {
    private final IDialogService dialogService;

    @InjectScope
    private FlightScope flightScope;

    private final UIAsyncObjectProperty<BatteryAlertLevel> alertBatteryLevel = new UIAsyncObjectProperty<>(this);
    private final UIAsyncQuantityProperty<Dimension.Percentage> batteryRemainingCharge;
    private final UIAsyncQuantityProperty<Dimension.Voltage> batteryVoltage;
    private final UIAsyncStringProperty batteryText = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty gnssQualityText = new UIAsyncStringProperty(this);

    private final UIAsyncObjectProperty<FlightSegment> flightSegment = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<GnssState> gnssState = new UIAsyncObjectProperty<>(this);
    private final UIAsyncQuantityProperty<Dimension.Percentage> gnssQuality;
    private final UIAsyncIntegerProperty gnssNumberOfSatellites = new UIAsyncIntegerProperty(this);

    private final UIAsyncStringProperty altitudeAglText = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty distanceToDroneText = new UIAsyncStringProperty(this);

    private final UIAsyncStringProperty latitudeText = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty longitudeText = new UIAsyncStringProperty(this);

    private final UIAsyncObjectProperty<Position> position = new UIAsyncObjectProperty<>(this);
    private final UIAsyncStringProperty timeUntilLandingText = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty flightTimeText = new UIAsyncStringProperty(this);
    private final UIAsyncObjectProperty<AutopilotState> autoPilotState = new UIAsyncObjectProperty<>(this);
    private final UIAsyncStringProperty oaDistanceText = new UIAsyncStringProperty(this);

    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);

    private final UIAsyncBooleanProperty batteryTelemetryOld = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty gnssTelemetryOld = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty flightSegmentTelemetryOld = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty positionTelemetryOld = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty autopilotStateTelemetryOld = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty flightTimeTelemetryOld = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty oaDistanceTelemetryOld = new UIAsyncBooleanProperty(this);

    private final UIAsyncBooleanProperty batteryButtonToggle = new UIAsyncBooleanProperty(this);
    private final BooleanProperty telemetryDetailsAvailable = new SimpleBooleanProperty(true);

    @Inject
    public TelemetryListViewModel(
            ISettingsManager settingsManager,
            IQuantityStyleProvider quantityStyleProvider,
            IDialogService dialogService) {
        GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);
        this.dialogService = dialogService;
        ReadOnlyAsyncObjectProperty<? extends IBattery> battery =
            PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::batteryProperty);

        alertBatteryLevel.bind(PropertyPath.from(battery).selectReadOnlyAsyncObject(IBattery::alertLevelProperty));

        batteryTelemetryOld.bind(PropertyPath.from(battery).selectReadOnlyAsyncBoolean(IBattery::telemetryOldProperty));
        gnssTelemetryOld.bind(
            PropertyPath.from(drone)
                .select(IDrone::gnssInfoProperty)
                .selectReadOnlyAsyncBoolean(IGnssInfo::telemetryOldProperty));
        flightSegmentTelemetryOld.bind(
            PropertyPath.from(drone).selectReadOnlyAsyncBoolean(IDrone::flightSegmentTelemetryOldProperty));
        positionTelemetryOld.bind(
            PropertyPath.from(drone).selectReadOnlyAsyncBoolean(IDrone::positionTelemetryOldProperty));
        autopilotStateTelemetryOld.bind(
            PropertyPath.from(drone).selectReadOnlyAsyncBoolean(IDrone::autopilotStateTelemetryOldProperty));
        flightTimeTelemetryOld.bind(
            PropertyPath.from(drone).selectReadOnlyAsyncBoolean(IDrone::flightTimeTelemetryOldProperty));
        oaDistanceTelemetryOld.bind(
            PropertyPath.from(drone).selectReadOnlyAsyncBoolean(IDrone::flightTimeTelemetryOldProperty));

        batteryRemainingCharge =
            new UIAsyncQuantityProperty<>(
                this,
                new UIQuantityPropertyMetadata.Builder<Dimension.Percentage>()
                    .quantityStyleProvider(quantityStyleProvider)
                    .unitInfo(UnitInfo.PERCENTAGE)
                    .create());
        batteryRemainingCharge.bind(
            PropertyPath.from(battery).selectReadOnlyAsyncDouble(IBattery::remainingChargePercentageProperty),
            remainingChargePercentage ->
                Double.isNaN(remainingChargePercentage.doubleValue())
                    ? null
                    : Quantity.of(remainingChargePercentage, Unit.PERCENTAGE));

        batteryVoltage =
            new UIAsyncQuantityProperty<>(
                this,
                new UIQuantityPropertyMetadata.Builder<Dimension.Voltage>()
                    .quantityStyleProvider(quantityStyleProvider)
                    .unitInfo(UnitInfo.VOLTAGE)
                    .create());
        batteryVoltage.bind(
            PropertyPath.from(battery).selectReadOnlyAsyncDouble(IBattery::voltageProperty),
            voltage -> Double.isNaN(voltage.doubleValue()) ? null : Quantity.of(voltage, Unit.VOLT));

        QuantityFormat remainingChargeFormat = new AdaptiveQuantityFormat(generalSettings);
        remainingChargeFormat.setSignificantDigits(6);
        remainingChargeFormat.setMaximumFractionDigits(0);

        QuantityFormat voltageFormat = new AdaptiveQuantityFormat(generalSettings);
        voltageFormat.setSignificantDigits(6);
        voltageFormat.setMaximumFractionDigits(1);

        batteryText.bind(
            Bindings.createStringBinding(
                () -> {
                    Quantity<Dimension.Percentage> remainingCharge = batteryRemainingCharge.get();
                    Quantity<Dimension.Voltage> voltage = batteryVoltage.get();
                    if (remainingCharge != null && voltage != null) {
                        if (!batteryButtonToggle.get()) {
                            return remainingChargeFormat.format(remainingCharge);
                        } else {
                            return voltageFormat.format(voltage);
                        }
                    } else {
                        return "--";
                    }
                },
                batteryVoltage,
                batteryRemainingCharge,
                batteryButtonToggle));

        AsyncObjectProperty<FlightSegment> flightSegment = new SimpleAsyncObjectProperty<>(this);
        flightSegment.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::flightSegmentProperty));

        QuantityFormat gnssQualityFormat = new AdaptiveQuantityFormat(generalSettings);
        gnssQualityFormat.setSignificantDigits(6);
        gnssQualityFormat.setMaximumFractionDigits(0);

        gnssQuality =
            new UIAsyncQuantityProperty<>(
                this,
                new UIQuantityPropertyMetadata.Builder<Dimension.Percentage>()
                    .quantityStyleProvider(quantityStyleProvider)
                    .unitInfo(UnitInfo.PERCENTAGE)
                    .create());

        gnssQualityText.bind(
            Bindings.createStringBinding(
                () -> {
                    Quantity<Dimension.Percentage> gnssQual = gnssQuality.get();
                    if (gnssQual != null) {
                        return gnssQualityFormat.format(gnssQual);
                    } else {
                        return "--";
                    }
                },
                gnssQuality));

        ReadOnlyAsyncObjectProperty<? extends IGnssInfo> gnssInfo =
            PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::gnssInfoProperty);
        gnssState.bind(PropertyPath.from(gnssInfo).selectReadOnlyAsyncObject(IGnssInfo::gnssStateProperty));
        gnssQuality.bind(
            PropertyPath.from(gnssInfo).selectReadOnlyAsyncDouble(IGnssInfo::qualityPercentageProperty),
            qualityPercentage ->
                Double.isNaN(qualityPercentage.doubleValue()) ? null : Quantity.of(qualityPercentage, Unit.PERCENTAGE));

        gnssNumberOfSatellites.bind(
            PropertyPath.from(gnssInfo).selectReadOnlyAsyncInteger(IGnssInfo::numberOfSatellitesProperty));

        position.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::positionProperty));

        altitudeAglText.bind(
            Bindings.createStringBinding(
                () -> {
                    if (position.get() == null) {
                        return "--";
                    }

                    double altAboveTakeoff = position.get().getAltitude();
                    QuantityFormat altitudeAglFormat = new AdaptiveQuantityFormat(generalSettings);
                    altitudeAglFormat.setSignificantDigits(6);
                    altitudeAglFormat.setMaximumFractionDigits(1);
                    altitudeAglFormat.setMinimumFractionDigits(1);
                    return altitudeAglFormat.format(
                        Quantity.of(altAboveTakeoff, Unit.METER), UnitInfo.INVARIANT_LENGTH);
                },
                position,
                generalSettings.localeProperty(),
                generalSettings.systemOfMeasurementProperty()));

        latitudeText.bind(
            Bindings.createStringBinding(
                () -> {
                    if (position.get() == null) {
                        return "--";
                    }

                    return position.get().getLatitude().toFormattedDMSString(); // TODO add format
                },
                position,
                generalSettings.localeProperty(),
                generalSettings.systemOfMeasurementProperty()));
        longitudeText.bind(
            Bindings.createStringBinding(
                () -> {
                    if (position.get() == null) {
                        return "--";
                    }

                    return position.get().getLongitude().toFormattedDMSString(); // TODO add format
                },
                position,
                generalSettings.localeProperty(),
                generalSettings.systemOfMeasurementProperty()));

        // TODO bind to distance to drone (calculate distance between takeoff position and current position)
        // TODO (careful about units when numbers get large)
        distanceToDroneText.bind(
            Bindings.createStringBinding(
                () -> {
                    if (position.get() == null) {
                        return "--";
                    }

                    double altAboveTakeoff = position.get().getAltitude();
                    QuantityFormat altitudeAglFormat = new AdaptiveQuantityFormat(generalSettings);
                    altitudeAglFormat.setSignificantDigits(6);
                    altitudeAglFormat.setMaximumFractionDigits(1);
                    altitudeAglFormat.setMinimumFractionDigits(1);
                    return altitudeAglFormat.format(
                        Quantity.of(altAboveTakeoff, Unit.METER), UnitInfo.INVARIANT_LENGTH);
                },
                position,
                generalSettings.localeProperty(),
                generalSettings.systemOfMeasurementProperty()));

        QuantityFormat flightTimeFormat = new AdaptiveQuantityFormat(generalSettings);
        ReadOnlyAsyncObjectProperty<Duration> flightTime =
            PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::flightTimeProperty);

        // TODO bind to time until landing (calculate flight time remaining)
        timeUntilLandingText.bind(
            Bindings.createStringBinding(
                () ->
                    (flightTime.get() != null
                        ? flightTimeFormat.format(Quantity.of(flightTime.get().toMillis(), Unit.MILLISECOND))
                        : "--"),
                flightTime));

        flightTimeText.bind(
            Bindings.createStringBinding(
                () ->
                    (flightTime.get() != null
                        ? flightTimeFormat.format(Quantity.of(flightTime.get().toMillis(), Unit.MILLISECOND))
                        : "--"),
                flightTime));

        autoPilotState.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::autopilotStateProperty));

        // TODO bind to OA distance from drone
        oaDistanceText.bind(
            Bindings.createStringBinding(
                () -> {
                    if (position.get() == null) {
                        return "--";
                    }

                    double altAboveTakeoff = position.get().getAltitude();
                    QuantityFormat altitudeAglFormat = new AdaptiveQuantityFormat(generalSettings);
                    altitudeAglFormat.setSignificantDigits(6);
                    altitudeAglFormat.setMaximumFractionDigits(1);
                    altitudeAglFormat.setMinimumFractionDigits(1);
                    return altitudeAglFormat.format(
                        Quantity.of(altAboveTakeoff, Unit.METER), UnitInfo.INVARIANT_LENGTH);
                },
                position,
                generalSettings.localeProperty(),
                generalSettings.systemOfMeasurementProperty()));
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        drone.bind(flightScope.currentDroneProperty());
        flightSegment.bind(flightScope.flightSegmentProperty());
    }

    ReadOnlyProperty<String> batteryTextProperty() {
        return batteryText;
    }

    ReadOnlyProperty<String> gnssQualityTextProperty() {
        return gnssQualityText;
    }

    ReadOnlyProperty<String> altitudeAglTextProperty() {
        return altitudeAglText;
    }

    ReadOnlyProperty<String> distanceToDroneTextProperty() {
        return distanceToDroneText;
    }

    ReadOnlyProperty<String> latitudeTextProperty() {
        return latitudeText;
    }

    ReadOnlyProperty<String> longitudeTextProperty() {
        return longitudeText;
    }

    ReadOnlyProperty<FlightSegment> flightSegmentProperty() {
        return flightSegment;
    }

    Property<BatteryAlertLevel> alertBatteryLevelProperty() {
        return alertBatteryLevel;
    }

    ReadOnlyProperty<GnssState> gnssStateProperty() {
        return gnssState;
    }

    ReadOnlyProperty<Quantity<Dimension.Percentage>> gnssQualityProperty() {
        return gnssQuality;
    }

    ReadOnlyProperty<Number> gnssNumberOfSatellitesProperty() {
        return gnssNumberOfSatellites;
    }

    ReadOnlyAsyncBooleanProperty batteryTelemetryOldProperty() {
        return batteryTelemetryOld;
    }

    ReadOnlyAsyncBooleanProperty gnssTelemetryOldProperty() {
        return gnssTelemetryOld;
    }

    ReadOnlyAsyncBooleanProperty flightSegmentTelemetryOldProperty() {
        return flightSegmentTelemetryOld;
    }

    ReadOnlyAsyncBooleanProperty positionTelemetryOldProperty() {
        return positionTelemetryOld;
    }

    ReadOnlyBooleanProperty telemetryDetailsAvailableProperty() {
        return telemetryDetailsAvailable;
    }

}
