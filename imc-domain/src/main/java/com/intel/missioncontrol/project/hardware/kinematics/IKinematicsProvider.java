/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.IOrientation;
import com.intel.missioncontrol.geospatial.Position;
import com.intel.missioncontrol.project.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.project.hardware.SnapPointId;

public interface IKinematicsProvider {

    IKinematicChain getOrCreateKinematicChain(IHardwareConfiguration hardwareConfiguration);

    IPayloadKinematicChain getOrCreatePayloadKinematicChain(
            IHardwareConfiguration hardwareConfiguration, int payloadMountIndex, int payloadIndex);

    /**
     * Use the given kinematic chain state to transform a WGS84 Position associated with a snap point on a kinematic
     * chain of a drone with a given earth frame orientation to a WGS84 Position corresponding to another snap point.
     */
    Position transformWGS84Position(
            KinematicChainState kinematicChainState,
            SnapPointId sourceSnapPointId,
            IOrientation sourceOrientation,
            Position sourceSnapPointPosition,
            SnapPointId targetSnapPointId);
}
