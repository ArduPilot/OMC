/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.drone.connection.DroneMessage;
import com.intel.missioncontrol.drone.connection.MavlinkDroneConnection;
import com.intel.missioncontrol.drone.connection.mavlink.IMavlinkParameter;
import com.intel.missioncontrol.drone.connection.mavlink.Parameter;
import com.intel.missioncontrol.drone.connection.mavlink.StatusTextFilter;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IMavlinkFlightPlanOptions;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.mission.FlightPlan;
import io.dronefleet.mavlink.common.ExtendedSysState;
import io.dronefleet.mavlink.common.Heartbeat;
import io.dronefleet.mavlink.common.MavAutopilot;
import io.dronefleet.mavlink.common.MavLandedState;
import io.dronefleet.mavlink.common.MavModeFlag;
import io.dronefleet.mavlink.common.MavType;
import io.dronefleet.mavlink.common.StorageInformation;
import io.dronefleet.mavlink.util.EnumValue;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.slf4j.LoggerFactory;

public class PX4Drone extends MavlinkDrone {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PX4Drone.class);

    private final AsyncBooleanProperty px4CustomModeOld =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());
    private final AsyncBooleanProperty mavLandedStateOld =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());
    private final AsyncObjectProperty<PX4CustomMode> px4CustomMode =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<PX4CustomMode>().initialValue(PX4CustomMode.UNDEFINED).create());

    private final AsyncObjectProperty<MavLandedState> mavLandedState =
        new SimpleAsyncObjectProperty<>(
            this,
            new PropertyMetadata.Builder<MavLandedState>()
                .initialValue(MavLandedState.MAV_LANDED_STATE_UNDEFINED)
                .create());

    PX4Drone(MavlinkDroneConnection droneConnection, IHardwareConfiguration hardwareConfiguration) {
        super(droneConnection, hardwareConfiguration);
    }

    public static PX4Drone create(
            MavlinkDroneConnection droneConnection, IHardwareConfiguration hardwareConfiguration) {
        PX4Drone drone = new PX4Drone(droneConnection, hardwareConfiguration);
        drone.initializeBindings();
        return drone;
    }

    @Override
    void initializeBindings() {
        super.initializeBindings();

        // Heartbeat
        droneConnection
            .getConnectionProtocolReceiver()
            .registerHeartbeatHandlerAsync(
                receivedPayload -> {
                    Heartbeat heartbeat = receivedPayload.getPayload();
                    if (heartbeat.type().entry() == MavType.MAV_TYPE_CAMERA) {
                        LOGGER.error("Camera heartbeat received by drone handler");
                        return;
                    }

                    if (heartbeat.autopilot().entry() != MavAutopilot.MAV_AUTOPILOT_PX4) {
                        px4CustomMode.set(PX4CustomMode.UNDEFINED);
                        px4CustomModeOld.set(false);
                        raiseDroneConnectionExceptionEvent(
                            new DroneConnectionException(Heartbeat.class, true, "Invalid autopilot type"));
                    }

                    if (heartbeat.type().entry() != MavType.MAV_TYPE_QUADROTOR
                            && heartbeat.type().entry() != MavType.MAV_TYPE_HEXAROTOR
                            && heartbeat.type().entry() != MavType.MAV_TYPE_OCTOROTOR) {
                        px4CustomMode.set(PX4CustomMode.UNDEFINED);
                        px4CustomModeOld.set(false);
                        raiseDroneConnectionExceptionEvent(
                            new DroneConnectionException(Heartbeat.class, true, "Invalid vehicle type"));
                    }

                    EnumValue<MavModeFlag> baseMode = heartbeat.baseMode();
                    boolean isCustom = baseMode.flagsEnabled(MavModeFlag.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED);
                    px4CustomMode.set(
                        isCustom ? PX4CustomMode.fromCustomMode(heartbeat.customMode()) : PX4CustomMode.UNDEFINED);
                    px4CustomModeOld.set(false);
                },
                () -> {
                    // timeout
                    px4CustomModeOld.set(true);
                });
        // failure is handled in base class.

        // Extended system state
        droneConnection
            .registerTelemetryWithAutoTimeoutAsync(
                ExtendedSysState.class,
                receivedPayload -> {
                    ExtendedSysState extendedSysState = receivedPayload.getPayload();
                    mavLandedState.set(extendedSysState.landedState().entry());
                    mavLandedStateOld.set(false);
                },
                // timeout
                () -> {
                    mavLandedStateOld.set(true);
                })
            .whenFailed(
                e ->
                    raiseDroneConnectionExceptionEvent(new DroneConnectionException(ExtendedSysState.class, false, e)));

        // Health:
        // Calibration:

        droneConnection
            .getParameterProtocolSender()
            .requestMultipleParamsByIdAsync(Arrays.asList("CAL_MAG0_ID", "CAL_GYRO0_ID", "CAL_ACC0_ID"))
            .whenSucceeded(
                parameters ->
                    getHealth()
                        .calibrationStatusProperty()
                        .set(
                            (parameters.stream().anyMatch(p -> p.getIntValue() == 0)
                                ? IHealth.CalibrationStatus.CALIBRATION_NEEDED
                                : IHealth.CalibrationStatus.OK)))
            .whenFailed(e -> getHealth().calibrationStatusProperty().set(IHealth.CalibrationStatus.UNKNOWN));

        // StorageStatus:
        // TODO update interval
        droneConnection
            .getTelemetryReceiver()
            .periodicallyRequestStorageInformationAsync(
                Duration.ofSeconds(1),
                payload -> {
                    StorageInformation storageInformation = payload.getPayload();
                    Storage.Status storageStatus;
                    double availableCapacityMiB = 0.0;
                    if (storageInformation.storageCount() == 0) {
                        storageStatus = Storage.Status.NO_STORAGE_DEVICE;
                    } else {
                        storageStatus = Storage.Status.OK;
                        availableCapacityMiB = storageInformation.availableCapacity();
                    }

                    Storage storage = getStorage();
                    storage.statusProperty().set(storageStatus);
                    storage.availableSpaceMiBProperty().set(availableCapacityMiB);
                },
                // timeout:
                () -> {
                    getStorage().statusProperty().set(Storage.Status.UNKNOWN);
                    getStorage().availableSpaceMiBProperty().set(Double.NaN);
                })
            .whenFailed(
                e -> {
                    Throwable ex = e.getCause();
                    if (ex instanceof java.util.concurrent.TimeoutException) {
                        getStorage().statusProperty().set(Storage.Status.UNKNOWN);
                        getStorage().availableSpaceMiBProperty().set(Double.NaN);
                    } else {
                        LOGGER.info("requestStorageInformation error:", e);
                        getStorage().statusProperty().set(Storage.Status.STORAGE_DEVICE_ERROR);
                    }
                });
    }

    @Override
    protected ObservableValue<AutopilotState> createAutopilotStateBinding() {
        return Bindings.createObjectBinding(
            () -> {
                switch (px4CustomMode.get().getMainMode()) {
                case PX4_CUSTOM_MAIN_MODE_UNDEFINED:
                    return AutopilotState.UNKNOWN;
                case PX4_CUSTOM_MAIN_MODE_AUTO:
                    return AutopilotState.AUTOPILOT;
                default:
                    return AutopilotState.MANUAL;
                }
            },
            px4CustomMode);
    }

    @Override
    protected ObservableValue<Boolean> createAutopilotStateTelemetryOldBinding() {
        return px4CustomModeOld;
    }

    @Override
    protected ObservableValue<FlightSegment> createFlightSegmentBinding() {
        return Bindings.createObjectBinding(
            () -> {
                switch (mavLandedStateProperty().get()) {
                case MAV_LANDED_STATE_ON_GROUND:
                    return FlightSegment.ON_GROUND;
                case MAV_LANDED_STATE_IN_AIR:
                    {
                        PX4CustomMode mode = px4CustomMode.get();
                        if (mode.getMainMode() == PX4CustomMode.MainMode.PX4_CUSTOM_MAIN_MODE_AUTO) {
                            if (mode.getSubModeAuto() == PX4CustomMode.SubModeAuto.PX4_CUSTOM_SUB_MODE_AUTO_MISSION) {
                                return FlightSegment.PLAN_RUNNING;
                            }

                            if (mode.getSubModeAuto() == PX4CustomMode.SubModeAuto.PX4_CUSTOM_SUB_MODE_AUTO_RTL) {
                                return FlightSegment.RETURN_TO_HOME;
                            }

                            if (mode.getSubModeAuto() == PX4CustomMode.SubModeAuto.PX4_CUSTOM_SUB_MODE_AUTO_TAKEOFF) {
                                return FlightSegment.TAKEOFF;
                            }
                        }

                        return FlightSegment.HOLD;
                    }
                case MAV_LANDED_STATE_TAKEOFF:
                    return FlightSegment.TAKEOFF;
                case MAV_LANDED_STATE_LANDING:
                    return FlightSegment.LANDING;
                case MAV_LANDED_STATE_UNDEFINED:
                default:
                    return FlightSegment.UNKNOWN;
                }
            },
            mavLandedStateProperty(),
            px4CustomMode);
    }

    @Override
    protected ObservableValue<Boolean> createFlightSegmentTelemetryOldBinding() {
        return Bindings.createBooleanBinding(
            () -> px4CustomModeOld.get() || mavLandedStateOld.get(), px4CustomModeOld, mavLandedStateOld);
    }

    @Override
    protected ObservableValue<MavLandedState> createMavLandedStateBinding() {
        return mavLandedState;
    }

    @Override
    protected ObservableValue<Boolean> createMavLandedStateOldBinding() {
        return mavLandedStateOld;
    }

    @Override
    protected Future<Void> runPreArmActionsAsync() {
        return super.runPreArmActionsAsync();
    }

    @Override
    protected List<IMavlinkParameter> createAutopilotParameterList(MavlinkFlightPlan mavlinkFlightPlan) {
        // TODO make this work from anywhere
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                "createAutopilotParameterList must be called from UI thread because of FlightPlan access");
        }

        IPlatformDescription platformDesc = hardwareConfigurationProperty().get().getPlatformDescription();
        if (!platformDesc.areEmergencyActionsSettable()) {
            LOGGER.info("Platform " + platformDesc.getName() + " does not allow setting PX4 emergency parameters");
            return Collections.emptyList();
        }

        FlightPlan flightPlan = mavlinkFlightPlan.getFlightPlan();

        IMavlinkFlightPlanOptions options = mavlinkFlightPlan.getOptions();

        List<IMavlinkParameter> params = new ArrayList<>();

        if (mavlinkFlightPlan.getStartAltitudePosition() != null) {
            float startAltitude = (float)mavlinkFlightPlan.getStartAltitudePosition().getAltitude();
            params.add(Parameter.createFloat("MIS_TAKEOFF_ALT", startAltitude));
        }

        params.add(Parameter.createFloat("RTL_RETURN_ALT", (float)flightPlan.safetyAltitudeProperty().get()));

        if (options.getAcceptanceAngleDegrees() != 0.0) {
            params.add(Parameter.createFloat("MIS_YAW_ERR", (float)options.getAcceptanceAngleDegrees()));
        }

        // Emergency actions:
        PX4EmergencyActions.LinkLossAction rcLinkLossAction =
            PX4EmergencyActions.convertLinkLossAction(flightPlan.rcLinkLossActionProperty().get());
        params.add(Parameter.createInt32("NAV_RCL_ACT", rcLinkLossAction.getValue()));
        params.add(
            Parameter.createFloat(
                "COM_RC_LOSS_T", (float)flightPlan.rcLinkLossActionDelayProperty().get().toMillis() / 1000.0f));

        PX4EmergencyActions.LinkLossAction linkLossAction =
            PX4EmergencyActions.convertLinkLossAction(flightPlan.primaryLinkLossActionProperty().get());
        params.add(Parameter.createInt32("NAV_DLL_ACT", linkLossAction.getValue()));
        params.add(
            Parameter.createInt32(
                "COM_DL_LOSS_T", (int)flightPlan.primaryLinkLossActionDelayProperty().get().toSeconds()));

        PX4EmergencyActions.GeoFenceAction gfAction =
            PX4EmergencyActions.convertGeofenceAction(flightPlan.geofenceBreachActionProperty().get());
        params.add(Parameter.createInt32("GF_ACTION", gfAction.getValue()));

        PX4EmergencyActions.PositionLossAction positionLossAction =
            PX4EmergencyActions.convertPositionLossAction(flightPlan.positionLossActionProperty().get());
        params.add(Parameter.createInt32("COM_POSCTL_NAVL", positionLossAction.getValue()));
        params.add(
            Parameter.createInt32(
                "COM_POS_FS_DELAY", (int)flightPlan.positionLossActionDelayProperty().get().toSeconds()));

        // TODO
        // flightPlan.geofenceBreachActionDelayProperty().get();

        // TODO
        // PX4EmergencyActions.LowBatAction lowBatAction;
        // params.add(Parameter.createInt32("COM_LOW_BAT_ACT", lowBatAction.getValue()));

        return params;
    }

    @Override
    protected void applyFlightPlanInitialSettings(MavlinkFlightPlan mavlinkFlightPlan) {}

    @Override
    protected Future<Boolean> evaluateMessageAsync(DroneMessage message) {
        String msg = message.getMessage();
        if (msg == null
                || msg.equals("ALL DATA LINKS LOST")
                || msg.equals("Data link lost")
                || msg.equals(
                    "Failsafe enabled: no datalink")) { // message is only received after link has been regained
            return Futures.successful(false);
        }

        return Futures.successful(true);
    }

    @Override
    protected Future<Void> sendSetLandingModeAsync() {
        return droneConnection
            .getCommandProtocolSender()
            .sendSetModeAsync(
                getAutoBaseMode(),
                PX4CustomMode.MainMode.PX4_CUSTOM_MAIN_MODE_AUTO.getValue(),
                PX4CustomMode.SubModeAuto.PX4_CUSTOM_SUB_MODE_AUTO_LAND.getValue());
    }

    @Override
    protected Future<Void> sendSetMissionModeAsync() {
        return droneConnection
            .getCommandProtocolSender()
            .sendSetModeAsync(
                getAutoBaseMode(),
                PX4CustomMode.MainMode.PX4_CUSTOM_MAIN_MODE_AUTO.getValue(),
                PX4CustomMode.SubModeAuto.PX4_CUSTOM_SUB_MODE_AUTO_MISSION.getValue(),
                new StatusTextFilter("^Distance between waypoints too far.*"));
    }

    @Override
    protected Future<Void> sendSetLoiterModeAsync() {
        return droneConnection
            .getCommandProtocolSender()
            .sendSetModeAsync(
                getAutoBaseMode(),
                PX4CustomMode.MainMode.PX4_CUSTOM_MAIN_MODE_AUTO.getValue(),
                PX4CustomMode.SubModeAuto.PX4_CUSTOM_SUB_MODE_AUTO_LOITER.getValue());
    }

    @Override
    protected Future<Void> sendSetReturnHomeModeAsync() {
        return droneConnection
            .getCommandProtocolSender()
            .sendSetModeAsync(
                getAutoBaseMode(),
                PX4CustomMode.MainMode.PX4_CUSTOM_MAIN_MODE_AUTO.getValue(),
                PX4CustomMode.SubModeAuto.PX4_CUSTOM_SUB_MODE_AUTO_RTL.getValue());
    }
}
