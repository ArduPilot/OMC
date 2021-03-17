/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.intel.missioncontrol.IEmergencyProcedure;
import com.intel.missioncontrol.drone.AlertLevel;
import com.intel.missioncontrol.drone.AutopilotState;
import com.intel.missioncontrol.drone.Battery;
import com.intel.missioncontrol.drone.FlightPlanWithWayPointIndex;
import com.intel.missioncontrol.drone.FlightSegment;
import com.intel.missioncontrol.drone.GnssInfo;
import com.intel.missioncontrol.drone.GnssState;
import com.intel.missioncontrol.drone.ICamera;
import com.intel.missioncontrol.drone.IDistanceSensor;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.IHealth;
import com.intel.missioncontrol.drone.IRemoteControl;
import com.intel.missioncontrol.drone.IStorage;
import com.intel.missioncontrol.drone.connection.IDroneConnectionExceptionListener;
import com.intel.missioncontrol.drone.connection.IDroneMessageListener;
import com.intel.missioncontrol.drone.legacy.PlaneHealth;
import com.intel.missioncontrol.drone.legacy.PlaneHealth.PlaneHealthChannel;
import com.intel.missioncontrol.drone.legacy.PlaneHealth.PlaneHealthChannelInfo;
import com.intel.missioncontrol.drone.legacy.PlaneHealth.PlaneHealthChannelStatus;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.airspace.Airspace;
import eu.mavinci.airspace.LowestAirspace;
import eu.mavinci.core.flightplan.CEvent;
import eu.mavinci.core.flightplan.GPSFixType;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneEventActions;
import eu.mavinci.core.plane.AirplaneFlightmode;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.CAirplaneCache;
import eu.mavinci.core.plane.UavCommand;
import eu.mavinci.core.plane.listeners.IAirplaneListenerAndroidState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackend;
import eu.mavinci.core.plane.listeners.IAirplaneListenerBackendConnectionLost;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConnectionEstablished;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConnectionState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDebug;
import eu.mavinci.core.plane.listeners.IAirplaneListenerExpertSimulatedFails;
import eu.mavinci.core.plane.listeners.IAirplaneListenerFlightphase;
import eu.mavinci.core.plane.listeners.IAirplaneListenerHealth;
import eu.mavinci.core.plane.listeners.IAirplaneListenerIsSimulation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerLinkInfo;
import eu.mavinci.core.plane.listeners.IAirplaneListenerMsg;
import eu.mavinci.core.plane.listeners.IAirplaneListenerName;
import eu.mavinci.core.plane.listeners.IAirplaneListenerOrientation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPhoto;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPlaneInfo;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPosition;
import eu.mavinci.core.plane.listeners.IAirplaneListenerPositionOrientation;
import eu.mavinci.core.plane.listeners.IAirplaneListenerSimulationSettings;
import eu.mavinci.core.plane.listeners.IAirplaneListenerStartPos;
import eu.mavinci.core.plane.listeners.ICommandListenerResult;
import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.Backend;
import eu.mavinci.core.plane.sendableobjects.BackendInfo;
import eu.mavinci.core.plane.sendableobjects.CommandResultData;
import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.LinkInfo;
import eu.mavinci.core.plane.sendableobjects.MVector;
import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.PhotoData;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.Port;
import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.plane.AirplaneCache;
import eu.mavinci.plane.IAirplane;
import eu.mavinci.plane.WindEstimate;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.util.Pair;
import org.apache.commons.lang3.NotImplementedException;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;

public class Drone
        implements IAirplaneListenerConnectionState,
            IAirplaneListenerFlightphase,
            IAirplaneListenerPlaneInfo,
            IAirplaneListenerHealth,
            IAirplaneListenerPosition,
            IAirplaneListenerOrientation,
            IAirplaneListenerDebug,
            IAirplaneListenerPositionOrientation,
            IAirplaneListenerLinkInfo,
            IAirplaneListenerPhoto,
            IAirplaneListenerBackend,
            IAirplaneListenerAndroidState,
            IAirplaneListenerName,
            IAirplaneListenerIsSimulation,
            IAirplaneListenerStartPos,
            IAirplaneListenerSimulationSettings,
            IAirplaneListenerConnectionEstablished,
            IAirplaneListenerBackendConnectionLost,
            IAirplaneListenerExpertSimulatedFails,
            IAirplaneListenerMsg,
            ICommandListenerResult,
            IDrone {

    private final AsyncObjectProperty<IPlatformDescription> platformDescription = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Battery> battery = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<GnssInfo> gnssInfo = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Position> position = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Quaternion> attitude = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<FlightSegment> flightSegment =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<FlightSegment>().initialValue(FlightSegment.UNKNOWN).create());
    private final AsyncObjectProperty<AutopilotState> autopilotState =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<AutopilotState>().initialValue(AutopilotState.AUTOPILOT).create());

    private final AsyncObjectProperty<Duration> flightTime =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<Duration>().initialValue(Duration.ZERO).create());

    // TODO: clean up below this

    private static final int SEND_BUTTON_TIMER_MILLISECOND = 25000;

    private Timer sendTimer = null;
    private final BooleanProperty sendInProgress = new SimpleBooleanProperty(false);

    private static final String NO_DATA = "--";
    /** Special 'temporary' const for hacking. Must be power of 2 in order to use bit mask. */
    private static final int NUMBER_OF_EVENT_SKIPPED = 64;

    private static final double GERMAN_STANDARD_MAX_HEIGHT = 100;

    private final ObjectProperty<AirplaneFlightphase> airplaneFlightPhase =
        new SimpleObjectProperty<>(AirplaneFlightphase.ground);
    private final ObjectProperty<AirplaneFlightmode> airplaneFlightMode =
        new SimpleObjectProperty<>(AirplaneFlightmode.ManualControl);
    private final ObjectProperty<AirplaneConnectorState> connectionState =
        new SimpleObjectProperty<>(AirplaneConnectorState.unconnected);
    private final ObjectProperty<IPlatformDescription> simulatedPlatformDescription = new SimpleObjectProperty<>();

    private final Property<String> connectorBatteryVoltageProperty = new SimpleStringProperty(NO_DATA);
    private final Property<Number> connectorBatteryVoltageValueProperty = new SimpleFloatProperty(0f);
    private final Property<String> connectorBatteryPercentageProperty = new SimpleStringProperty(NO_DATA);
    private final Property<Number> connectorBatteryPercentageValueProperty = new SimpleFloatProperty(0f);

    private final Property<Number> glonassProperty = new SimpleIntegerProperty(0);
    private final BooleanProperty isGpsLost = new SimpleBooleanProperty();

    private final IntegerProperty currentWaypointProperty = new SimpleIntegerProperty(-1);
    private final IntegerProperty motor1RpmProperty = new SimpleIntegerProperty(0);
    private final Property<String> groundSpeedProperty = new SimpleStringProperty(NO_DATA);

    private final Property<String> maxAltitudeProperty = new SimpleStringProperty(NO_DATA);
    private final DoublePropertyBase maxAltitudeValueProperty = new SimpleDoubleProperty(0.0);
    private final Property<String> altitudeRelationProperty = new SimpleStringProperty("-");
    private final Property<AlertLevel> altitudeAlertProperty = new SimpleObjectProperty<>();
    private final DoublePropertyBase recommendedAltValueProperty = new SimpleDoubleProperty();

    private final DoubleProperty windDirectionProperty = new SimpleDoubleProperty(0.0);
    private final Property<String> windSpeedProperty = new SimpleStringProperty(NO_DATA);

    private final Property<AirplaneConnectorState> connectionProperty =
        new SimpleObjectProperty<>(AirplaneConnectorState.unconnected);

    private final MapProperty<String, PlaneHealthChannelStatus> notImportantAlertsProperty =
        new SimpleMapProperty<>(FXCollections.emptyObservableMap());

    private final MapProperty<String, PlaneHealthChannelStatus> alertsProperty =
        new SimpleMapProperty<>(FXCollections.emptyObservableMap());

    private final Property<Number> lineOfSightProperty = new SimpleDoubleProperty(0.0);

    private final StringProperty planeName = new SimpleStringProperty("");

    private final ObjectProperty<PositionData> positionDataRaw = new SimpleObjectProperty<>();
    private final ObjectProperty<PositionOrientationData> positionOrientationDataRaw = new SimpleObjectProperty<>();
    private final ObjectProperty<OrientationData> orientationDataRaw = new SimpleObjectProperty<>();
    private final ObjectProperty<DebugData> debugDataRaw = new SimpleObjectProperty<>();
    private final ObjectProperty<LinkInfo> linkInfoRaw = new SimpleObjectProperty<>();
    private final ObjectProperty<PhotoData> photoDataRaw = new SimpleObjectProperty<>();
    private final ObjectProperty<BackendInfo> backendInfoRaw = new SimpleObjectProperty<>();
    private final ObjectProperty<AndroidState> androidStateRaw = new SimpleObjectProperty<>();

    private final BooleanProperty simulationRaw = new SimpleBooleanProperty();
    private final ObjectProperty<StartPositionRaw> startPositionRaw = new SimpleObjectProperty<>();
    private final ObjectProperty<SimulationSettingsRaw> simulationSettingsRaw = new SimpleObjectProperty<>();
    private final StringProperty connectionEstablishedRaw = new SimpleStringProperty();
    private final ObjectProperty<ConnectionLostReasons> backendConnectionlostRaw = new SimpleObjectProperty<>();
    private final ObjectProperty<ExpertSimulatedFailures> expertSimulatedFailures = new SimpleObjectProperty<>();
    private final ObjectProperty<IEmergencyProcedure> currentEmergencyAction =
        new SimpleObjectProperty<>(); // TODO: Set some default value.
    private final IntegerProperty uavMsgSeverityLevelProperty = new SimpleIntegerProperty();
    private final StringProperty uavMsgProperty = new SimpleStringProperty();
    private final ObjectProperty<CommandResultData> commandResultDataObjectProperty = new SimpleObjectProperty<>();

    private final IAirplane legacyPlane;
    private final AirplaneCache cache;

    private PlaneHealth health;
    private int eventCounter;
    private static final Map<Boolean, List<AirplaneEventActions>> DECISION_EVENT = getEmergencyProcedureOptions();
    private static final List<AirplaneEventActions> COPTER_EMERGENCY_PROCEDURES =
        setCopterEmergencyProceduresList(); // Stream.of(AirplaneEventActions.values()).filter(e ->
    // e.getDisplayNameKey().contains("Copter")).collect(Collectors.toList());
    private static final List<AirplaneEventActions> PLANE_EMERGENCY_PROCEDURES =
        setPlaneEmergencyProceduresList(); // Stream.of(AirplaneEventActions.values()).filter(e ->
    // !e.getDisplayNameKey().contains("Copter")).collect(Collectors.toList());

    public Drone(IAirplane legacyPlane) {
        Expect.notNull(legacyPlane, "legacyPlane");

        this.legacyPlane = legacyPlane;
        legacyPlane.addListener(this);
        this.cache = legacyPlane.getAirplaneCache();

        battery.setValue(new Battery());
        gnssInfo.setValue(new GnssInfo());
        platformDescription.setValue(legacyPlane.getHardwareConfiguration().getPlatformDescription());
        flightSegment.setValue(FlightSegment.UNKNOWN);
    }

    private static Map<Boolean, List<AirplaneEventActions>> getEmergencyProcedureOptions() {
        Map<Boolean, List<AirplaneEventActions>> eventDecision =
            Stream.of(AirplaneEventActions.values())
                .collect(Collectors.partitioningBy(e -> e.getDisplayNameKey().contains("Copter")));
        return eventDecision;
    }

    private static List<AirplaneEventActions> setCopterEmergencyProceduresList() {
        List<AirplaneEventActions> copterProcedureList = new ArrayList<>();
        copterProcedureList.addAll(DECISION_EVENT.get(true));
        return copterProcedureList;
    }

    private static List<AirplaneEventActions> setPlaneEmergencyProceduresList() {
        List<AirplaneEventActions> planeProcedureList = new ArrayList<>();
        planeProcedureList.addAll(DECISION_EVENT.get(true));
        return planeProcedureList;
    }

    public static Drone forLegacyPlane(IAirplane legacyPlane) {
        if (legacyPlane == null) {
            return null;
        }

        return new Drone(legacyPlane);
    }

    @Override
    public synchronized void recv_health(HealthData data) {
        if ((health == null) || (data == null)) {
            return;
        }

        // temporary solution to skip some events that come from UAV to UI
        if (isAllowedToUpdateUi()) {
            updateMainBattery(data);
            updateConnectorBattery(data);
            updateGpsQuality(data);
            updateGlonass(data);
            updateMotor1Rpm(data);
            updateHealthAlerts(data);
            updateWindFalcon(data);
        }

        incrementNumberOfReceivedEvents();
    }

    // Black magic of bit mask every `NUMBER_OF_EVENT_SKIPPED` eventCounter will be set to `0`
    private void incrementNumberOfReceivedEvents() {
        eventCounter = (eventCounter + 1) & NUMBER_OF_EVENT_SKIPPED - 1;
        // System.out.println("EventCounter: " + eventCounter);
    }

    private boolean isAllowedToUpdateUi() {
        return eventCounter == 0;
    }

    private void updateHealthAlerts(HealthData data) {
        final Map<String, PlaneHealthChannelStatus> statuses = health.getChannelStatusesWithAlert(data);
        final ObservableMap<String, PlaneHealthChannelStatus> statusesObservable =
            FXCollections.observableMap(statuses);

        final Map<String, PlaneHealthChannelStatus> notImportantStatuses =
            PlaneHealth.getNotImportantChannelStatusesWithAlert(statuses);
        final ObservableMap<String, PlaneHealthChannelStatus> notImportantStatusesObservable =
            FXCollections.observableMap(notImportantStatuses);

        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.runLater(
            () -> {
                alertsProperty.setValue(statusesObservable);
                notImportantAlertsProperty.setValue(notImportantStatusesObservable);
            });
    }

    private void updateGpsQuality(HealthData data) {
        final Pair<GPSFixType, AlertLevel> gpsType = extractGpsQuality(data);

        gnssInfo.get().gnssStateProperty().setValue(convertGpsFixType(gpsType.getKey()));
        gnssInfo.get()
            .qualityPercentageProperty()
            .setValue(getHealth().getChannelStatus(data, PlaneHealthChannel.GPS).getAbsolute() * 10.0);
    }

    private GnssState convertGpsFixType(GPSFixType gpsFixType) {
        switch (gpsFixType) {
        case noFix:
            return GnssState.NO_FIX;
        case gpsFix:
            return GnssState.GPS;
        case dgps:
            return GnssState.GPS;
        case PPS:
            return GnssState.UNKNOWN;
        case rtkFixedBL:
            return GnssState.RTK_FIXED;
        case rtkFloatingBL:
            return GnssState.RTK_FLOAT;
        case estimated:
            return GnssState.NO_FIX;
        case manualInput:
            return GnssState.NO_FIX;
        case simulation:
            return GnssState.NO_FIX;
        case unknown:
            return GnssState.UNKNOWN;
        case staticFixed:
            return GnssState.GPS;
        case PPP:
            return GnssState.UNKNOWN;
        default:
            return GnssState.UNKNOWN;
        }
    }

    private void updateFlightTime() {
        final var time =
            (positionOrientationDataRaw.getValue() != null && positionOrientationDataRaw.getValue().elapsed_time > 0)
                ? Duration.ofSeconds(positionOrientationDataRaw.getValue().elapsed_time)
                : null;
        flightTime.setValue(time);
    }

    private Pair<GPSFixType, AlertLevel> extractGpsQuality(HealthData data) {
        PlaneHealthChannelStatus gpsQualityStatus = getHealth().getChannelStatus(data, PlaneHealthChannel.GPS_QUALITY);

        if (gpsQualityStatus == null) {
            return new Pair<GPSFixType, AlertLevel>(GPSFixType.noFix, AlertLevel.RED);
        }

        Float gpsQualityOrdinal = gpsQualityStatus.getAbsolute();
        Expect.notNull(gpsQualityOrdinal, "gpsQualityOrdinal");

        if (!GPSFixType.isValid(gpsQualityOrdinal)) {
            return new Pair<GPSFixType, AlertLevel>(GPSFixType.noFix, AlertLevel.RED);
        }

        GPSFixType gpsFixType = GPSFixType.values()[gpsQualityOrdinal.intValue()];
        return new Pair<GPSFixType, AlertLevel>(gpsFixType, gpsQualityStatus.getAlert());
    }

    private void updateGlonass(HealthData data) {
        updateNumberAbsoluteProperty(data, PlaneHealthChannel.GLONASS, glonassProperty);
    }

    private void updateMotor1Rpm(HealthData data) {
        updateNumberAbsoluteProperty(data, PlaneHealthChannel.MOTOR1, motor1RpmProperty);
    }

    private void updateNumberAbsoluteProperty(
            HealthData data, PlaneHealthChannel channel, final Property<Number> property) {
        PlaneHealthChannelStatus status = getHealth().getChannelStatus(data, channel);

        if (status == null) {
            return;
        }

        final Float value = status.getAbsolute();

        if (value == null) {
            return;
        }

        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.runLater(
            () -> {
                property.setValue(value);
            });
    }

    private void updateMainBattery(HealthData data) {
        final PlaneHealthChannelStatus batteryStatus =
            getHealth().getChannelStatus(data, PlaneHealthChannel.BATTERY_MAIN);
        if (batteryStatus == null) {
            return;
        }

        final float voltageValue = batteryStatus.getAbsoluteValue();
        final float percentageValue = batteryStatus.getPercentValue();

        battery.get().voltageProperty().set(voltageValue);
        battery.get().remainingChargePercentageProperty().set(percentageValue);

        Bindings.createObjectBinding(
            () -> {
                Double remainingChargePercent = battery.get().remainingChargePercentageProperty().getValue();
                if (remainingChargePercent <= 10.0) {
                    return AlertLevel.RED;
                } else if (remainingChargePercent <= 25.0) {
                    return AlertLevel.YELLOW;
                } else {
                    return AlertLevel.GREEN;
                }
            },
            battery.get().remainingChargePercentageProperty());
    }

    // TODO
    private void updateConnectorBattery(HealthData data) {
        updateBattery(
            data,
            PlaneHealthChannel.BATTERY_CONNECTOR,
            connectorBatteryVoltageProperty,
            connectorBatteryPercentageProperty,
            connectorBatteryVoltageValueProperty,
            connectorBatteryPercentageValueProperty);
    }

    private void updateBattery(
            HealthData data,
            PlaneHealthChannel channel,
            Property<String> absoluteProperty,
            Property<String> percentageProperty,
            Property<Number> absoluteValueProperty,
            Property<Number> percentageValueProperty) {
        PlaneHealthChannelStatus batteryStatus = getHealth().getChannelStatus(data, channel);

        if (batteryStatus == null) {
            return;
        }

        final String voltage = batteryStatus.getAbsoluteAsString();
        final String percentage = batteryStatus.getPercentAsString();
        final float voltageValue = batteryStatus.getAbsoluteValue();
        final float percentageValue = batteryStatus.getPercentValue();

        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.runLater(
            () -> {
                absoluteProperty.setValue(voltage);
                percentageProperty.setValue(percentage);
                absoluteValueProperty.setValue(voltageValue);
                percentageValueProperty.setValue(percentageValue);
            });
    }

    @Override
    public synchronized void recv_planeInfo(PlaneInfo info) {
        health = new PlaneHealth(info);
    }

    @Override
    public synchronized void recv_position(PositionData position) {
        positionDataRaw.setValue(position);
        if (position == null) {
            return;
        }

        updateGroundSpeed(position);
        updateAltitude(position);
        updateWindSirius();
        updatePosition(position);
        updateFlightMode(position);
    }

    private void updateFlightMode(PositionData positionData) {
        Dispatcher.platform()
            .runLater(() -> airplaneFlightMode.set(AirplaneFlightmode.values()[positionData.flightmode]));
    }

    private void updateAirplaneFlightMode() {
        final int positionDataValue = positionDataRaw.getValue().flightmode;

        Dispatcher.platform()
            .runLater(() -> airplaneFlightMode.setValue(AirplaneFlightmode.values()[positionDataValue]));
    }

    private void updateAirplaneFlightPhase() {
        final AirplaneFlightphase airplaneFlightPhaseValue =
            AirplaneFlightphase.values()[positionDataRaw.getValue().flightphase];
        if (airplaneFlightPhaseValue != null) {
            Dispatcher.platform().runLater(() -> airplaneFlightPhase.setValue(airplaneFlightPhaseValue));
        }
    }

    private void updatePosition(PositionData position) {
        final Position currentPosition = getCurrentPosition(position);
        final double lineOfSight = getLineOfSight(currentPosition);

        this.position.setValue(currentPosition);

        Dispatcher.platform().runLater(() -> lineOfSightProperty.setValue(lineOfSight));
    }

    private Position getCurrentPosition(PositionData position) {
        if (cache == null) {
            return getCurrentPositionFromData(position);
        }

        try {
            return cache.getCurPos();
        } catch (AirplaneCacheEmptyException ex) {
            return getCurrentPositionFromData(position);
        }
    }

    private Position getCurrentPositionFromData(PositionData position) {
        return new Position(LatLon.fromDegrees(position.lat, position.lon), (position.altitude / 100.0));
    }

    private void updateWindSirius() {
        if (!isCopter()) {
            WindEstimate wind = legacyPlane.getWindEstimate();

            if (wind == null) {
                return;
            }

            final double windDirection = wind.phi;
            final String windSpeed = StringHelper.speedToIngName(wind.vW, 1, true);

            Dispatcher dispatcher = Dispatcher.platform();
            dispatcher.runLater(
                () -> {
                    windDirectionProperty.setValue(windDirection);
                    windSpeedProperty.setValue(windSpeed);
                });
        }
    }

    private void updateWindFalcon(HealthData data) {
        if (isCopter()) {
            PlaneHealthChannelStatus status = getHealth().getChannelStatus(data, PlaneHealthChannel.WIND_SPEED);

            if (status == null) {
                return;
            }

            final Float value = status.getAbsolute();

            if (value == null) {
                return;
            }

            final String windSpeed = StringHelper.speedToIngName(value.doubleValue(), 1, true);

            Dispatcher.platform().runLater(() -> windSpeedProperty.setValue(windSpeed));
            updateNumberAbsoluteProperty(data, PlaneHealthChannel.WIND_DIRECTION, windDirectionProperty);
        }
    }

    private void updateAltitude(PositionData position) {
        double maxAltitude = getMaxAltitude();
        final String maxAltitudeString = maxAltitude + "m";

        final double altitude = getAltitude(position);
        final String altitudeString = altitude + "m";

        final Pair<String, AlertLevel> altitudeRelationAndAlert = getAltitudeRelationAndAlert(altitude, maxAltitude);

        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.runLater(
            () -> {
                maxAltitudeProperty.setValue(maxAltitudeString);
                maxAltitudeValueProperty.setValue(maxAltitude);
                altitudeRelationProperty.setValue(altitudeRelationAndAlert.getKey());
                altitudeAlertProperty.setValue(altitudeRelationAndAlert.getValue());
                recommendedAltValueProperty.setValue(getTargetAltitude());
            });
    }

    private Pair<String, AlertLevel> getAltitudeRelationAndAlert(double altitude, double maxAltitude) {
        if (maxAltitude <= 5.0) {
            return new Pair<String, AlertLevel>(">", AlertLevel.RED);
        }

        if (altitude < (maxAltitude - Airspace.SAFETY_MARGIN_IN_METER)) {
            return new Pair<String, AlertLevel>("<", AlertLevel.GREEN);
        }

        if (altitude < maxAltitude) {
            return new Pair<String, AlertLevel>("<", AlertLevel.YELLOW);
        }

        return new Pair<String, AlertLevel>(">", AlertLevel.RED);
    }

    private double getAltitude(PositionData position) {
        if (cache == null) {
            return Double.NaN;
        }

        try {
            return cache.getCurPlaneElevOverGround();
        } catch (AirplaneCacheEmptyException ex) {
            System.out.println("EXCEPTION in altitude. Giving relative altitude");
            if (position != null) {
                return Math.round(position.altitude / 100.0);
            }
        }

        return Double.NaN;
    }

    private double getMaxAltitude() {
        LowestAirspace airspace = getAirspace();

        if (airspace == null) {
            return 0.0;
        }

        if (isCopter()) {
            return Math.min(airspace.getMinimalAltOverGround(), GERMAN_STANDARD_MAX_HEIGHT);
        } else {
            return airspace.getMinimalAltOverGround();
        }
    }

    private double getTargetAltitude() {
        if (cache == null) {
            return 0.0;
        }

        try {
            return cache.getTargetAltitude();
        } catch (AirplaneCacheEmptyException e) {
            return 0.0;
        }
    }

    private LowestAirspace getAirspace() {
        if (cache == null) {
            return null;
        }

        try {
            return cache.getMaxMAVAltitude();
        } catch (AirplaneCacheEmptyException ex) {
            Debug.getLog().log(Level.SEVERE, "Cannot get LowestAirspace from AirplaneCache", ex);
            return null;
        }
    }

    private void updateGroundSpeed(PositionData position) {
        final String groundSpeed = StringHelper.speedToIngName(position.groundspeed / 100.0, -3, true);
        Dispatcher.platform().runLater(() -> groundSpeedProperty.setValue(groundSpeed));
    }

    @Override
    public void connectionStateChange(AirplaneConnectorState newState) {
        if (newState == null) {
            newState = AirplaneConnectorState.unconnected;
            // TODO reset properties
        }

        final AirplaneConnectorState state = newState;

        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.runLater(
            () -> {
                connectionProperty.setValue(state);
                connectionState.setValue(state);
            });
    }

    public AirplaneFlightphase getAirplaneFlightPhase() {
        return airplaneFlightPhase.get();
    }

    public ObjectProperty<AirplaneFlightmode> airplaneFlightModeProperty() {
        return airplaneFlightMode;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IPlatformDescription> platformDescriptionProperty() {
        return platformDescription;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Battery> batteryProperty() {
        return battery;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IHealth> healthProperty() {
        return null;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IStorage> storageProperty() {
        return null;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IRemoteControl> remoteControlProperty() {
        return null;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<FlightPlan> activeFlightPlanProperty() {
        return null;
    }

    @Override
    public ReadOnlyAsyncIntegerProperty activeFlightPlanWaypointIndexProperty() {
        return null;
    }

    public ReadOnlyObjectProperty<AirplaneFlightphase> airplaneFlightPhaseProperty() {
        return airplaneFlightPhase;
    }

    public boolean getIsGpsLost() {
        return isGpsLost.get();
    }

    public BooleanProperty isGpsLostProperty() {
        return isGpsLost;
    }

    public ReadOnlyObjectProperty<AirplaneConnectorState> connectionStateProperty() {
        return connectionState;
    }

    public Property<AirplaneConnectorState> connectionProperty() {
        return connectionProperty;
    }

    public IAirplane getLegacyPlane() {
        return legacyPlane;
    }

    @Override
    public void recv_msg(Integer severityLevel, String data) {
        final int uavSeverityLevel = severityLevel;
        final String uavMsg = data;
        System.out.println("Drone: severity level and message are: " + uavSeverityLevel + ", " + uavMsg);
        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.runLater(
            () -> {
                uavMsgSeverityLevelProperty.setValue(uavSeverityLevel);
                uavMsgProperty.setValue(uavMsg);
            });
    }

    @Override
    public void recv_flightPhase(Integer fp) {
        AirplaneFlightphase afp = AirplaneFlightphase.values()[fp];
        FlightSegment flightSegment;
        switch (afp) {
        case ground:
            flightSegment = FlightSegment.ON_GROUND;
            break;
        case takeoff:
            flightSegment = FlightSegment.TAKEOFF;
            break;
        case airborne:
            flightSegment = FlightSegment.HOLD;
            break;
        case descending:
            flightSegment = FlightSegment.LANDING;
            break;
        case landing:
            flightSegment = FlightSegment.LANDING;
            break;
        case FixedOrientation:
            // TODO
            flightSegment = FlightSegment.UNKNOWN;
            break;
        case groundtest:
            // TODO
            flightSegment = FlightSegment.UNKNOWN;
            break;
        case returnhome:
            flightSegment = FlightSegment.PLAN_RUNNING;
            break;
        case gpsloss:
            // TODO
            flightSegment = FlightSegment.UNKNOWN;
            break;
        case waitingforgps:
            // TODO
            flightSegment = FlightSegment.UNKNOWN;
            break;
        case jumpToLanding:
            flightSegment = FlightSegment.UNKNOWN;
            break;
        case areaRestricted:
            // TODO
            flightSegment = FlightSegment.UNKNOWN;
            break;
        case startFlight:
            flightSegment = FlightSegment.PLAN_RUNNING;
            break;
        case holdPosition:
            flightSegment = FlightSegment.HOLD;
            break;
        default:
            throw new NotImplementedException("Invalid AirplaneFlightphase");
        }

        this.flightSegment.setValue(flightSegment);

        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.runLater(
            () -> {
                airplaneFlightPhase.set(AirplaneFlightphase.values()[fp]);
                isGpsLost.setValue(fp == AirplaneFlightphase.gpsloss.ordinal());
            });
        // flightSegment.set(AirplaneFlightphase.values()[fp]);
        // isGpsLost.setValue(fp == AirplaneFlightphase.gpsloss.ordinal());
    }

    public void setSimulatedPlatformDescription(IPlatformDescription simulatedPlatformDescription) {
        this.simulatedPlatformDescription.set(simulatedPlatformDescription);
    }

    public IPlatformDescription getSimulatedPlatformDescription() {
        return simulatedPlatformDescription.get();
    }

    public ObjectProperty<IPlatformDescription> simulatedPlatformDescriptionProperty() {
        return simulatedPlatformDescription;
    }

    public Property<String> connectorBatteryVoltageProperty() {
        return connectorBatteryVoltageProperty;
    }

    public Property<Number> connectorBatteryVoltageValueProperty() {
        return connectorBatteryVoltageValueProperty;
    }

    public Property<String> connectorBatteryPercentageProperty() {
        return connectorBatteryPercentageProperty;
    }

    public Property<Number> connectorBatteryPercentageValueProperty() {
        return connectorBatteryPercentageValueProperty;
    }

    public Property<Number> glonassProperty() {
        return glonassProperty;
    }

    public IntegerProperty motor1RpmProperty() {
        return motor1RpmProperty;
    }

    public Property<String> groundSpeedProperty() {
        return groundSpeedProperty;
    }

    public Property<String> maxAltitudeProperty() {
        return maxAltitudeProperty;
    }

    public DoublePropertyBase maxAltitudeValueProperty() {
        return maxAltitudeValueProperty;
    }

    public Property<String> altitudeRelationProperty() {
        return altitudeRelationProperty;
    }

    public Property<AlertLevel> altitudeAlertProperty() {
        return altitudeAlertProperty;
    }

    public DoublePropertyBase recommendedAltValueProperty() {
        return recommendedAltValueProperty;
    }

    public DoubleProperty windDirectionProperty() {
        return windDirectionProperty;
    }

    public Property<String> windSpeedProperty() {
        return windSpeedProperty;
    }

    public MapProperty<String, PlaneHealthChannelStatus> notImportantAlertsProperty() {
        return notImportantAlertsProperty;
    }

    public MapProperty<String, PlaneHealthChannelStatus> alertsProperty() {
        return alertsProperty;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Position> positionProperty() {
        return position;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty positionTelemetryOldProperty() {
        return null;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Quaternion> attitudeProperty() {
        return attitude;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty attitudeTelemetryOldProperty() {
        return null;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<FlightSegment> flightSegmentProperty() {
        return flightSegment;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty flightSegmentTelemetryOldProperty() {
        return null;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Duration> flightTimeProperty() {
        return flightTime;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty flightTimeTelemetryOldProperty() {
        return null;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<GnssInfo> gnssInfoProperty() {
        return gnssInfo;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<AutopilotState> autopilotStateProperty() {
        return autopilotState;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty autopilotStateTelemetryOldProperty() {
        return null;
    }

    @Override
    public ReadOnlyAsyncListProperty<? extends ICamera> camerasProperty() {
        return null;
    }

    @Override
    public ReadOnlyAsyncListProperty<? extends IDistanceSensor> distanceSensorsProperty() {
        return null;
    }

    @Override
    public void addListener(IDroneConnectionExceptionListener droneConnectionExceptionListener) {}

    @Override
    public void removeListener(IDroneConnectionExceptionListener droneConnectionExceptionListener) {}

    @Override
    public void addListener(IDroneMessageListener droneMessageListener) {}

    @Override
    public void removeListener(IDroneMessageListener droneMessageListener) {}

    public Property<Number> lineOfSightProperty() {
        return lineOfSightProperty;
    }

    public synchronized PlaneHealth getHealth() {
        return health;
    }

    public ObjectProperty<PositionData> positionDataRawProperty() {
        return positionDataRaw;
    }

    public ObjectProperty<PositionOrientationData> positionOrientationDataRawProperty() {
        return positionOrientationDataRaw;
    }

    public ObjectProperty<OrientationData> orientationDataRawproperty() {
        return orientationDataRaw;
    }

    public ObjectProperty<DebugData> debugDataRawProperty() {
        return debugDataRaw;
    }

    public ObjectProperty<LinkInfo> linkInfoRawProperty() {
        return linkInfoRaw;
    }

    public ObjectProperty<PhotoData> photoDataRawProperty() {
        return photoDataRaw;
    }

    public ObjectProperty<BackendInfo> backendInfoRawProperty() {
        return backendInfoRaw;
    }

    public ObjectProperty<AndroidState> androidStateRawproperty() {
        return androidStateRaw;
    }

    public ObjectProperty<ConnectionLostReasons> backendConnectionLostRawProperty() {
        return backendConnectionlostRaw;
    }

    public StringProperty connectionEstablishedRawProperty() {
        return connectionEstablishedRaw;
    }

    public BooleanProperty simulationRawProperty() {
        return simulationRaw;
    }

    public ObjectProperty<SimulationSettingsRaw> simulationSettingsRawProperty() {
        return simulationSettingsRaw;
    }

    public ObjectProperty<StartPositionRaw> startPositionRawProperty() {
        return startPositionRaw;
    }

    public ObjectProperty<ExpertSimulatedFailures> expertSimulatedFailuresproperty() {
        return expertSimulatedFailures;
    }

    public StringProperty planeNameProperty() {
        return planeName;
    }

    public ObjectProperty<CommandResultData> commandResultDataObjectProperty() {
        return commandResultDataObjectProperty;
    }

    public IntegerProperty getCurrentWaypointProperty() {
        return currentWaypointProperty;
    }

    public CEvent getLastFailEvent() {
        if (cache != null) {
            try {
                CEvent lastFailEvent = cache.getLastFailEvent();

                if (lastFailEvent != null) {
                    return lastFailEvent;
                }
            } catch (AirplaneCacheEmptyException ex) {
                // Do not process
            }
        }

        return null;
    }

    public LatLon getCurrentPositionLatLon() throws AirplaneCacheEmptyException {
        if (cache == null) {
            return null;
        }

        return cache.getCurLatLon();
    }

    public double getCurrentElevation() throws AirplaneCacheEmptyException {
        if (cache == null) {
            return 0.0;
        }

        return cache.getCurPlaneElevOverGround();
    }

    public double getAirborneTimeSeconds() {
        if (cache == null) {
            return 0.0;
        }

        return cache.getAirborneTime();
    }

    public String getTotalTime() {
        Flightplan fp = legacyPlane.getFPmanager().getOnAirFlightplan();
        if (fp != null) {
            double speed =
                fp.getHardwareConfiguration()
                    .getPlatformDescription()
                    .getMaxPlaneSpeed()
                    .convertTo(Unit.METER_PER_SECOND)
                    .getValue()
                    .doubleValue();
            double length = fp.getLengthInMeter();
            return StringHelper.secToShortDHMS(length / speed);
        }

        return "";
    }

    public String getTimeLeft() {
        Flightplan fp = legacyPlane.getFPmanager().getOnAirFlightplan();
        if (fp == null) {
            return "0:00";
        }

        double progress = getFlightPlanProgress();

        if (progress > 1.0) {
            progress = 1.0;
        } else if (progress < 0.0) {
            progress = 0.0;
        }

        double timeLeft = fp.getTimeInSec() * (1.0 - progress);
        return StringHelper.secToShortDHMS(timeLeft);
    }

    public PlaneHealthChannelInfo getHealthChannelInfo(PlaneHealthChannel channel) {
        PlaneHealth health = getHealth();
        if (health == null) {
            return null;
        }

        return health.getChannelInfo(channel);
    }

    public AlertLevel getHealthAlert(Number healthValue, PlaneHealthChannel channel) {
        PlaneHealthChannelInfo healthInfo = getHealthChannelInfo(channel);

        if (healthInfo == null) {
            return AlertLevel.RED;
        }

        return healthInfo.getAlert(healthValue);
    }

    public double getFlightPlanProgress() {
        if (cache == null) {
            return 0.0;
        }

        if (isCopter()) {}

        try {
            return cache.getFpProgress();
        } catch (AirplaneCacheEmptyException ex) {
            return 0.0;
        }
    }

    public double getMaxLineOfSight() {
        return legacyPlane
            .getHardwareConfiguration()
            .getPlatformDescription()
            .getMaxLineOfSight()
            .convertTo(Unit.METER)
            .getValue()
            .doubleValue();
    }

    private double getLineOfSight(Position currentPosition) {
        LatLon startPosition = getStartPosition();

        if ((startPosition == null) || (currentPosition == null)) {
            return 0.0;
        }

        return CAirplaneCache.distanceMeters(
            startPosition.getLatitude().getDegrees(),
            startPosition.getLongitude().getDegrees(),
            currentPosition.getLatitude().getDegrees(),
            currentPosition.getLongitude().getDegrees());
    }

    private LatLon getStartPosition() {
        if (cache == null) {
            return null;
        }

        try {
            return cache.getStartPos();
        } catch (AirplaneCacheEmptyException ex) {
            return null;
        }
    }

    public IPlatformDescription getPlatformDescription() {
        Flightplan flightPlan = getOnAirFlightplan();

        if (flightPlan == null) {
            return null;
        }

        return flightPlan.getHardwareConfiguration().getPlatformDescription();
    }

    private Flightplan getOnAirFlightplan() {
        return legacyPlane.getFPmanager().getOnAirFlightplan();
    }

    @Override
    public void recv_androidState(AndroidState state) {
        androidStateRaw.setValue(state);
    }

    @Override
    public void recv_backend(Backend host, MVector<Port> ports) {
        BackendInfo info = null;
        if (host != null) {
            info = host.info;
        }

        backendInfoRaw.setValue(info);
    }

    @Override
    public void recv_debug(DebugData d) {
        debugDataRaw.setValue(d);
    }

    @Override
    public void recv_linkInfo(LinkInfo li) {
        linkInfoRaw.setValue(li);
    }

    @Override
    public void recv_orientation(OrientationData o) {
        orientationDataRaw.setValue(o);
        updateFlightTime();
    }

    @Override
    public void recv_photo(PhotoData photo) {
        photoDataRaw.setValue(photo);
    }

    @Override
    public void recv_positionOrientation(PositionOrientationData po) {
        positionOrientationDataRaw.setValue(po);
        if (po == null) {
            return;
        }

        if (po.reentrypoint >= 0) {
            updateCurrentWaypoint(po);
        }
    }

    private void updateCurrentWaypoint(PositionOrientationData po) {
        final int currentWaypoint = po.reentrypoint;
        Dispatcher.platform().runLater(() -> currentWaypointProperty.setValue(currentWaypoint));
    }

    @Override
    public void recv_nameChange(String name) {
        planeName.setValue(name);
    }

    @Override
    public void recv_isSimulation(Boolean sim) {
        simulationRaw.setValue(sim);
    }

    @Override
    public void recv_startPos(Double lon, Double lat, Integer pressureZero) {
        startPositionRaw.setValue(new StartPositionRaw(lat, lon, pressureZero));
    }

    @Override
    public void recv_simulationSpeed(Float speed) {
        SimulationSettingsRaw oldSettings = simulationSettingsRaw.get();
        SimulationSettingsRaw newSettings;
        if (oldSettings == null) {
            newSettings = new SimulationSettingsRaw(speed, null);
        } else {
            newSettings = new SimulationSettingsRaw(speed, oldSettings.getSettings());
        }

        simulationSettingsRaw.setValue(newSettings);
    }

    @Override
    public void recv_simulationSettings(SimulationSettings settings) {
        SimulationSettingsRaw oldSettings = simulationSettingsRaw.get();
        SimulationSettingsRaw newSettings;
        if (oldSettings == null) {
            newSettings = new SimulationSettingsRaw(null, settings);
        } else {
            newSettings = new SimulationSettingsRaw(oldSettings.getSpeed(), settings);
        }

        simulationSettingsRaw.setValue(newSettings);
    }

    @Override
    public void recv_connectionEstablished(String port) {
        connectionEstablishedRaw.setValue(port);
    }

    @Override
    public void err_backendConnectionLost(ConnectionLostReasons reason) {
        backendConnectionlostRaw.setValue(reason);
    }

    @Override
    public void recv_simulatedFails(Integer failBitMask, Integer debug1, Integer debug2, Integer debug3) {
        final ExpertSimulatedFailures expertSimulatedFailuresObj =
            new ExpertSimulatedFailures(failBitMask, debug1, debug2, debug3);
        expertSimulatedFailures.setValue(expertSimulatedFailuresObj);
    }

    @Override
    public void recv_cmd_result(CommandResultData resultData) {
        System.out.println(
            "I am in Drone.java. Result is: "
                + resultData.uavCommand.getDisplayName()
                + "; "
                + resultData.uavCommandResult.getDisplayName()
                + "; "
                + resultData.otherInformation);
        if (resultData == null) {
            return;
        }

        updateCommandResult(resultData);
    }

    private void updateCommandResult(CommandResultData resultData) {
        Dispatcher.platform().runLater(() -> commandResultDataObjectProperty.setValue(resultData));
    }

    public static class StartPositionRaw {

        private Double lat;
        private Double lon;
        private Integer pressureZero;

        public StartPositionRaw(Double lat, Double lon, Integer pressureZero) {
            this.lat = lat;
            this.lon = lon;
            this.pressureZero = pressureZero;
        }

        public Double getLat() {
            return lat;
        }

        public Double getLon() {
            return lon;
        }

        public Integer getPressureZero() {
            return pressureZero;
        }
    }

    public static class SimulationSettingsRaw {

        private Float speed;
        private SimulationSettings settings;

        public SimulationSettingsRaw(Float speed, SimulationSettings settings) {
            this.speed = speed;
            this.settings = settings;
        }

        public Float getSpeed() {
            return speed;
        }

        public SimulationSettings getSettings() {
            return settings;
        }
    }

    public static class ExpertSimulatedFailures {

        private Integer failBitMask;
        private Integer debug1;
        private Integer debug2;
        private Integer debug3;

        public ExpertSimulatedFailures(Integer failBitMask, Integer debug1, Integer debug2, Integer debug3) {
            this.debug1 = debug1;
            this.debug2 = debug2;
            this.debug3 = debug3;
            this.failBitMask = failBitMask;
        }

        public Integer getFailBitMask() {
            return failBitMask;
        }

        public Integer getDebug1() {
            return debug1;
        }

        public Integer getDebug2() {
            return debug2;
        }

        public Integer getDebug3() {
            return debug3;
        }
    }

    public OrientationData getOrientationFromCache() {
        if (cache == null) {
            return null;
        }

        try {
            return cache.getOrientation();
        } catch (AirplaneCacheEmptyException ex) {
            return null;
        }
    }

    public boolean isCopter() {
        IPlatformDescription platformDescription = getPlatformDescription();

        if (platformDescription == null) {
            return false;
        }

        return platformDescription.isInCopterMode();
    }

    public void setEmergencyAction(IEmergencyProcedure procedure) {
        // set the emergency procedure in the object property
        currentEmergencyAction.setValue(procedure);
        // TODO: and also set it in the plane
        // getLegacyPlane().setEmergencyAction(()
    }

    // This value will be set based on the value chosen by the user.
    public ObjectProperty<IEmergencyProcedure> getCurrentEmergencyAction() {
        return currentEmergencyAction;
    }

    public List<AirplaneEventActions> getCopterEmergencyProcedures() {
        return COPTER_EMERGENCY_PROCEDURES;
    }

    public List<AirplaneEventActions> getPlaneEmergencyProcedures() {
        return PLANE_EMERGENCY_PROCEDURES;
    }

    public IntegerProperty getUavMsgSeverityLevelProperty() {
        return uavMsgSeverityLevelProperty;
    }

    public StringProperty getUavMsgProperty() {
        return uavMsgProperty;
    }

    @Override
    public Future<Void> takeOffAsync(FlightPlanWithWayPointIndex flightPlanWithWayPointIndex) {
        return Dispatcher.platform()
            .runLaterAsync(() -> getLegacyPlane().setFlightPhase(AirplaneFlightphase.takeoff))
            .whenSucceeded(() -> flightSegment.setValue(FlightSegment.TAKEOFF));
    }

    public Future<Void> abortTakeOffAsync() {
        return Dispatcher.platform()
            .runLaterAsync(() -> getLegacyPlane().setFlightPhase(AirplaneFlightphase.holdPosition))
            .whenSucceeded(
                () -> {
                    // TODO: abort takeoff
                    flightSegment.setValue(FlightSegment.HOLD);
                });
    }

    public Future<Void> landAsync() {
        return Dispatcher.platform()
            .runLaterAsync(() -> getLegacyPlane().setFlightPhase(AirplaneFlightphase.jumpToLanding))
            .whenSucceeded(() -> flightSegment.setValue(FlightSegment.LANDING));
    }

    public Future<Void> abortLandingAsync() {
        return Dispatcher.platform()
            .runLaterAsync(() -> getLegacyPlane().setFlightPhase(AirplaneFlightphase.holdPosition))
            .whenSucceeded(
                (v) -> {
                    // TODO: abort landing
                    flightSegment.setValue(FlightSegment.HOLD);
                });
    }

    @Override
    public Future<Void> startFlightPlanAsync(FlightPlanWithWayPointIndex flightPlanWithWayPointIndex) {
        return Dispatcher.platform()
            .runLaterAsync(() -> getLegacyPlane().setFlightPhase(AirplaneFlightphase.startFlight))
            .whenSucceeded(
                (v) -> {
                    // TODO: start flight plan
                    flightSegment.setValue(FlightSegment.PLAN_RUNNING);
                });
    }

    public Future<Void> pauseFlightPlanAsync() {
        return Dispatcher.platform()
            .runLaterAsync(() -> getLegacyPlane().setFlightPhase(AirplaneFlightphase.holdPosition))
            .whenSucceeded(
                (v) -> {
                    // TODO: pause flightplan
                    flightSegment.setValue(FlightSegment.HOLD);
                });
    }

    public Future<Void> resumeFlightPlanAsync() {
        return Dispatcher.platform()
            .runLaterAsync(() -> getLegacyPlane().setFlightPhase(AirplaneFlightphase.startFlight))
            .whenSucceeded(
                (v) -> {
                    // TODO: resume flight plan
                    flightSegment.setValue(FlightSegment.PLAN_RUNNING);
                });
    }

    public Future<Void> returnHomeAsync() {
        return Dispatcher.platform()
            .runLaterAsync(() -> getLegacyPlane().setFlightPhase(AirplaneFlightphase.returnhome))
            .whenSucceeded(
                (v) -> {
                    // TODO: return home
                    flightSegment.setValue(FlightSegment.PLAN_RUNNING);
                });
    }

    @Override
    public Future<Void> setActiveFlightPlanAsync(FlightPlanWithWayPointIndex flightPlan) {
        return null;
    }

    public BooleanProperty sendInProgressProperty() {
        return sendInProgress;
    }

    private void sendFlightPlan() {
        commandResultDataObjectProperty().set(null);
        // TODO
        // getLegacyPlane().getFPmanager().sendFP(currentFlightPlanSelection.get().getLegacyFlightplan(), false);
        sendInProgressProperty().set(true);
        sendTimer = new Timer();

        TimerTask sendTask =
            new TimerTask() {
                public void run() {
                    Dispatcher dispatcher = Dispatcher.platform();
                    dispatcher.runLater(
                        () -> {
                            sendInProgressProperty().set(false);
                            if (sendTimer != null) {
                                sendTimer.cancel();
                                sendTimer = null;
                            }
                        });
                }

            };

        sendTimer.schedule(sendTask, SEND_BUTTON_TIMER_MILLISECOND);
        commandResultDataObjectProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null && newValue.uavCommand == UavCommand.SEND_MISSION) {
                        switch (newValue.uavCommandResult) {
                        case SUCCESS:
                            System.out.println("-------received send ack SUCCESS!!!");
                            break;
                        case ERROR:
                            System.out.println("-------received send ack error :(");
                            break;
                        case DENIED:
                            System.out.println("-------received send ack denied :(");
                            break;
                        case INVALID:
                            System.out.println("-------received send ack invalid :(");
                            break;
                        case OTHER:
                            System.out.println("-------received send ack OTHER error");
                            break;
                        case TIMEOUT:
                        default:
                            System.out.println("-------received send ack timeout :(");
                            break;
                        }

                        sendInProgressProperty().set(false);
                        if (sendTimer != null) {
                            sendTimer.cancel();
                            sendTimer = null;
                        }
                    }
                });
    }
}
