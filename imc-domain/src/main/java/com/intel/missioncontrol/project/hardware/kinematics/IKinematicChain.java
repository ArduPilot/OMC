/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.IOrientation;
import com.intel.missioncontrol.project.hardware.SnapPointId;
import java.util.List;
import java.util.Map;

public interface IKinematicChain {

    List<KinematicSegment> getControllableSegments();

    int getGimbalStateVectorLength();

    KinematicChainState createState(IGimbalStateVector gimbalStateVector);

    Map<SnapPointId, KinematicSegment> getKinematicSegmentMap();

    /**
     * Determine the earth frame attitude constraints for the payload's optical axis assuming a horizontal drone body
     * and default roll angle of the payload about its optical axis (i.e. the payload angle about its optical axis at
     * the default gimbal state vector with zero joint angles).
     */
    AttitudeConstraints calculateAttitudeConstraintsAtDefaultRoll(SnapPointId snapPointId);

    AttitudeConstraints calculateAttitudeConstraintsAtRollAngleDeg(SnapPointId snapPointId, double earthFrameRollDeg);

    AffineTransform getTransformFromBody(SnapPointId payloadSnapPointId, IGimbalStateVector stateVector);

    GimbalStateSolution solveForBodyFramePayloadPitchAndRoll(
            SnapPointId payloadSnapPointId, double pitchDeg, double rollDeg, IGimbalStateVector referenceStateVector);

    GimbalStateSolution solveForBodyFramePayloadOrientation(
            SnapPointId payloadSnapPointId, IOrientation payloadOrientation, IGimbalStateVector referenceStateVector);

    GimbalStateSolution solveForEarthFramePayloadOrientation(
            SnapPointId payloadSnapPointId,
            IOrientation payloadOrientation,
            IOrientation droneBodyOrientation,
            IGimbalStateVector referenceStateVector);

    IGimbalStateVector getDefaultGimbalStateVector();
}
