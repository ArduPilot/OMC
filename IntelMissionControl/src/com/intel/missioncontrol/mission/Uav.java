/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.mission;

import com.intel.missioncontrol.IEmergencyProcedure;
import com.intel.missioncontrol.helper.Expect;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.ui.sidepane.flight.AlertLevel;
import com.intel.missioncontrol.ui.sidepane.flight.PlaneHealth;
import com.intel.missioncontrol.ui.sidepane.flight.PlaneHealth.PlaneHealthChannel;
import com.intel.missioncontrol.ui.sidepane.flight.PlaneHealth.PlaneHealthChannelInfo;
import com.intel.missioncontrol.ui.sidepane.flight.PlaneHealth.PlaneHealthChannelStatus;
import eu.mavinci.airspace.Airspace;
import eu.mavinci.airspace.LowestAirspace;
import eu.mavinci.core.flightplan.CEvent;
import eu.mavinci.core.flightplan.GPSFixType;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.AirplaneEventActions;
import eu.mavinci.core.plane.AirplaneFlightmode;
import eu.mavinci.core.plane.AirplaneFlightphase;
import eu.mavinci.core.plane.CAirplaneCache;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
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

import java.util.Map;
import java.util.logging.Level;

public class Uav
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
            ICommandListenerResult  {

    private static final String NO_DATA = "--";
    /** Special 'temporary' const for hacking. Must be power of 2 in order to use bit mask. */
    private static final int NUMBER_OF_EVENT_SKIPPED = 64;

    private static final double GERMAN_STANDARD_MAX_HEIGHT = 100;

    private final ObjectProperty<AirplaneFlightphase> flightPhase = new SimpleObjectProperty<>(AirplaneFlightphase.ground);
    private final ObjectProperty<AirplaneFlightmode> flightMode = new SimpleObjectProperty<>(AirplaneFlightmode.ManualControl);
    private final ObjectProperty<AirplaneConnectorState> connectionState = new SimpleObjectProperty<>(AirplaneConnectorState.unconnected);
    private final ObjectProperty<IPlatformDescription> simulatedPlatformDescription = new SimpleObjectProperty<>();

    private final Property<String> batteryVoltageProperty = new SimpleStringProperty(NO_DATA);
    private final Property<Number> batteryVoltageValueProperty = new SimpleFloatProperty(0f);
    private final Property<String> batteryPercentageProperty = new SimpleStringProperty(NO_DATA);
    private final Property<Number> batteryPercentageValueProperty = new SimpleFloatProperty(-1f);

    private final Property<String> connectorBatteryVoltageProperty = new SimpleStringProperty(NO_DATA);
    private final Property<Number> connectorBatteryVoltageValueProperty = new SimpleFloatProperty(0f);
    private final Property<String> connectorBatteryPercentageProperty = new SimpleStringProperty(NO_DATA);
    private final Property<Number> connectorBatteryPercentageValueProperty = new SimpleFloatProperty(0f);

    private final Property<Number> gpsProperty = new SimpleIntegerProperty(0);
    private final Property<Number> glonassProperty = new SimpleIntegerProperty(0);
    private final Property<GPSFixType> gpsQualityProperty = new SimpleObjectProperty<>(GPSFixType.noFix);
    private final Property<AlertLevel> gpsQualityAlertProperty = new SimpleObjectProperty<>();
    private final BooleanProperty isGpsLost = new SimpleBooleanProperty();

    private final IntegerProperty currentWaypointProperty = new SimpleIntegerProperty(-1);
    private final IntegerProperty motor1RpmProperty = new SimpleIntegerProperty(0);
    private final Property<String> groundSpeedProperty = new SimpleStringProperty(NO_DATA);

    private final Property<String> altitudeProperty = new SimpleStringProperty(NO_DATA);
    private final DoublePropertyBase altitudeValueProperty = new SimpleDoubleProperty(0.0);
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

    private final Property<Position> positionProperty = new SimpleObjectProperty<>(Position.ZERO);
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
    private final StringProperty flightTime = new SimpleStringProperty();
    private final ObjectProperty<IEmergencyProcedure> currentEmergencyAction = new SimpleObjectProperty<>(); //TODO: Set some default value.
    private final IntegerProperty uavMsgSeverityLevelProperty = new SimpleIntegerProperty();
    private final StringProperty uavMsgProperty = new SimpleStringProperty();
    private final ObjectProperty<CommandResultData> commandResultDataObjectProperty = new SimpleObjectProperty<>();

    private final IAirplane legacyPlane;
    private final AirplaneCache cache;

    private PlaneHealth health;
    private int eventCounter;
    private static final Map<Boolean, List<AirplaneEventActions>> DECISION_EVENT = getEmergencyProcedureOptions();
    private static final List<AirplaneEventActions> COPTER_EMERGENCY_PROCEDURES = setCopterEmergencyProceduresList();//Stream.of(AirplaneEventActions.values()).filter(e -> e.getDisplayNameKey().contains("Copter")).collect(Collectors.toList());
    private static final List<AirplaneEventActions> PLANE_EMERGENCY_PROCEDURES = setPlaneEmergencyProceduresList(); //Stream.of(AirplaneEventActions.values()).filter(e -> !e.getDisplayNameKey().contains("Copter")).collect(Collectors.toList());

    public Uav(IAirplane legacyPlane) {
        Expect.notNull(legacyPlane, "legacyPlane");

        this.legacyPlane = legacyPlane;
        legacyPlane.addListener(this);
        this.cache = legacyPlane.getAirplaneCache();
    }


    private static Map<Boolean,List<AirplaneEventActions>> getEmergencyProcedureOptions(){
        Map<Boolean, List<AirplaneEventActions>> eventDecision = Stream.of(AirplaneEventActions.values()).collect(Collectors.partitioningBy(e -> e.getDisplayNameKey().contains("Copter")));
        return eventDecision;
    }

    private static List<AirplaneEventActions> setCopterEmergencyProceduresList(){
        List<AirplaneEventActions> copterProcedureList = new ArrayList<>();
        copterProcedureList.addAll(DECISION_EVENT.get(true));
        return copterProcedureList;
    }

    private static List<AirplaneEventActions>setPlaneEmergencyProceduresList(){
        List<AirplaneEventActions> planeProcedureList = new ArrayList<>();
        planeProcedureList.addAll(DECISION_EVENT.get(true));
        return planeProcedureList;
    }
    public static Uav forLegacyPlane(IAirplane legacyPlane) {
        if (legacyPlane == null) {
            return null;
        }

        return new Uav(legacyPlane);
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
            updateGps(data);
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
        //System.out.println("EventCounter: " + eventCounter);
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

        Platform.runLater(
            () -> {
                alertsProperty.setValue(statusesObservable);
                notImportantAlertsProperty.setValue(notImportantStatusesObservable);
            });
    }

    private void updateGpsQuality(HealthData data) {
        final Pair<GPSFixType, AlertLevel> gpsType = extractGpsQuality(data);

        Platform.runLater(
            () -> {
                gpsQualityProperty.setValue(gpsType.getKey());
                gpsQualityAlertProperty.setValue(gpsType.getValue());
            });
    }


    private void updateFlightTime(){
        final String flightTime = (positionOrientationDataRaw.getValue() != null && positionOrientationDataRaw.getValue().elapsed_time > 0)?
                StringHelper.secToShortDHMS(positionOrientationDataRaw.getValue().elapsed_time) : "0:00";
            Platform.runLater((() -> flightTimeProperty().setValue(flightTime)));
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

    private void updateGps(HealthData data) {
        updateNumberAbsoluteProperty(data, PlaneHealthChannel.GPS, gpsProperty);
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

        Platform.runLater(
            () -> {
                property.setValue(value);
            });
    }

    private void updateMainBattery(HealthData data) {
        updateBattery(
            data,
            PlaneHealthChannel.BATTERY_MAIN,
            batteryVoltageProperty,
            batteryPercentageProperty,
            batteryVoltageValueProperty,
            batteryPercentageValueProperty);
    }

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

        Platform.runLater(
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
        Platform.runLater(
                () ->  {flightMode.set(AirplaneFlightmode.values()[positionData.flightmode]);}
        );

    }

    private void updateFlightMode() {
        final int positionDataValue = positionDataRaw.getValue().flightmode;

        Platform.runLater(
                () -> {
                    flightMode.setValue(AirplaneFlightmode.values()[positionDataValue]);

                }
        );
    }

    private void updateFlightPhase(){
        final AirplaneFlightphase flightPhaseValue = AirplaneFlightphase.values()[positionDataRaw.getValue().flightphase];
        if (flightPhase != null) {
            Platform.runLater(
                    () -> {
                        flightPhase.setValue(flightPhaseValue);
                    }
            );
        }
    }


    private void updatePosition(PositionData position) {
        final Position currentPosition = getCurrentPosition(position);
        final double lineOfSight = getLineOfSight(currentPosition);

        Platform.runLater(
            () -> {
                positionProperty.setValue(currentPosition);
                lineOfSightProperty.setValue(lineOfSight);
            });
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

            Platform.runLater(
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

            Platform.runLater(
                () -> {
                    windSpeedProperty.setValue(windSpeed);
                });
            updateNumberAbsoluteProperty(data, PlaneHealthChannel.WIND_DIRECTION, windDirectionProperty);
        }
    }

    private void updateAltitude(PositionData position) {
        double maxAltitude = getMaxAltitude();
        final String maxAltitudeString = maxAltitude + "m";

        final double altitude = getAltitude(position);
        final String altitudeString = altitude + "m";

        final Pair<String, AlertLevel> altitudeRelationAndAlert = getAltitudeRelationAndAlert(altitude, maxAltitude);

        Platform.runLater(
            () -> {
                maxAltitudeProperty.setValue(maxAltitudeString);
                maxAltitudeValueProperty.setValue(maxAltitude);
                altitudeValueProperty.setValue(altitude);
                altitudeProperty.setValue(altitudeString);
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
            return 0.0;
        }

        try {
            return cache.getCurPlaneElevOverGround();
        } catch (AirplaneCacheEmptyException ex) {
            System.out.println("EXCEPTION in altitude. Giving relative altitude");
            if (position != null) {
                return Math.round(position.altitude / 100.0);
            }
        }

        return 0.0;
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

        Platform.runLater(
            () -> {
                groundSpeedProperty.setValue(groundSpeed);
            });
    }

    @Override
    public void connectionStateChange(AirplaneConnectorState newState) {
        if (newState == null) {
            newState = AirplaneConnectorState.unconnected;
            // TODO reset properties
        }

        final AirplaneConnectorState state = newState;

        Platform.runLater(
            () -> {
                connectionProperty.setValue(state);
                connectionState.setValue(state);
            });
    }

    public AirplaneFlightphase getFlightPhase() {
        return flightPhase.get();
    }

    public ObjectProperty<AirplaneFlightmode> flightModeProperty() {
        return flightMode;
    }

    public ReadOnlyObjectProperty<AirplaneFlightphase> flightPhaseProperty() {
        return flightPhase;
    }

    public boolean getIsGpsLost() {
        return isGpsLost.get();
    }

    public BooleanProperty isGpsLostProperty() {
        return isGpsLost;
    }

    public AirplaneConnectorState getConnectionState() {
        return connectionState.get();
    }

    public ReadOnlyObjectProperty<AirplaneConnectorState> connectionStateProperty() {
        return connectionState;
    }

    public IAirplane getLegacyPlane() {
        return legacyPlane;
    }

    @Override
    public void recv_msg(Integer severityLevel, String data) {
        final int uavSeverityLevel = severityLevel;
        final String uavMsg = data;
        System.out.println("Uav: severity level and message are: " + uavSeverityLevel
                + ", " + uavMsg);
        Platform.runLater(
                () -> {
                    uavMsgSeverityLevelProperty.setValue(uavSeverityLevel);
                    uavMsgProperty.setValue(uavMsg);
                });
    }

    @Override
    public void recv_flightPhase(Integer fp) {
        Platform.runLater(
                () -> {
                    flightPhase.set(AirplaneFlightphase.values()[fp]);
                    isGpsLost.setValue(fp == AirplaneFlightphase.gpsloss.ordinal());
                }
        );
        //flightPhase.set(AirplaneFlightphase.values()[fp]);
        //isGpsLost.setValue(fp == AirplaneFlightphase.gpsloss.ordinal());
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

    public Property<String> batteryVoltageProperty() {
        return batteryVoltageProperty;
    }

    public Property<Number> batteryVoltageValueProperty() {
        return batteryVoltageValueProperty;
    }

    public Property<String> batteryPercentageProperty() {
        return batteryPercentageProperty;
    }

    public Property<Number> batteryPercentageValueProperty() {
        return batteryPercentageValueProperty;
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

    public Property<Number> gpsProperty() {
        return gpsProperty;
    }

    public Property<Number> glonassProperty() {
        return glonassProperty;
    }

    public Property<GPSFixType> gpsQualityProperty() {
        return gpsQualityProperty;
    }

    public Property<AlertLevel> gpsQualityAlertProperty() {
        return gpsQualityAlertProperty;
    }

    public IntegerProperty motor1RpmProperty() {
        return motor1RpmProperty;
    }

    public Property<String> groundSpeedProperty() {
        return groundSpeedProperty;
    }

    public Property<String> altitudeProperty() {
        return altitudeProperty;
    }

    public DoublePropertyBase altitudeValueProperty() {
        return altitudeValueProperty;
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

    public Property<AirplaneConnectorState> connectionProperty() {
        return connectionProperty;
    }

    public MapProperty<String, PlaneHealthChannelStatus> notImportantAlertsProperty() {
        return notImportantAlertsProperty;
    }

    public MapProperty<String, PlaneHealthChannelStatus> alertsProperty() {
        return alertsProperty;
    }

    public Property<Position> positionProperty() {
        return positionProperty;
    }

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

    public StringProperty flightTimeProperty() {
        return flightTime;
        //return new SimpleStringProperty((positionOrientationDataRaw!= null)? StringHelper.secToShortDHMS(positionOrientationDataRaw.getValue().time_sec) : "0:00");
    }

    public IntegerProperty getCurrentWaypointProperty(){
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
        if(po == null){
            return;
        }
        if (po.reentrypoint >= 0) {
            updateCurrentWaypoint(po);
        }
    }

    private void updateCurrentWaypoint(PositionOrientationData po){
        final int currentWaypoint = po.reentrypoint;
        Platform.runLater(
                () -> {
                    currentWaypointProperty.setValue(currentWaypoint);
                });
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
        System.out.println("I am in Uav.java. Result is: " + resultData.uavCommand.getDisplayName() + "; " + resultData.uavCommandResult.getDisplayName() + "; " + resultData.otherInformation);
        if (resultData == null) {
            return;
        }

        updateCommandResult(resultData);
    }

    private void updateCommandResult(CommandResultData resultData) {
        final CommandResultData data = resultData;
        Platform.runLater(
                () -> {
                    commandResultDataObjectProperty.setValue(resultData);
                });
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


    public void setEmergencyAction(IEmergencyProcedure procedure){
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

    public List<AirplaneEventActions> getPlaneEmergencyProcedures(){
        return PLANE_EMERGENCY_PROCEDURES;
    }

    public IntegerProperty getUavMsgSeverityLevelProperty() {
        return uavMsgSeverityLevelProperty;
    }

    public StringProperty getUavMsgProperty() {
        return uavMsgProperty;
    }
}
