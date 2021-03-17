/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.drone.connection.DroneMessage;
import com.intel.missioncontrol.drone.connection.MavlinkDroneConnection;
import com.intel.missioncontrol.drone.connection.mavlink.IMavlinkParameter;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import io.dronefleet.mavlink.ardupilotmega.PlaneMode;
import io.dronefleet.mavlink.common.Heartbeat;
import io.dronefleet.mavlink.common.MavAutopilot;
import io.dronefleet.mavlink.common.MavLandedState;
import io.dronefleet.mavlink.common.MavModeFlag;
import io.dronefleet.mavlink.common.MavType;
import io.dronefleet.mavlink.util.EnumValue;
import java.util.Collections;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;

public class ArduPlaneDrone extends MavlinkDrone {

    private final AsyncBooleanProperty arduPlaneCustomModeOld = new SimpleAsyncBooleanProperty(this);

    private final AsyncObjectProperty<ArduPlaneCustomMode> arduPlaneCustomMode =
        new SimpleAsyncObjectProperty<>(
            this,
            new PropertyMetadata.Builder<ArduPlaneCustomMode>().initialValue(ArduPlaneCustomMode.undefined).create());

    @SuppressWarnings("WeakerAccess")
    protected ArduPlaneDrone(MavlinkDroneConnection droneConnection, IHardwareConfiguration hardwareConfiguration) {
        super(droneConnection, hardwareConfiguration);

        // Heartbeat
        droneConnection
            .getConnectionProtocolReceiver()
            .registerHeartbeatHandlerAsync(
                receivedPayload -> {
                    Heartbeat heartbeat = receivedPayload.getPayload();
                    arduPlaneCustomModeOld.set(false);

                    if (heartbeat.autopilot().entry() != MavAutopilot.MAV_AUTOPILOT_ARDUPILOTMEGA) {
                        arduPlaneCustomMode.set(ArduPlaneCustomMode.undefined);
                        raiseDroneConnectionExceptionEvent(
                            new DroneConnectionException(Heartbeat.class, true, "Invalid autopilot type"));
                    }

                    if (heartbeat.type().entry() != MavType.MAV_TYPE_FIXED_WING) {
                        arduPlaneCustomMode.set(ArduPlaneCustomMode.undefined);
                        raiseDroneConnectionExceptionEvent(
                            new DroneConnectionException(Heartbeat.class, true, "Invalid vehicle type"));
                    }

                    EnumValue<MavModeFlag> baseMode = heartbeat.baseMode();
                    boolean isCustom = baseMode.flagsEnabled(MavModeFlag.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED);
                    arduPlaneCustomMode.set(
                        isCustom
                            ? ArduPlaneCustomMode.fromCustomMode(heartbeat.customMode())
                            : ArduPlaneCustomMode.undefined);
                },
                hardwareConfiguration.getPlatformDescription().getConnectionProperties().getLinkLostTimeoutSeconds(),
                () -> {
                    // timeout
                    arduPlaneCustomModeOld.set(true);
                });
        // failure is handled in base class.
    }

    public static ArduPlaneDrone create(
            MavlinkDroneConnection droneConnection, IHardwareConfiguration hardwareConfiguration) {
        ArduPlaneDrone drone = new ArduPlaneDrone(droneConnection, hardwareConfiguration);
        drone.initializeBindings();
        return drone;
    }

    @Override
    protected ObservableValue<AutopilotState> createAutopilotStateBinding() {
        return Bindings.createObjectBinding(
            () -> {
                if (arduPlaneCustomMode.get() == ArduPlaneCustomMode.undefined) {
                    return AutopilotState.UNKNOWN;
                }

                switch (arduPlaneCustomMode.get().getPlaneMode()) {
                case PLANE_MODE_CIRCLE:
                case PLANE_MODE_AUTO:
                case PLANE_MODE_LOITER:
                case PLANE_MODE_RTL:
                case PLANE_MODE_QLOITER:
                case PLANE_MODE_QLAND:
                case PLANE_MODE_QRTL:
                case PLANE_MODE_STABILIZE:
                case PLANE_MODE_QSTABILIZE:
                    return AutopilotState.AUTOPILOT;
                case PLANE_MODE_MANUAL:
                case PLANE_MODE_TRAINING:
                case PLANE_MODE_ACRO:
                case PLANE_MODE_FLY_BY_WIRE_A:
                case PLANE_MODE_FLY_BY_WIRE_B:
                case PLANE_MODE_CRUISE:
                case PLANE_MODE_AUTOTUNE:
                case PLANE_MODE_AVOID_ADSB:
                case PLANE_MODE_GUIDED:
                case PLANE_MODE_INITIALIZING:
                case PLANE_MODE_QHOVER:
                default:
                    return AutopilotState.MANUAL;
                }
            },
            arduPlaneCustomMode);
    }

    @Override
    protected ObservableValue<Boolean> createAutopilotStateTelemetryOldBinding() {
        return arduPlaneCustomModeOld;
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
                        ArduPlaneCustomMode mode = arduPlaneCustomMode.get();
                        if (mode.getPlaneMode() == PlaneMode.PLANE_MODE_AUTO) {
                            return FlightSegment.PLAN_RUNNING;
                        }

                        if (mode.getPlaneMode() == PlaneMode.PLANE_MODE_RTL
                                || mode.getPlaneMode() == PlaneMode.PLANE_MODE_QRTL) {
                            return FlightSegment.RETURN_TO_HOME;
                        }

                        if (mode.getPlaneMode() == PlaneMode.PLANE_MODE_QLAND) {
                            return FlightSegment.LANDING;
                        }

                        if (mode.getPlaneMode() == PlaneMode.PLANE_MODE_CIRCLE
                                || mode.getPlaneMode() == PlaneMode.PLANE_MODE_LOITER
                                || mode.getPlaneMode() == PlaneMode.PLANE_MODE_QLOITER
                                || mode.getPlaneMode() == PlaneMode.PLANE_MODE_STABILIZE
                                || mode.getPlaneMode() == PlaneMode.PLANE_MODE_QSTABILIZE) {
                            return FlightSegment.HOLD;
                        }

                        return FlightSegment.UNKNOWN;
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
            arduPlaneCustomMode);
    }

    @Override
    protected ObservableValue<Boolean> createFlightSegmentTelemetryOldBinding() {
        return Bindings.createBooleanBinding(
            () -> arduPlaneCustomModeOld.get() || mavLandedStateOldProperty().get(),
            arduPlaneCustomModeOld,
            mavLandedStateOldProperty());
    }

    @Override
    protected ObservableValue<MavLandedState> createMavLandedStateBinding() {
        return Bindings.createObjectBinding(
            () -> {
                // TODO find better indication of landed state
                switch (armedStateProperty().get()) {
                case DISARMED:
                    return MavLandedState.MAV_LANDED_STATE_ON_GROUND;
                case ARMED:
                    return MavLandedState.MAV_LANDED_STATE_IN_AIR;
                case UNKNOWN:
                default:
                    return MavLandedState.MAV_LANDED_STATE_UNDEFINED;
                }
            },
            arduPlaneCustomMode);
    }

    @Override
    protected ObservableValue<Boolean> createMavLandedStateOldBinding() {
        return arduPlaneCustomModeOld;
    }

    @Override
    protected Future<Void> runPreArmActionsAsync() {
        return super.runPreArmActionsAsync();
    }

    @Override
    protected List<IMavlinkParameter> createAutopilotParameterList(MavlinkFlightPlan mavlinkFlightPlan) {
        // TODO
        return Collections.emptyList();
    }

    @Override
    protected void applyFlightPlanInitialSettings(MavlinkFlightPlan mavlinkFlightPlan) {}

    @Override
    protected Future<Boolean> evaluateMessageAsync(DroneMessage message) {
        return Futures.successful(true);
    }

    @Override
    protected Future<Void> sendSetLandingModeAsync() {
        throw new NotImplementedException("Landing a fixed wing is not supported");
    }

    @Override
    protected Future<Void> sendSetMissionModeAsync() {
        return droneConnection
            .getCommandProtocolSender()
            .sendSetModeAsync(getAutoBaseMode(), EnumValue.of(PlaneMode.PLANE_MODE_AUTO).value(), 0);
    }

    @Override
    protected Future<Void> sendSetLoiterModeAsync() {
        return droneConnection
            .getCommandProtocolSender()
            .sendSetModeAsync(getAutoBaseMode(), EnumValue.of(PlaneMode.PLANE_MODE_LOITER).value(), 0);
    }

    @Override
    protected Future<Void> sendSetReturnHomeModeAsync() {
        return droneConnection
            .getCommandProtocolSender()
            .sendSetModeAsync(getAutoBaseMode(), EnumValue.of(PlaneMode.PLANE_MODE_RTL).value(), 0);
    }

    @Override
    Future<Void> setActiveFlightPlanAsync(FlightPlanWithWayPointIndex flightPlanWithWayPointIndex) {
        return super.setActiveFlightPlanAsync(flightPlanWithWayPointIndex)
            .thenRunAsync(() -> droneConnection.getMissionProtocolSender().sendMissionSetCurrentAsync(1));
    }
}
