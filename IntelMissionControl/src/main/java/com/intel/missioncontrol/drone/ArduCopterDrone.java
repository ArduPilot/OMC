/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.drone.connection.DroneMessage;
import com.intel.missioncontrol.drone.connection.MavlinkDroneConnection;
import com.intel.missioncontrol.drone.connection.mavlink.IMavlinkParameter;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import io.dronefleet.mavlink.ardupilotmega.CopterMode;
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

public class ArduCopterDrone extends MavlinkDrone {

    private final AsyncBooleanProperty arduCopterCustomModeOld = new SimpleAsyncBooleanProperty(this);

    private final AsyncObjectProperty<ArduCopterCustomMode> arduCopterCustomMode =
        new SimpleAsyncObjectProperty<>(
            this,
            new PropertyMetadata.Builder<ArduCopterCustomMode>().initialValue(ArduCopterCustomMode.undefined).create());

    private ArduCopterDrone(MavlinkDroneConnection droneConnection, IHardwareConfiguration hardwareConfiguration) {
        super(droneConnection, hardwareConfiguration);

        // Heartbeat
        droneConnection
            .getConnectionProtocolReceiver()
            .registerHeartbeatHandlerAsync(
                receivedPayload -> {
                    Heartbeat heartbeat = receivedPayload.getPayload();
                    arduCopterCustomModeOld.set(false);

                    if (heartbeat.autopilot().entry() != MavAutopilot.MAV_AUTOPILOT_ARDUPILOTMEGA) {
                        arduCopterCustomMode.set(ArduCopterCustomMode.undefined);
                        raiseDroneConnectionExceptionEvent(
                            new DroneConnectionException(Heartbeat.class, true, "Invalid autopilot type"));
                    }

                    if (heartbeat.type().entry() != MavType.MAV_TYPE_QUADROTOR
                            && heartbeat.type().entry() != MavType.MAV_TYPE_HEXAROTOR
                            && heartbeat.type().entry() != MavType.MAV_TYPE_OCTOROTOR) {
                        arduCopterCustomMode.set(ArduCopterCustomMode.undefined);
                        raiseDroneConnectionExceptionEvent(
                            new DroneConnectionException(Heartbeat.class, true, "Invalid vehicle type"));
                    }

                    EnumValue<MavModeFlag> baseMode = heartbeat.baseMode();
                    boolean isCustom = baseMode.flagsEnabled(MavModeFlag.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED);
                    arduCopterCustomMode.set(
                        isCustom
                            ? ArduCopterCustomMode.fromCustomMode(heartbeat.customMode())
                            : ArduCopterCustomMode.undefined);
                },
                hardwareConfiguration.getPlatformDescription().getConnectionProperties().getLinkLostTimeoutSeconds(),
                () -> {
                    // timeout
                    arduCopterCustomModeOld.set(true);
                });
        // failure is handled in base class.
    }

    public static ArduCopterDrone create(
            MavlinkDroneConnection droneConnection, IHardwareConfiguration hardwareConfiguration) {
        ArduCopterDrone drone = new ArduCopterDrone(droneConnection, hardwareConfiguration);
        drone.initializeBindings();
        return drone;
    }

    @Override
    protected ObservableValue<AutopilotState> createAutopilotStateBinding() {
        return Bindings.createObjectBinding(
            () -> {
                if (arduCopterCustomMode.get() == ArduCopterCustomMode.undefined) {
                    return AutopilotState.UNKNOWN;
                }

                switch (arduCopterCustomMode.get().getCopterMode()) {
                case COPTER_MODE_STABILIZE:
                case COPTER_MODE_ACRO:
                case COPTER_MODE_ALT_HOLD:
                case COPTER_MODE_CIRCLE:
                case COPTER_MODE_DRIFT:
                case COPTER_MODE_SPORT:
                case COPTER_MODE_FLIP:
                case COPTER_MODE_AUTOTUNE:
                case COPTER_MODE_BRAKE:
                case COPTER_MODE_THROW:
                case COPTER_MODE_AVOID_ADSB:
                case COPTER_MODE_GUIDED_NOGPS:
                    return AutopilotState.MANUAL;
                case COPTER_MODE_AUTO:
                case COPTER_MODE_GUIDED:
                case COPTER_MODE_LOITER:
                case COPTER_MODE_RTL:
                case COPTER_MODE_LAND:
                case COPTER_MODE_POSHOLD:
                case COPTER_MODE_SMART_RTL:
                    return AutopilotState.AUTOPILOT;
                default:
                    return AutopilotState.MANUAL;
                }
            },
            arduCopterCustomMode);
    }

    @Override
    protected ObservableValue<Boolean> createAutopilotStateTelemetryOldBinding() {
        return arduCopterCustomModeOld;
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
                        ArduCopterCustomMode mode = arduCopterCustomMode.get();
                        if (mode.getCopterMode() == CopterMode.COPTER_MODE_AUTO) {
                            return FlightSegment.PLAN_RUNNING;
                        }

                        if (mode.getCopterMode() == CopterMode.COPTER_MODE_RTL
                                || mode.getCopterMode() == CopterMode.COPTER_MODE_SMART_RTL) {
                            return FlightSegment.RETURN_TO_HOME;
                        }

                        if (mode.getCopterMode() == CopterMode.COPTER_MODE_LAND) {
                            return FlightSegment.LANDING;
                        }

                        if (mode.getCopterMode() == CopterMode.COPTER_MODE_GUIDED
                                || mode.getCopterMode() == CopterMode.COPTER_MODE_POSHOLD
                                || mode.getCopterMode() == CopterMode.COPTER_MODE_LOITER) {
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
            arduCopterCustomMode);
    }

    @Override
    protected ObservableValue<Boolean> createFlightSegmentTelemetryOldBinding() {
        return Bindings.createBooleanBinding(
            () -> arduCopterCustomModeOld.get() || mavLandedStateOldProperty().get(),
            arduCopterCustomModeOld,
            mavLandedStateOldProperty());
    }

    @Override
    protected ObservableValue<MavLandedState> createMavLandedStateBinding() {
        return Bindings.createObjectBinding(
            () -> {
                // TODO find better indication of landed state
                if (positionProperty().get() == null) {
                    return MavLandedState.MAV_LANDED_STATE_UNDEFINED;
                }

                if (positionProperty().get().getAltitude() > 0.1f) {
                    return MavLandedState.MAV_LANDED_STATE_IN_AIR;
                }

                return MavLandedState.MAV_LANDED_STATE_ON_GROUND;
            },
            arduCopterCustomMode);
    }

    @Override
    protected ObservableValue<Boolean> createMavLandedStateOldBinding() {
        return arduCopterCustomModeOld;
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
        return droneConnection
            .getCommandProtocolSender()
            .sendSetModeAsync(getAutoBaseMode(), EnumValue.of(CopterMode.COPTER_MODE_LAND).value(), 0);
    }

    @Override
    protected Future<Void> sendSetMissionModeAsync() {
        return droneConnection.getCommandProtocolSender().sendMissionStartAsync();
    }

    @Override
    protected Future<Void> sendSetLoiterModeAsync() {
        return droneConnection
            .getCommandProtocolSender()
            .sendSetModeAsync(getAutoBaseMode(), EnumValue.of(CopterMode.COPTER_MODE_LOITER).value(), 0);
    }

    @Override
    protected Future<Void> sendSetReturnHomeModeAsync() {
        return droneConnection
            .getCommandProtocolSender()
            .sendSetModeAsync(getAutoBaseMode(), EnumValue.of(CopterMode.COPTER_MODE_RTL).value(), 0);
    }
}
