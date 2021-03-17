/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.drone.connection.MavlinkDroneConnection;
import com.intel.missioncontrol.drone.connection.mavlink.IMavlinkParameter;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import io.dronefleet.mavlink.ardupilotmega.PlaneMode;
import io.dronefleet.mavlink.util.EnumValue;
import java.util.ArrayList;
import java.util.List;
import org.asyncfx.concurrent.Future;

public class ArdupilotQuadPlaneDrone extends ArduPlaneDrone {

    private ArdupilotQuadPlaneDrone(
            MavlinkDroneConnection droneConnection, IHardwareConfiguration hardwareConfiguration) {
        super(droneConnection, hardwareConfiguration);
    }

    public static ArduPlaneDrone create(
            MavlinkDroneConnection droneConnection, IHardwareConfiguration hardwareConfiguration) {
        ArdupilotQuadPlaneDrone drone = new ArdupilotQuadPlaneDrone(droneConnection, hardwareConfiguration);
        drone.initializeBindings();
        return drone;
    }

    @Override
    protected List<IMavlinkParameter> createAutopilotParameterList(MavlinkFlightPlan mavlinkFlightPlan) {
        List<IMavlinkParameter> res = new ArrayList<>(super.createAutopilotParameterList(mavlinkFlightPlan));

        // TODO: check if needed
        // res.add(Parameter.createInt8("Q_ENABLE", 2)); //2: VTOL_AUTO

        return res;
    }

    @Override
    protected Future<Void> sendSetLandingModeAsync() {
        return droneConnection
            .getCommandProtocolSender()
            .sendSetModeAsync(getAutoBaseMode(), EnumValue.of(PlaneMode.PLANE_MODE_QLAND).value(), 0);
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
            .sendSetModeAsync(getAutoBaseMode(), EnumValue.of(PlaneMode.PLANE_MODE_QLOITER).value(), 0);
    }

    @Override
    protected Future<Void> sendSetReturnHomeModeAsync() {
        return droneConnection
            .getCommandProtocolSender()
            .sendSetModeAsync(getAutoBaseMode(), EnumValue.of(PlaneMode.PLANE_MODE_QRTL).value(), 0);
    }
}
