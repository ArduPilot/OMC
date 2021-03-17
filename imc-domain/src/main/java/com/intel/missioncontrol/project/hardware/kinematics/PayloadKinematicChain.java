/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.IOrientation;
import com.intel.missioncontrol.project.hardware.SnapPointId;

/**
 * Kinematic model of a single payload attached to a drone with a gimbal, representing a specific end point within an
 * IKinematicChain. Specifically, the kinematic end point is the payload nodal point, which, for optical cameras,
 * corresponds to a (virtual) location along the optical axis at the tip of the camera's view cone.
 */
public class PayloadKinematicChain implements IPayloadKinematicChain {
    private final IKinematicChain kinematicChain;
    private final SnapPointId nodalPointSnapPointId;
    private final AttitudeConstraints attitudeConstraints;

    public PayloadKinematicChain(IKinematicChain kinematicChain, SnapPointId nodalPointSnapPointId) {
        this.kinematicChain = kinematicChain;
        this.nodalPointSnapPointId = nodalPointSnapPointId;

        this.attitudeConstraints = kinematicChain.calculateAttitudeConstraintsAtDefaultRoll(nodalPointSnapPointId);
    }

    @Override
    public IKinematicChain getKinematicChain() {
        return kinematicChain;
    }

    @Override
    public AttitudeConstraints getAttitudeConstraints() {
        return attitudeConstraints;
    }

    @Override
    public SnapPointId getNodalPointSnapPointId() {
        return nodalPointSnapPointId;
    }

    @Override
    public GimbalStateSolution solveForEarthFramePayloadOrientation(
            IOrientation payloadOrientation, IOrientation droneAttitude, IGimbalStateVector gimbalStateVector) {
        return kinematicChain.solveForEarthFramePayloadOrientation(
            nodalPointSnapPointId, payloadOrientation, droneAttitude, gimbalStateVector);
    }

    /**
     * Determine the earth frame attitude constraints for the payload's optical axis assuming a horizontal drone body
     * and the given payload roll angle about its optical axis.
     */
    private AttitudeConstraints calculateAttitudeConstraintsAtRollAngleDeg(double payloadRollDeg) {
        return kinematicChain.calculateAttitudeConstraintsAtRollAngleDeg(nodalPointSnapPointId, payloadRollDeg);
    }

}
