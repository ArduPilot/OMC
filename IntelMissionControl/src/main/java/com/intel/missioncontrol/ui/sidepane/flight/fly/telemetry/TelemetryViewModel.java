/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import com.google.inject.Inject;
import com.intel.missioncontrol.drone.AutopilotState;
import com.intel.missioncontrol.drone.BatteryAlertLevel;
import com.intel.missioncontrol.drone.DistanceSensor;
import com.intel.missioncontrol.drone.FlightSegment;
import com.intel.missioncontrol.drone.GnssState;
import com.intel.missioncontrol.drone.IBattery;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.IGnssInfo;
import com.intel.missioncontrol.drone.IObstacleAvoidance;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.QuantityFormat;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.measure.property.UIAsyncQuantityProperty;
import com.intel.missioncontrol.measure.property.UIQuantityPropertyMetadata;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.FlightPlanValidation;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.commands.FutureCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedDelegateCommand;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import java.time.Duration;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncDoubleProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;

public class TelemetryViewModel extends ViewModelBase {

    private final UIAsyncObjectProperty<BatteryAlertLevel> alertBatteryLevel = new UIAsyncObjectProperty<>(this);
    private final UIAsyncQuantityProperty<Dimension.Percentage> batteryRemainingCharge;
    private final UIAsyncQuantityProperty<Dimension.Voltage> batteryVoltage;
    private final UIAsyncStringProperty batteryText = new UIAsyncStringProperty(this);
    private final UIAsyncObjectProperty<FlightSegment> flightSegment = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<GnssState> gnssState = new UIAsyncObjectProperty<>(this);
    private final UIAsyncQuantityProperty<Dimension.Percentage> gnssQuality;
    private final UIAsyncStringProperty altitudeAglText = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty distanceToDroneText = new UIAsyncStringProperty(this);
    private final UIAsyncObjectProperty<Position> position = new UIAsyncObjectProperty<>(this);
    private final UIAsyncStringProperty timeUntilLandingText = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty flightTimeText = new UIAsyncStringProperty(this);
    private final UIAsyncObjectProperty<AutopilotState> autoPilotState = new UIAsyncObjectProperty<>(this);
    private final UIAsyncStringProperty oaDistanceText = new UIAsyncStringProperty(this);
    private final UIAsyncObjectProperty<DistanceSensor.AlertLevel> oaDistanceAlertLevel =
        new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<IObstacleAvoidance.Mode> obstacleAvoidanceMode =
        new UIAsyncObjectProperty<>(this);
    private final UIAsyncBooleanProperty hardwareOACapable = new UIAsyncBooleanProperty(true);
    private final UIAsyncDoubleProperty closestDistanceMeters =
        new UIAsyncDoubleProperty(this, new UIPropertyMetadata.Builder<Number>().initialValue(Double.NaN).create());
    private final UIAsyncBooleanProperty closestDistanceTelemetryOld = new UIAsyncBooleanProperty(this);
    private final UIAsyncObjectProperty<Duration> timeUntilLanding = new UIAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);
    private final ReadOnlyAsyncObjectProperty<FlightPlan> activeFlightPlan;
    private final ReadOnlyAsyncIntegerProperty activeWaypointIndex;
    private final UIAsyncBooleanProperty batteryTelemetryOld = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty gnssTelemetryOld = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty flightSegmentTelemetryOld = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty positionTelemetryOld = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty autopilotStateTelemetryOld = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty flightTimeTelemetryOld = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty oaDistanceTelemetryOld = new UIAsyncBooleanProperty(this);
    private final ParameterizedDelegateCommand<Object> showTelemetryDetailDialogCommand;
    private final FutureCommand showObstacleAvoidanceDialogCommand;
    private final UIAsyncBooleanProperty batteryButtonToggle = new UIAsyncBooleanProperty(this);
    private final Command batteryButtonCommand;
    private final BooleanProperty telemetryDetailsAvailable = new SimpleBooleanProperty(true);

    @SuppressWarnings("FieldCanBeLocal")
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @InjectScope
    private FlightScope flightScope;

    @Inject
    public TelemetryViewModel(
            ISettingsManager settingsManager,
            IQuantityStyleProvider quantityStyleProvider,
            IWWGlobes wwGlobes,
            IDialogService dialogService) {
        GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);
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
        // PropertyPath.from(drone).selectReadOnlyAsyncBoolean(IDrone::attitudeTelemetryOldProperty);

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

        batteryButtonCommand = new DelegateCommand(() -> batteryButtonToggle.set(!batteryButtonToggle.get()));

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

        gnssQuality =
            new UIAsyncQuantityProperty<>(
                this,
                new UIQuantityPropertyMetadata.Builder<Dimension.Percentage>()
                    .quantityStyleProvider(quantityStyleProvider)
                    .unitInfo(UnitInfo.PERCENTAGE)
                    .create());
        ReadOnlyAsyncObjectProperty<? extends IGnssInfo> gnssInfo =
            PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::gnssInfoProperty);
        gnssState.bind(PropertyPath.from(gnssInfo).selectReadOnlyAsyncObject(IGnssInfo::gnssStateProperty));
        gnssQuality.bind(
            PropertyPath.from(gnssInfo).selectReadOnlyAsyncDouble(IGnssInfo::qualityPercentageProperty),
            qualityPercentage ->
                Double.isNaN(qualityPercentage.doubleValue()) ? null : Quantity.of(qualityPercentage, Unit.PERCENTAGE));

        showTelemetryDetailDialogCommand =
            new ParameterizedDelegateCommand<>(
                payload -> {
                    telemetryDetailsAvailable.set(false);

                    Future<TelemetryDetailViewModel> dialog =
                        dialogService.requestDialogAsync(this, TelemetryDetailViewModel.class, false);

                    dialog.whenDone((v) -> telemetryDetailsAvailable.set(true));
                    dialog.whenSucceeded(
                        (v) -> {
                            telemetryDetailsAvailable.set(true);
                        });
                    dialog.whenFailed(
                        e -> {
                            telemetryDetailsAvailable.set(true);
                            // LOGGER.error("Error evaluating message from drone: " + message, e);
                            System.out.println("requestParamById Error: " + e.getMessage());
                        });
                },
                telemetryDetailsAvailable);

        showObstacleAvoidanceDialogCommand =
            new FutureCommand(
                () -> dialogService.requestDialogAsync(this, ObstacleAvoidanceTelemetryViewModel.class, false));

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

        // distance to drone: use takeoff point as reference
        ReadOnlyObjectProperty<Position> activeTakeoffPosition =
            PropertyPath.from(drone)
                .select(IDrone::activeFlightPlanProperty)
                .selectReadOnlyObject(FlightPlan::takeoffPositionProperty);

        Globe globe = wwGlobes.getDefaultGlobe();

        distanceToDroneText.bind(
            Bindings.createStringBinding(
                () -> {
                    Position pos = position.get();
                    Position activeTakeoffPos = activeTakeoffPosition.get();
                    if (pos == null || activeTakeoffPos == null) {
                        return "--";
                    }

                    Vec4 vPos = globe.computePointFromPosition(pos);
                    Vec4 vToff = globe.computePointFromPosition(activeTakeoffPos);
                    double d = vPos.distanceTo3(vToff);

                    QuantityFormat format = new AdaptiveQuantityFormat(generalSettings);
                    format.setSignificantDigits(6);
                    format.setMaximumFractionDigits(1);
                    format.setMinimumFractionDigits(1);
                    return format.format(Quantity.of(d, Unit.METER), UnitInfo.INVARIANT_LENGTH);
                },
                position,
                activeTakeoffPosition,
                generalSettings.localeProperty(),
                generalSettings.systemOfMeasurementProperty()));

        QuantityFormat flightTimeFormat = new AdaptiveQuantityFormat(generalSettings);
        ReadOnlyAsyncObjectProperty<Duration> flightTime =
            PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::flightTimeProperty);

        flightTimeText.bind(
            Bindings.createStringBinding(
                () ->
                    (flightTime.get() != null
                        ? flightTimeFormat.format(Quantity.of(flightTime.get().toMillis(), Unit.MILLISECOND))
                        : "--"),
                flightTime));

        activeFlightPlan = PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::activeFlightPlanProperty);
        activeWaypointIndex =
            PropertyPath.from(drone).selectReadOnlyAsyncInteger(IDrone::activeFlightPlanWaypointIndexProperty);

        activeFlightPlan.addListener(
            (o, oldValue, newValue) -> updateTimeUntilLanding(newValue, activeWaypointIndex.get()),
            Dispatcher.platform());
        activeWaypointIndex.addListener(
            (o, oldValue, newValue) -> updateTimeUntilLanding(activeFlightPlan.get(), newValue.intValue()),
            Dispatcher.platform());

        timeUntilLandingText.bind(
            Bindings.createStringBinding(
                () ->
                    (timeUntilLanding.get() != null
                        ? flightTimeFormat.format(Quantity.of(timeUntilLanding.get().toMillis(), Unit.MILLISECOND))
                        : "--"),
                timeUntilLanding));

        autoPilotState.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::autopilotStateProperty));

        oaDistanceAlertLevel.bind(
            PropertyPath.from(drone)
                .select(IDrone::obstacleAvoidanceProperty)
                .selectReadOnlyAsyncObject(
                    oa -> oa.getAggregatedDistanceSensor().alertLevelProperty(), DistanceSensor.AlertLevel.UNKNOWN));

        obstacleAvoidanceMode.addListener(
            ((observable, oldValue, newValue) -> {
                if (newValue == IObstacleAvoidance.Mode.NOT_AVAILABLE) {
                    oaDistanceText.set("CA OFF");
                    hardwareOACapable.setValue(false);
                } else {
                    hardwareOACapable.setValue(true);
                }
            }));

        obstacleAvoidanceMode.bind(
            PropertyPath.from(drone)
                .select(IDrone::obstacleAvoidanceProperty)
                .selectReadOnlyAsyncObject(oa -> oa.modeProperty()));

        closestDistanceMeters.bind(
            PropertyPath.from(drone)
                .select(IDrone::obstacleAvoidanceProperty)
                .selectReadOnlyAsyncDouble(
                    oa -> oa.getAggregatedDistanceSensor().closestDistanceMetersProperty(), Double.NaN));
        oaDistanceTelemetryOld.bind(
            PropertyPath.from(drone)
                .select(IDrone::obstacleAvoidanceProperty)
                .selectReadOnlyAsyncBoolean(oa -> oa.getAggregatedDistanceSensor().telemetryOldProperty()));

        oaDistanceText.bind(
            Bindings.createStringBinding(
                () -> {
                    double d = closestDistanceMeters.get();
                    if (Double.isNaN(d) || Double.isInfinite(d)) {
                        return "CA OFF";
                    }

                    QuantityFormat format = new AdaptiveQuantityFormat(generalSettings);
                    format.setSignificantDigits(6);
                    format.setMaximumFractionDigits(1);
                    format.setMinimumFractionDigits(1);
                    return format.format(Quantity.of(d, Unit.METER), UnitInfo.INVARIANT_LENGTH);
                },
                closestDistanceMeters,
                generalSettings.localeProperty(),
                generalSettings.systemOfMeasurementProperty()));
    }

    private void updateTimeUntilLanding(FlightPlan fp, int waypointIndex) {
        Duration d;
        if (fp == null) {
            d = null;
        } else {
            d = null;
            if (waypointIndex >= 0 && waypointIndex < fp.waypointsProperty().size()) {
                d =
                    Duration.ofSeconds(
                        Math.round(
                            FlightPlanValidation.estimateFlightTime(fp, fp.waypointsProperty().get(waypointIndex))));
            }
        }

        timeUntilLanding.set(d);
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

    ReadOnlyProperty<String> altitudeAglTextProperty() {
        return altitudeAglText;
    }

    ReadOnlyProperty<String> distanceToDroneTextProperty() {
        return distanceToDroneText;
    }

    ReadOnlyProperty<DistanceSensor.AlertLevel> oaDistanceAlertLevelProperty() {
        return oaDistanceAlertLevel;
    }

    ReadOnlyProperty<String> oaDistanceTextProperty() {
        return oaDistanceText;
    }

    ReadOnlyProperty<FlightSegment> flightSegmentProperty() {
        return flightSegment;
    }

    ReadOnlyProperty<String> timeUntilLandingTextProperty() {
        return timeUntilLandingText;
    }

    ReadOnlyProperty<String> flightTimeTextProperty() {
        return flightTimeText;
    }

    ReadOnlyProperty<AutopilotState> autoPilotStateProperty() {
        return autoPilotState;
    }

    Property<BatteryAlertLevel> alertBatteryLevelProperty() {
        return alertBatteryLevel;
    }

    ReadOnlyProperty<Quantity<Dimension.Percentage>> batteryRemainingChargeProperty() {
        return batteryRemainingCharge;
    }

    ReadOnlyProperty<GnssState> gnssStateProperty() {
        return gnssState;
    }

    ReadOnlyProperty<Quantity<Dimension.Percentage>> gnssQualityProperty() {
        return gnssQuality;
    }

    Command getBatteryButtonCommand() {
        return batteryButtonCommand;
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

    ReadOnlyAsyncBooleanProperty autopilotStateTelemetryOldProperty() {
        return autopilotStateTelemetryOld;
    }

    ReadOnlyAsyncBooleanProperty flightTimeTelemetryOldProperty() {
        return flightTimeTelemetryOld;
    }

    ReadOnlyAsyncBooleanProperty oaDistanceTelemetryOldProperty() {
        return oaDistanceTelemetryOld;
    }

    ReadOnlyBooleanProperty telemetryDetailsAvailableProperty() {
        return telemetryDetailsAvailable;
    }

    ParameterizedCommand getShowTelemetryDetailDialogCommand() {
        return showTelemetryDetailDialogCommand;
    }

    ReadOnlyProperty<IObstacleAvoidance.Mode> obstacleAvoidanceModeProperty() {
        return obstacleAvoidanceMode;
    }

    ReadOnlyAsyncBooleanProperty hardwareOACapableProperty() {
        return hardwareOACapable;
    }

    public Command getShowObstacleAvoidanceDialogCommand() {
        return showObstacleAvoidanceDialogCommand;
    }

}
