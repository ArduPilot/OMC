/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.IOrientation;
import com.intel.missioncontrol.project.hardware.SnapPointId;

public interface IPayloadKinematicChain {

    /**
     * get the full kinematic chain of which this payload chain is a part of.
     */
    IKinematicChain getKinematicChain();

    AttitudeConstraints getAttitudeConstraints();

    /**
     * get the payload nodal point snap point (on the optical axis of the payload).
     */
    SnapPointId getNodalPointSnapPointId();

    /**
     * Given a drone body orientation (attitude), determine a gimbal state vector that results in a payload optical axis
     * orientation as close to the given orientation as possible. Orientations are given in earth frame.
     * https://en.wikipedia.org/wiki/Flight_dynamics_(fixed-wing_aircraft)
     *
     * <p>If the yaw angle of the payload orientation is NaN, yaw is ignored and taken to be arbitrary.
     *
     * <p>The resulting gimbal state vector is not necessarily unique. Preference is given to a solution near
     * referenceStateVector (or, if that is null, a default configuration).
     *
     * <p>The solution affects all parts of the kinematic chain, including other payloads.
     */
    GimbalStateSolution solveForEarthFramePayloadOrientation(
            IOrientation payloadOrientation, IOrientation droneAttitude, IGimbalStateVector gimbalStateVector);
}
