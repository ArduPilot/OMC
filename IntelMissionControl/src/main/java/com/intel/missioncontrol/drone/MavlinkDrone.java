/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.drone.connection.DroneMessage;
import com.intel.missioncontrol.drone.connection.IDroneConnectionExceptionListener;
import com.intel.missioncontrol.drone.connection.IDroneMessageListener;
import com.intel.missioncontrol.drone.connection.IMavlinkDroneConnection;
import com.intel.missioncontrol.drone.connection.mavlink.IMavlinkParameter;
import com.intel.missioncontrol.hardware.HardwareConfigurationException;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IMavlinkFlightPlanOptions;
import com.intel.missioncontrol.hardware.MavlinkFlightPlanOptions;
import com.intel.missioncontrol.mission.FlightPlan;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanChangeListener;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Quaternion;
import io.dronefleet.mavlink.common.Attitude;
import io.dronefleet.mavlink.common.GlobalPositionInt;
import io.dronefleet.mavlink.common.GpsFixType;
import io.dronefleet.mavlink.common.GpsRawInt;
import io.dronefleet.mavlink.common.Heartbeat;
import io.dronefleet.mavlink.common.MavLandedState;
import io.dronefleet.mavlink.common.MavMissionType;
import io.dronefleet.mavlink.common.MavModeFlag;
import io.dronefleet.mavlink.common.MavSeverity;
import io.dronefleet.mavlink.common.MavSysStatusSensor;
import io.dronefleet.mavlink.common.MissionCurrent;
import io.dronefleet.mavlink.common.MissionItemReached;
import io.dronefleet.mavlink.common.Statustext;
import io.dronefleet.mavlink.common.SysStatus;
import io.dronefleet.mavlink.util.EnumValue;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import javafx.beans.value.ObservableValue;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncDoubleProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncIntegerProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncDoubleProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MavlinkDrone implements IDrone {
    private static final Logger LOGGER = LoggerFactory.getLogger(MavlinkDrone.class);
    private MavlinkFlightPlan mavlinkFlightPlan;

    private final AsyncBooleanProperty attitudeTelemetryOld =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());
    private final AsyncBooleanProperty positionTelemetryOld =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());
    private final AsyncBooleanProperty flightSegmentTelemetryOld =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());
    private final AsyncBooleanProperty flightTimeTelemetryOld =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());
    private final AsyncBooleanProperty autopilotStateTelemetryOld =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());
    private final AsyncBooleanProperty mavLandedStateOld =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private final AsyncObjectProperty<IHardwareConfiguration> hardwareConfiguration =
        new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Battery> battery = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Health> health = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Storage> storage = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<RemoteControl> remoteControl = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<GnssInfo> gnssInfo = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Position> position = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<Quaternion> attitude = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<FlightSegment> flightSegment =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<FlightSegment>().initialValue(FlightSegment.UNKNOWN).create());
    private final AsyncObjectProperty<AutopilotState> autopilotState =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<AutopilotState>().initialValue(AutopilotState.UNKNOWN).create());
    private final AsyncObjectProperty<ArmedState> armedState =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<ArmedState>().initialValue(ArmedState.UNKNOWN).create());
    private final AsyncObjectProperty<Duration> flightTime = new SimpleAsyncObjectProperty<>(this);

    private final AsyncDoubleProperty flightPlanUploadProgress =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(Double.NaN).create());
    private final AsyncObjectProperty<FlightPlan> activeFlightPlan = new SimpleAsyncObjectProperty<>(this);
    private final AsyncIntegerProperty activeFlightPlanWaypointIndex = new SimpleAsyncIntegerProperty(this);

    private final AsyncObjectProperty<MavLandedState> mavLandedState =
        new SimpleAsyncObjectProperty<>(
            this,
            new PropertyMetadata.Builder<MavLandedState>()
                .initialValue(MavLandedState.MAV_LANDED_STATE_UNDEFINED)
                .create());

    private final AsyncListProperty<MavlinkCamera> cameras = new SimpleAsyncListProperty<>(this);

    private final AsyncObjectProperty<GrayhawkObstacleAvoidance> obstacleAvoidance =
        new SimpleAsyncObjectProperty<>(this);

    private final List<IDroneConnectionExceptionListener> droneConnectionExceptionListeners =
        new CopyOnWriteArrayList<>();

    private final List<IDroneMessageListener> droneMessageListeners = new CopyOnWriteArrayList<>();

    private CancellationSource waypointUpdateCancellationSource = null;

    final IMavlinkDroneConnection droneConnection;

    private final FlightTimeTimer flightTimeTimer;

    MavlinkDrone(IMavlinkDroneConnection droneConnection, IHardwareConfiguration hardwareConfiguration) {
        this.droneConnection = droneConnection;

        LOGGER.info("new MavlinkDrone");

        battery.setValue(new Battery());
        gnssInfo.setValue(new GnssInfo());
        health.setValue(new Health());
        storage.setValue(new Storage());
        remoteControl.setValue(new RemoteControl());

        this.hardwareConfiguration.set(hardwareConfiguration);

        flightTimeTimer = new FlightTimeTimer(droneConnection.getCancellationSource());

        droneConnection
            .getHeartbeatSenderFuture()
            .whenFailed(
                e -> raiseDroneConnectionExceptionEvent(new DroneConnectionException(Heartbeat.class, false, e)));

        // TODO this should be removed with IMC-2806
        IFlightplanChangeListener clearActiveFlightPlanOnEditsListener = new IFlightplanChangeListener() {
            @Override
            public void flightplanStructureChanged(IFlightplanRelatedObject fp) {
                clearActiveFlightPlan();
            }

            @Override
            public void flightplanValuesChanged(IFlightplanRelatedObject fpObj) {
                clearActiveFlightPlan();
            }

            @Override
            public void flightplanElementRemoved(CFlightplan fp, int i, IFlightplanRelatedObject statement) {
                clearActiveFlightPlan();
            }

            @Override
            public void flightplanElementAdded(CFlightplan fp, IFlightplanRelatedObject statement) {
                clearActiveFlightPlan();
            }
        };
        activeFlightPlan.addListener(
                (obs, from, to) -> {
                    if(from != null) from.getLegacyFlightplan().removeFPChangeListener(clearActiveFlightPlanOnEditsListener);
                    if(to != null) to.getLegacyFlightplan().addFPChangeListener(clearActiveFlightPlanOnEditsListener);
                }
        );
    }

    void initializeBindings() {
        double linkLostTimeoutSeconds =
            hardwareConfiguration.get().getPlatformDescription().getConnectionProperties().getLinkLostTimeoutSeconds();

        // Heartbeat
        droneConnection
            .getConnectionProtocolReceiver()
            .registerHeartbeatHandlerAsync(
                receivedPayload -> {
                    Heartbeat heartbeat = receivedPayload.getPayload();
                    if (heartbeat.type().entry() != droneConnection.getMavType()) {
                        raiseDroneConnectionExceptionEvent(
                            new DroneConnectionException(Heartbeat.class, true, "Invalid vehicle type"));
                    }

                    EnumValue<MavModeFlag> baseMode = heartbeat.baseMode();
                    armedState.set(
                        baseMode.flagsEnabled(MavModeFlag.MAV_MODE_FLAG_SAFETY_ARMED)
                            ? ArmedState.ARMED
                            : ArmedState.DISARMED);

                    // more handlers in subclasses
                },
                linkLostTimeoutSeconds,
                () -> {
                    // timeout
                    armedState.set(ArmedState.UNKNOWN);
                    raiseDroneConnectionExceptionEvent(
                        new DroneConnectionException(Heartbeat.class, true, "Connection timeout"));
                })
            .whenFailed(
                e -> raiseDroneConnectionExceptionEvent(new DroneConnectionException(Heartbeat.class, false, e)));

        // SysStatus
        droneConnection
            .registerTelemetryWithAutoTimeoutAsync(
                SysStatus.class,
                receivedPayload -> {
                    battery.get().telemetryOldProperty().set(false);
                    SysStatus sysStatus = receivedPayload.getPayload();
                    double batteryVoltage = ((double)sysStatus.voltageBattery()) * 0.001;
                    int remainingBatteryPercentage = sysStatus.batteryRemaining();
                    battery.get().voltageProperty().set(batteryVoltage);

                    if (remainingBatteryPercentage == -1) {
                        battery.get().remainingChargePercentageProperty().set(Double.NaN);
                    } else {
                        battery.get().remainingChargePercentageProperty().set(sysStatus.batteryRemaining());
                    }

                    // TODO alertLevels from JSON hardware descriptions
                    if (remainingBatteryPercentage >= 30) {
                        battery.get().alertLevelProperty().set(BatteryAlertLevel.GREEN);
                    } else if (remainingBatteryPercentage >= 20) {
                        battery.get().alertLevelProperty().set(BatteryAlertLevel.YELLOW);
                    } else if (remainingBatteryPercentage >= 0) {
                        battery.get().alertLevelProperty().set(BatteryAlertLevel.RED);
                    } else {
                        battery.get().alertLevelProperty().set(BatteryAlertLevel.UNKNOWN);
                    }

                    // RC health:
                    RemoteControl.Status remoteControlStatus;

                    boolean rcEnabled =
                        sysStatus
                            .onboardControlSensorsEnabled()
                            .flagsEnabled(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_RC_RECEIVER);

                    boolean rcPresent =
                        sysStatus
                            .onboardControlSensorsPresent()
                            .flagsEnabled(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_RC_RECEIVER);

                    if (!rcEnabled || !rcPresent) {
                        remoteControlStatus = RemoteControl.Status.NO_REMOTE_CONTROL;
                    } else {
                        boolean rcHealth =
                            sysStatus
                                .onboardControlSensorsHealth()
                                .flagsEnabled(MavSysStatusSensor.MAV_SYS_STATUS_SENSOR_RC_RECEIVER);

                        if (!rcHealth) {
                            remoteControlStatus = RemoteControl.Status.REMOTE_CONTROL_ERROR;
                        } else {
                            remoteControlStatus = RemoteControl.Status.OK;
                        }
                    }

                    remoteControl.get().statusProperty().set(remoteControlStatus);
                },
                // timeout
                () -> battery.get().telemetryOldProperty().set(true))
            .whenFailed(
                e -> raiseDroneConnectionExceptionEvent(new DroneConnectionException(SysStatus.class, false, e)));

        // GlobalPosition
        droneConnection
            .registerTelemetryWithAutoTimeoutAsync(
                GlobalPositionInt.class,
                receivedPayload -> {
                    positionTelemetryOld.set(false);
                    GlobalPositionInt globalPosition = receivedPayload.getPayload();
                    position.set(
                        new Position(
                            Angle.fromDegreesLatitude(((double)globalPosition.lat() * 1e-7)),
                            Angle.fromDegreesLongitude(((double)globalPosition.lon() * 1e-7)),
                            ((double)globalPosition.relativeAlt() * 0.001)));
                },
                // timeout:
                () -> positionTelemetryOld.set(true))
            .whenFailed(
                e ->
                    raiseDroneConnectionExceptionEvent(
                        new DroneConnectionException(GlobalPositionInt.class, false, e)));

        // GPS raw:
        droneConnection
            .registerTelemetryWithAutoTimeoutAsync(
                GpsRawInt.class,
                receivedPayload -> {
                    gnssInfo.get().telemetryOldProperty().set(false);
                    GpsRawInt gpsRaw = receivedPayload.getPayload();
                    int numberOfSatellites = gpsRaw.satellitesVisible(); // 255 if unknown

                    double gpsQuality = Double.NaN;
                    if (numberOfSatellites < 255) {
                        gpsQuality = (numberOfSatellites - 3) * 10;
                        if (gpsQuality > 100) {
                            gpsQuality = 100;
                        }

                        if (gpsQuality < 0) {
                            gpsQuality = 0;
                        }
                    }

                    gnssInfo.get()
                        .numberOfSatellitesProperty()
                        .set(numberOfSatellites < 0 || numberOfSatellites >= 255 ? -1 : numberOfSatellites);
                    gnssInfo.get().qualityPercentageProperty().set(gpsQuality);

                    GpsFixType fixType = gpsRaw.fixType().entry();
                    GnssState gnssState;
                    switch (fixType) {
                    case GPS_FIX_TYPE_NO_GPS:
                    case GPS_FIX_TYPE_NO_FIX:
                        gnssState = GnssState.NO_FIX;
                        break;
                    case GPS_FIX_TYPE_2D_FIX:
                    case GPS_FIX_TYPE_3D_FIX:
                    case GPS_FIX_TYPE_DGPS:
                        gnssState = GnssState.GPS;
                        break;
                    case GPS_FIX_TYPE_RTK_FLOAT:
                        gnssState = GnssState.RTK_FLOAT;
                        break;
                    case GPS_FIX_TYPE_RTK_FIXED:
                        gnssState = GnssState.RTK_FIXED;
                        break;
                    case GPS_FIX_TYPE_STATIC:
                    case GPS_FIX_TYPE_PPP:
                    default:
                        gnssState = GnssState.UNKNOWN;
                        break;
                    }

                    gnssInfo.get().gnssStateProperty().set(gnssState);
                },
                // timeout
                () -> gnssInfo.get().telemetryOldProperty().set(true))
            .whenFailed(
                e -> raiseDroneConnectionExceptionEvent(new DroneConnectionException(GpsRawInt.class, false, e)));

        // Attitude
        droneConnection
            .registerTelemetryWithAutoTimeoutAsync(
                Attitude.class,
                receivedPayload -> {
                    attitudeTelemetryOld.set(false);
                    Attitude a = receivedPayload.getPayload();
                    attitude.set(
                        Quaternion.fromRotationXYZ(
                            Angle.fromRadians(a.roll()), Angle.fromRadians(a.pitch()), Angle.fromRadians(a.yaw())));
                },
                // timeout
                () -> attitudeTelemetryOld.set(true))
            .whenFailed(
                e -> raiseDroneConnectionExceptionEvent(new DroneConnectionException(Attitude.class, false, e)));

        droneConnection
            .getTelemetryReceiver()
            .registerStatusTextHandlerAsync(
                receivedPayload -> {
                    Statustext statustext = receivedPayload.getPayload();
                    String text = statustext.text();
                    MavSeverity mavSeverity = statustext.severity().entry();

                    Function<MavSeverity, DroneMessage.Severity> getSeverity =
                        (MavSeverity ms) -> {
                            switch (ms) {
                            case MAV_SEVERITY_WARNING:
                                return DroneMessage.Severity.WARNING;
                            case MAV_SEVERITY_NOTICE:
                            case MAV_SEVERITY_INFO:
                            case MAV_SEVERITY_DEBUG:
                                return DroneMessage.Severity.INFO;
                            case MAV_SEVERITY_EMERGENCY:
                            case MAV_SEVERITY_ALERT:
                            case MAV_SEVERITY_CRITICAL:
                            case MAV_SEVERITY_ERROR:
                            default:
                                return DroneMessage.Severity.ERROR;
                            }
                        };

                    var message = new DroneMessage(text, Date.from(Instant.now()), getSeverity.apply(mavSeverity));
                    evaluateMessageAsync(message)
                        .whenFailed(e -> LOGGER.error("Error evaluating message from drone: " + message, e))
                        .whenSucceeded(
                            raiseEvent -> {
                                if (raiseEvent) {
                                    raiseDroneMessageEvent(message);
                                }
                            });
                })
            .whenFailed(
                e -> raiseDroneConnectionExceptionEvent(new DroneConnectionException(Statustext.class, false, e)));

        // cameras
        cameras.bind(droneConnection.connectedCamerasProperty());

        // flight time
        flightTime.bind(flightTimeTimer.flightTimeProperty());
        flightTimeTelemetryOld.set(false);
        requestFlightStartTimeAsync()
            .whenSucceeded(flightTimeTimer::update)
            .whenFailed(
                () -> {
                    flightTimeTelemetryOld.set(true);
                    flightTimeTimer.update(null);
                });
        flightSegment.addListener(
            (o, oldValue, newValue) -> {
                if (newValue == FlightSegment.ON_GROUND) {
                    // change to ground detected
                    flightTimeTimer.update(null);
                    flightTimeTelemetryOld.set(false);
                } else if (oldValue == FlightSegment.ON_GROUND && newValue != FlightSegment.UNKNOWN) {
                    // change from ground to airborne detected
                    requestFlightStartTimeAsync()
                        .thenAccept(
                            flightStartTime -> {
                                flightTimeTimer.update(flightStartTime != null ? flightStartTime : Instant.now());
                                flightTimeTelemetryOld.set(false);
                            })
                        .whenFailed(
                            () -> {
                                flightTimeTelemetryOld.set(true);
                                flightTimeTimer.update(Instant.now());
                            });
                }
            });

        // mission cleanup
        flightSegment.bind(createFlightSegmentBinding());
        flightSegment.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue == FlightSegment.ON_GROUND && activeFlightPlan.get() != null) {
                    clearActiveFlightPlan();
                }
            });

        // bindings provided by derived classes
        flightSegmentTelemetryOld.bind(createFlightSegmentTelemetryOldBinding());
        autopilotState.bind(createAutopilotStateBinding());
        autopilotStateTelemetryOld.bind(createAutopilotStateTelemetryOldBinding());
        mavLandedState.bind(createMavLandedStateBinding());
        mavLandedStateOld.bind(createMavLandedStateOldBinding());
    }

    protected Future<Instant> requestFlightStartTimeAsync() {
        return Futures.successful(null);
    }

    /**
     * Evaluate a message received from the drone, and indicate if a DroneMessageEvent should be raised.
     *
     * @return True if this message should raise a DroneMessageEvent, false otherwise.
     */
    protected abstract Future<Boolean> evaluateMessageAsync(DroneMessage message);

    private void registerMissionUpdateReceivers(CancellationSource cancellationSource) {
        // Listen to waypoint update messages
        droneConnection
            .createMissionProtocolReceiver(cancellationSource)
            .registerMissionCurrentHandlerAsync(
                receivedPayload -> {
                    MissionCurrent missionCurrent = receivedPayload.getPayload();
                    int seq = missionCurrent.seq();

                    synchronized (activeFlightPlan) {
                        if (activeFlightPlan.get() != null && mavlinkFlightPlan != null) {
                            try {
                                activeFlightPlanWaypointIndex.set(
                                    mavlinkFlightPlan.getWayPointIndexForMissionItemIndex(seq));
                            } catch (IllegalArgumentException e) {
                                LOGGER.error("Waypoint index error", e);
                                activeFlightPlanWaypointIndex.set(0);
                            }
                        }
                    }
                },
                () -> {
                    // ignore timeout
                })
            .whenFailed(
                e -> raiseDroneConnectionExceptionEvent(new DroneConnectionException(MissionCurrent.class, false, e)));

        droneConnection
            .createMissionProtocolReceiver(cancellationSource)
            .registerMissionItemReachedHandlerAsync(
                receivedPayload -> {
                    MissionItemReached missionItemReached = receivedPayload.getPayload();
                    int seq = missionItemReached.seq();

                    synchronized (activeFlightPlan) {
                        if (mavlinkFlightPlan == null) {
                            return;
                        }

                        int count = mavlinkFlightPlan.getMissionItemCount();
                        if (activeFlightPlan.get() == null || seq < count - 1) {
                            return;
                        }

                        // Last waypoint was reached
                        if (autopilotState.get() == AutopilotState.AUTOPILOT
                                && flightSegment.get() == FlightSegment.PLAN_RUNNING
                                && !mavlinkFlightPlan.getLandAutomatically()) {
                            clearActiveFlightPlan();
                            pauseFlightPlanAsync();
                        }
                    }
                },
                () -> {
                    // ignore timeout
                })
            .whenFailed(
                e ->
                    raiseDroneConnectionExceptionEvent(
                        new DroneConnectionException(MissionItemReached.class, false, e)));
    }

    int getAutoBaseMode() {
        return EnumValue.create(
                MavModeFlag.MAV_MODE_FLAG_SAFETY_ARMED,
                MavModeFlag.MAV_MODE_FLAG_STABILIZE_ENABLED,
                MavModeFlag.MAV_MODE_FLAG_GUIDED_ENABLED,
                MavModeFlag.MAV_MODE_FLAG_AUTO_ENABLED,
                MavModeFlag.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED)
            .value();
    }

    Health getHealth() {
        return health.get();
    }

    Storage getStorage() {
        return storage.get();
    }

    protected abstract ObservableValue<FlightSegment> createFlightSegmentBinding();

    protected abstract ObservableValue<Boolean> createFlightSegmentTelemetryOldBinding();

    protected abstract ObservableValue<AutopilotState> createAutopilotStateBinding();

    protected abstract ObservableValue<Boolean> createAutopilotStateTelemetryOldBinding();

    protected abstract ObservableValue<MavLandedState> createMavLandedStateBinding();

    protected abstract ObservableValue<Boolean> createMavLandedStateOldBinding();

    // DroneConnectionException event:
    public void addListener(IDroneConnectionExceptionListener droneConnectionExceptionListener) {
        droneConnectionExceptionListeners.add(droneConnectionExceptionListener);
    }

    public void removeListener(IDroneConnectionExceptionListener droneConnectionExceptionListener) {
        droneConnectionExceptionListeners.removeIf(listener -> listener.equals(droneConnectionExceptionListener));
    }

    void raiseDroneConnectionExceptionEvent(DroneConnectionException e) {
        LOGGER.warn("DroneConnection exception: " + e.getMessage(), e);

        for (IDroneConnectionExceptionListener listener : droneConnectionExceptionListeners) {
            listener.onDroneConnectionException(this, e);
        }
    }

    // DroneMessage event:
    @Override
    public void addListener(IDroneMessageListener droneMessageListener) {
        droneMessageListeners.add(droneMessageListener);
    }

    @Override
    public void removeListener(IDroneMessageListener droneMessageListener) {
        droneMessageListeners.removeIf(listener -> listener.equals(droneMessageListener));
    }

    private void raiseDroneMessageEvent(DroneMessage message) {
        for (IDroneMessageListener listener : droneMessageListeners) {
            listener.onDroneMessage(this, message);
        }
    }

    @Override
    public ReadOnlyAsyncDoubleProperty flightPlanUploadProgressProperty() {
        return flightPlanUploadProgress;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<FlightPlan> activeFlightPlanProperty() {
        return activeFlightPlan;
    }

    @Override
    public ReadOnlyAsyncIntegerProperty activeFlightPlanWaypointIndexProperty() {
        return activeFlightPlanWaypointIndex;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<IHardwareConfiguration> hardwareConfigurationProperty() {
        return hardwareConfiguration;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IBattery> batteryProperty() {
        return battery;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IHealth> healthProperty() {
        return health;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IStorage> storageProperty() {
        return storage;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IRemoteControl> remoteControlProperty() {
        return remoteControl;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Position> positionProperty() {
        return position;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Quaternion> attitudeProperty() {
        return attitude;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<FlightSegment> flightSegmentProperty() {
        return flightSegment;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Duration> flightTimeProperty() {
        return flightTime;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty flightTimeTelemetryOldProperty() {
        return flightTimeTelemetryOld;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<GnssInfo> gnssInfoProperty() {
        return gnssInfo;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty flightSegmentTelemetryOldProperty() {
        return flightSegmentTelemetryOld;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty autopilotStateTelemetryOldProperty() {
        return autopilotStateTelemetryOld;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<AutopilotState> autopilotStateProperty() {
        return autopilotState;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty positionTelemetryOldProperty() {
        return positionTelemetryOld;
    }

    @Override
    public ReadOnlyAsyncBooleanProperty attitudeTelemetryOldProperty() {
        return attitudeTelemetryOld;
    }

    @Override
    public ReadOnlyAsyncListProperty<? extends ICamera> camerasProperty() {
        return cameras;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IObstacleAvoidance> obstacleAvoidanceProperty() {
        return obstacleAvoidance;
    }

    ReadOnlyAsyncObjectProperty<MavLandedState> mavLandedStateProperty() {
        return mavLandedState;
    }

    ReadOnlyAsyncBooleanProperty mavLandedStateOldProperty() {
        return mavLandedStateOld;
    }

    ReadOnlyAsyncObjectProperty<ArmedState> armedStateProperty() {
        return armedState;
    }

    @Override
    public Future<Void> takeOffAsync(FlightPlanWithWayPointIndex flightPlanWithWayPointIndex) {
        if (!autopilotState.get().equals(AutopilotState.AUTOPILOT)) {
            return Futures.failed(new IllegalStateException("Drone not in Automatic mode"));
        }

        if (!flightSegment.get().equals(FlightSegment.ON_GROUND)) {
            return Futures.failed(new IllegalStateException("Drone not on ground"));
        }

        if (flightPlanWithWayPointIndex.getFlightPlan() == null) {
            return Futures.failed(new IllegalArgumentException("No mission given for execution"));
        }

        // disarm if armed (and allowed via flightplan options):
        Future<Void> disarmFuture;
        if (armedState.get() != ArmedState.DISARMED) {
            if (hardwareConfiguration
                    .get()
                    .getPlatformDescription()
                    .getMavlinkFlightPlanOptions()
                    .getAutoDisarmBeforeTakeoff()) {
                disarmFuture =
                    droneConnection
                        .getCommandProtocolSender()
                        .sendArmDisarmAsync(false)
                        .whenSucceeded(() -> LOGGER.debug("takeOffAsync: Disarmed"))
                        .whenFailed(e -> LOGGER.debug("takeOffAsync: Disarm failed, " + e));
            } else {
                disarmFuture = Futures.failed(new IllegalStateException("Drone must be disarmed before takeoff"));
            }
        } else {
            disarmFuture = Futures.successful(null);
        }

        // upload flight plan and start:
        return disarmFuture
            .thenRunAsync(() -> setActiveFlightPlanAsync(flightPlanWithWayPointIndex))
            .whenSucceeded(() -> LOGGER.debug("takeOffAsync: Active flightplan was set"))
            .thenRunAsync(this::runPreArmActionsAsync)
            .whenSucceeded(() -> LOGGER.debug("takeOffAsync: Ran pre arm actions"))
            .thenRunAsync(() -> droneConnection.getCommandProtocolSender().sendArmDisarmAsync(true))
            .whenSucceeded(() -> LOGGER.debug("takeOffAsync: Armed"))
            .thenRunAsync(
                () -> {
                    if (!autopilotState.get().equals(AutopilotState.AUTOPILOT)) {
                        return Futures.failed(new IllegalStateException("Drone not in Automatic mode"));
                    }

                    return sendSetMissionModeAsync();
                })
            .whenDone(
                f -> {
                    if (!f.isSuccess()) {
                        clearActiveFlightPlan();
                        if (f.isFailed()) {
                            LOGGER.debug("takeOffAsync error: " + f.getException());
                        }
                    } else {
                        LOGGER.debug("takeOffAsync: Mission mode set");
                    }
                })
            .whenCancelled(() -> LOGGER.debug("takeOffAsync cancelled"));
    }

    protected Future<Void> runPreArmActionsAsync() {
        return Futures.successful(null);
    }

    protected abstract List<IMavlinkParameter> createAutopilotParameterList(MavlinkFlightPlan mavlinkFlightPlan);

    protected abstract void applyFlightPlanInitialSettings(MavlinkFlightPlan mavlinkFlightPlan);

    private Future<Void> setAutopilotParametersAsync(MavlinkFlightPlan mavlinkFlightPlan) {
        return Dispatcher.platform()
            .getLaterAsync(() -> createAutopilotParameterList(mavlinkFlightPlan))
            .thenApplyAsync(
                autopilotParameterList -> {
                    LOGGER.info("Setting mavlink autopilot parameters: " + autopilotParameterList.toString());
                    applyFlightPlanInitialSettings(mavlinkFlightPlan);
                    return droneConnection.getParameterProtocolSender().setParamsAsync(autopilotParameterList);
                });
    }

    @Override
    public Future<Void> abortTakeOffAsync() {
        return landAsync();
    }

    @Override
    public Future<Void> landAsync() {
        if (!autopilotState.get().equals(AutopilotState.AUTOPILOT)) {
            return Futures.failed(new IllegalStateException("Drone not in Automatic mode"));
        }

        return sendSetLandingModeAsync();
    }

    @Override
    public Future<Void> abortLandingAsync() {
        return pauseFlightPlanAsync();
    }

    @Override
    public Future<Void> startFlightPlanAsync(FlightPlanWithWayPointIndex flightPlanWithWayPointIndex) {
        if (flightPlanWithWayPointIndex.getFlightPlan() == null) {
            return Futures.failed(new IllegalArgumentException("No mission given for execution"));
        }

        if (!autopilotState.get().equals(AutopilotState.AUTOPILOT)) {
            return Futures.failed(new IllegalStateException("Drone not in Automatic mode"));
        }

        return pauseFlightPlanAsync()
            .thenRunAsync(() -> setActiveFlightPlanAsync(flightPlanWithWayPointIndex))
            .thenRunAsync(
                () -> {
                    if (!autopilotState.get().equals(AutopilotState.AUTOPILOT)) {
                        return Futures.failed(new IllegalStateException("Drone not in Automatic mode"));
                    }

                    return sendSetMissionModeAsync();
                })
            .whenCancelled(() -> LOGGER.info("startFlightPlanAsync cancelled"));
    }

    @Override
    public Future<Void> pauseFlightPlanAsync() {
        if (!autopilotState.get().equals(AutopilotState.AUTOPILOT)) {
            return Futures.failed(new IllegalStateException("Drone not in Automatic mode"));
        }

        return sendSetLoiterModeAsync();
    }

    @Override
    public Future<Void> returnHomeAsync() {
        if (!autopilotState.get().equals(AutopilotState.AUTOPILOT)) {
            return Futures.failed(new IllegalStateException("Drone not in Automatic mode"));
        }

        return sendSetReturnHomeModeAsync();
    }

    /** Set the active flightplan, uploading data to the drone. */
    Future<Void> setActiveFlightPlanAsync(FlightPlanWithWayPointIndex flightPlanWithWayPointIndex) {
        return Dispatcher.background()
            .runLaterAsync(this::clearActiveFlightPlan)
            .thenRunAsync(() -> droneConnection.getMissionProtocolSender().sendClearMissionAsync())
            .thenRunAsync(
                () -> {
                    if (flightPlanWithWayPointIndex == null) {
                        return Futures.successful();
                    }

                    final IMavlinkFlightPlanOptions mavlinkFlightPlanOptions =
                        hardwareConfiguration.get().getPlatformDescription().getMavlinkFlightPlanOptions();

                    try {
                        MavlinkFlightPlanOptions.verify(mavlinkFlightPlanOptions);
                    } catch (HardwareConfigurationException e) {
                        return Futures.failed(e);
                    }

                    Position currentPosition = position.get();
                    if (currentPosition == null) {
                        return Futures.failed(new IllegalStateException("Current position unavailable"));
                    }

                    // TODO: posting to UI because of FlightPlan properties. Should make those properties async instead.

                    Dispatcher dispatcher = Dispatcher.platform();
                    return dispatcher
                        .getLaterAsync(
                            () -> {
                                Position startAltitudePosition =
                                    getStartAltitudePosition(
                                        flightPlanWithWayPointIndex.getFlightPlan(), currentPosition);
                                return MavlinkFlightPlan.fromFlightPlanWithWayPointIndex(
                                    flightPlanWithWayPointIndex, mavlinkFlightPlanOptions, startAltitudePosition);
                            })
                        .thenApplyAsync(
                            mavlinkFlightPlan ->
                                setAutopilotParametersAsync(mavlinkFlightPlan).thenGet(() -> mavlinkFlightPlan))
                        .thenApplyAsync( // set active FlightPlan and upload
                            mavlinkFlightPlan -> {
                                clearActiveFlightPlan();
                                LOGGER.info("Uploading: " + mavlinkFlightPlan.getDebugDescription());
                                Future<Void> f =
                                    droneConnection
                                        .getMissionProtocolSender()
                                        .sendMissionItemsAsync(
                                            MavMissionType.MAV_MISSION_TYPE_MISSION,
                                            mavlinkFlightPlan.getMissionItems())
                                        .whenSucceeded(
                                            v ->
                                                updateActiveFlightPlanProperties(
                                                    flightPlanWithWayPointIndex.getFlightPlan(),
                                                    flightPlanWithWayPointIndex.getWayPointIndex(),
                                                    mavlinkFlightPlan));
                                f.addProgressListener(flightPlanUploadProgress::set);
                                return f;
                            })
                        .whenCancelled(() -> LOGGER.debug("setActiveFlightPlanAsync cancelled"))
                        .whenDone(
                            f -> {
                                if (!f.isSuccess()) {
                                    flightPlanUploadProgress.set(Double.NaN);
                                }
                            });
                });
    }

    /** Get position between minStartAltitude and maxStartAltitude closest to current Altitude */
    private Position getStartAltitudePosition(FlightPlan flightPlan, Position currentPosition) {
        double altitude = currentPosition.getAltitude();

        if (altitude > flightPlan.getMaxStartAltitude()
                && this.mavLandedState.get() != MavLandedState.MAV_LANDED_STATE_IN_AIR) {
            altitude = flightPlan.getMaxStartAltitude();
        }

        if (altitude < flightPlan.getMinStartAltitude()) {
            altitude = flightPlan.getMinStartAltitude();
        }

        return new Position(currentPosition.latitude, currentPosition.longitude, altitude);
    }

    private void clearActiveFlightPlan() {
        updateActiveFlightPlanProperties(null, 0, null);
        try (LockedList<MavlinkCamera> cams = cameras.lock()) {
            for (MavlinkCamera cam : cams) {
                cam.imageCountProperty().set(0);
            }
        }
    }

    private void updateActiveFlightPlanProperties(
            FlightPlan flightPlan, int wayPointIndex, MavlinkFlightPlan mavlinkFlightPlan) {
        synchronized (activeFlightPlan) {
            if (waypointUpdateCancellationSource != null) {
                waypointUpdateCancellationSource.cancel();
            }

            activeFlightPlan.set(flightPlan);
            activeFlightPlanWaypointIndex.set(wayPointIndex);
            flightPlanUploadProgress.set(flightPlan == null ? Double.NaN : 1.0);
            this.mavlinkFlightPlan = mavlinkFlightPlan;

            if (flightPlan != null) {
                waypointUpdateCancellationSource = new CancellationSource();
                registerMissionUpdateReceivers(waypointUpdateCancellationSource);
            }
        }
    }

    protected abstract Future<Void> sendSetLandingModeAsync();

    protected abstract Future<Void> sendSetMissionModeAsync();

    protected abstract Future<Void> sendSetLoiterModeAsync();

    protected abstract Future<Void> sendSetReturnHomeModeAsync();
}
