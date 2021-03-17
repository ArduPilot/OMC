/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.Arc;
import com.intel.missioncontrol.geometry.EulerAngles;
import com.intel.missioncontrol.geometry.Vec3;
import com.intel.missioncontrol.project.hardware.SnapPointId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class KinematicsTest {

    private static Vec3 ORIGIN = new Vec3(0.0, 0.0, 0.0);
    private static Vec3 X_AXIS = new Vec3(1.0, 0.0, 0.0);
    private static Vec3 Y_AXIS = new Vec3(0.0, 1.0, 0.0);
    private static Vec3 Z_AXIS = new Vec3(0.0, 0.0, 1.0);

    private static KinematicSegment bodySegment =
        new KinematicSegment(
            SnapPointId.BODY_ORIGIN,
            // offset
            ORIGIN,
            // rotationAxis
            Z_AXIS,
            -1,
            Arc.fromAnglesDeg(0.0, 0.0),
            null);

    private static KinematicSegment antennaSegment =
        new KinematicSegment(
            SnapPointId.GNSS_ANTENNA,
            // offset: antenna 0.3m above bodySegment snap point (= origin)
            new Vec3(0.0, 0.0, 0.3),
            // rotationAxis
            Z_AXIS,
            -1,
            Arc.fromAnglesDeg(0.0, 0.0),
            bodySegment);

    private static final SnapPointId ROLL_GIMBAL_SNAP_POINT_ID = new SnapPointId("rollGimbalSnapPoint");
    private static final SnapPointId PITCH_GIMBAL_SNAP_POINT_ID = new SnapPointId("pitchGimbalSnapPoint");
    private static final SnapPointId PAYLOAD_SNAP_POINT_ID = new SnapPointId("P0");

    private static KinematicChain fixedForwardKinematicChain = createFixedForwardKinematicChain();
    private static KinematicChain fixedDownwardKinematicChain = createFixedDownwardKinematicChain();
    private static KinematicChain fixedDownwardRolledKinematicChain = createFixedDownwardRolledKinematicChain();
    private static KinematicChain rollPitchKinematicChain = createRollPitchKinematicChain();
    private static KinematicChain pitchRollKinematicChain = createPitchRollKinematicChain();

    private static KinematicChain createFixedForwardKinematicChain() {
        // Build a chain consisting of a drone body, an antenna and a fixed payload with an optical axis in default
        // X-direction.
        List<KinematicSegment> kinematicSegments = new ArrayList<>();

        kinematicSegments.add(bodySegment);
        kinematicSegments.add(antennaSegment);

        // payload snap point:
        kinematicSegments.add(
            new KinematicSegment(
                PAYLOAD_SNAP_POINT_ID,
                // offset: payload snap point shifted by 0.5m from bodySegment snap point (= origin) in x-direction
                // (forward flight direction).
                new Vec3(0.5, 0.0, 0.0),
                // rotationAxis
                Z_AXIS,
                -1,
                Arc.fromAnglesDeg(0.0, 0.0),
                bodySegment));

        return new KinematicChain(kinematicSegments);
    }

    private static KinematicChain createFixedDownwardKinematicChain() {
        // Build a chain consisting of a drone body, an antenna and a fixed payload with an optical axis in Z-direction
        // (looking down like Sirius).
        List<KinematicSegment> kinematicSegments = new ArrayList<>();

        kinematicSegments.add(bodySegment);
        kinematicSegments.add(antennaSegment);

        // payload snap point:
        kinematicSegments.add(
            new KinematicSegment(
                PAYLOAD_SNAP_POINT_ID,
                // offset: payload snap point shifted by 0.5m from bodySegment snap point (= origin) in x-direction
                // (forward flight direction).
                new Vec3(0.5, 0.0, 0.0),
                // rotationAxis
                Y_AXIS,
                -1,
                Arc.fromAnglesDeg(-90.0, -90.0),
                bodySegment));

        return new KinematicChain(kinematicSegments);
    }

    private static KinematicChain createFixedDownwardRolledKinematicChain() {
        // Build a chain consisting of a drone body, an antenna and a fixed payload with an optical axis in Z-direction
        // (looking down like Sirius), and 90° rotation about the optical axis.
        List<KinematicSegment> kinematicSegments = new ArrayList<>();

        kinematicSegments.add(bodySegment);
        kinematicSegments.add(antennaSegment);

        // intermediate snap point for optical axis direction:
        SnapPointId intermediateSnapPointId = new SnapPointId("P0i");

        KinematicSegment intermediateSegment =
            new KinematicSegment(
                intermediateSnapPointId,
                // offset: payload snap point shifted by 0.5m from bodySegment snap point (= origin) in x-direction
                // (forward flight direction).
                new Vec3(0.5, 0.0, 0.0),
                // rotationAxis (= pitch axis)
                Y_AXIS,
                -1,
                Arc.fromAnglesDeg(-90.0, -90.0),
                bodySegment);

        kinematicSegments.add(intermediateSegment);

        // payload snap point: intermediate with rotation about optical axis:
        kinematicSegments.add(
            new KinematicSegment(
                PAYLOAD_SNAP_POINT_ID,
                // offset: none with respect to intermediate point
                ORIGIN,
                // rotationAxis (=optical axis)
                X_AXIS,
                -1,
                Arc.fromAnglesDeg(90.0, 90.0),
                intermediateSegment));

        return new KinematicChain(kinematicSegments);
    }

    private static KinematicChain createRollPitchKinematicChain() {
        // Build a chain consisting of a drone body, a fixed antenna and a payload on a "roll" and "pitch" axis gimbal.
        List<KinematicSegment> kinematicSegments = new ArrayList<>();
        kinematicSegments.add(bodySegment);
        kinematicSegments.add(antennaSegment);

        // movable roll gimbal
        KinematicSegment rollGimbal =
            new KinematicSegment(
                ROLL_GIMBAL_SNAP_POINT_ID,
                // offset
                ORIGIN,
                // rotationAxis, X AXIS corresponds to axis from tail to nose: clockwise rotation is "right wing down".
                X_AXIS,
                // first controllable segment
                0,
                // roll constraints:
                Arc.fromAnglesDeg(-32.0, 30.0),
                bodySegment);

        kinematicSegments.add(rollGimbal);

        // movable pitch gimbal
        KinematicSegment pitchGimbal =
            new KinematicSegment(
                PITCH_GIMBAL_SNAP_POINT_ID,
                // offset
                ORIGIN,
                // rotationAxis, Y AXIS corresponds to axis from left to right wing tip: clockwise rotation is "pitch
                // up".
                Y_AXIS,
                // second controllable segment
                1,
                // pitch constraints: pointing vertically down to 30° up over horizon.
                Arc.fromAnglesDeg(-90.0, 30.0),
                rollGimbal);

        kinematicSegments.add(pitchGimbal);

        // payload snap point:
        kinematicSegments.add(
            new KinematicSegment(
                PAYLOAD_SNAP_POINT_ID,
                // offset: payload snap point shifted by 0.5m from rollGimbal snap point in x-direction
                // (forward flight direction when all controllable angles are zero).
                new Vec3(0.5, 0.0, 0.0),
                // rotationAxis
                Z_AXIS,
                -1,
                Arc.fromAnglesDeg(0.0, 0.0),
                pitchGimbal));

        return new KinematicChain(kinematicSegments);
    }

    private static KinematicChain createPitchRollKinematicChain() {
        // Build a chain consisting of a drone body, a fixed antenna and a payload on a "pitch" and "roll" axis gimbal.
        List<KinematicSegment> kinematicSegments = new ArrayList<>();
        kinematicSegments.add(bodySegment);
        kinematicSegments.add(antennaSegment);

        // movable pitch gimbal
        KinematicSegment pitchGimbal =
            new KinematicSegment(
                PITCH_GIMBAL_SNAP_POINT_ID,
                // offset
                ORIGIN,
                // rotationAxis, Y AXIS corresponds to axis from left to right wing tip: clockwise rotation is "pitch
                // up".
                Y_AXIS,
                // second controllable segment
                0,
                // pitch constraints: pointing vertically down to 30° up over horizon.
                Arc.fromAnglesDeg(-90.0, 30.0),
                bodySegment);

        // movable roll gimbal
        KinematicSegment rollGimbal =
            new KinematicSegment(
                ROLL_GIMBAL_SNAP_POINT_ID,
                // offset
                ORIGIN,
                // rotationAxis, X AXIS corresponds to axis from tail to nose: clockwise rotation is "right wing down".
                X_AXIS,
                // first controllable segment
                1,
                // roll constraints:
                Arc.fromAnglesDeg(-12.0, 10.0),
                pitchGimbal);

        kinematicSegments.add(pitchGimbal);
        kinematicSegments.add(rollGimbal);

        // payload snap point:
        kinematicSegments.add(
            new KinematicSegment(
                PAYLOAD_SNAP_POINT_ID,
                // offset: payload snap point shifted by 0.5m from pitchGimbal snap point in x-direction
                // (forward flight direction when all controllable angles are zero).
                new Vec3(0.5, 0.0, 0.0),
                // rotationAxis
                Z_AXIS,
                -1,
                Arc.fromAnglesDeg(0.0, 0.0),
                rollGimbal));

        return new KinematicChain(kinematicSegments);
    }

    @Test
    void Fixed_Forward_Kinematic_Chain_Has_Correct_Attitude_Constraints() {
        KinematicChain kinematicChain = fixedForwardKinematicChain;
        AttitudeConstraints attitudeConstraints =
            kinematicChain.calculateAttitudeConstraintsAtDefaultRoll(PAYLOAD_SNAP_POINT_ID);

        double toleranceDeg = 1e-3;
        Assertions.assertEquals(0.0, attitudeConstraints.getRollDeg(), toleranceDeg);
        Assertions.assertEquals(0.0, attitudeConstraints.getPitchRange().getNormalizedStartAngleDeg(), toleranceDeg);
        Assertions.assertEquals(0.0, attitudeConstraints.getPitchRange().getNormalizedEndAngleDeg(), toleranceDeg);
    }

    @Test
    void Fixed_Forward_Kinematic_Chain_Transforms_Correctly() {
        KinematicChain kinematicChain = fixedForwardKinematicChain;
        GimbalStateVector stateVector = kinematicChain.getDefaultGimbalStateVector();

        Vec3 expectedPayloadSnapPointPosition = new Vec3(0.5, 0.0, 0.0);
        Vec3 expectedOpticalAxisDirection = X_AXIS;
        double expectedPayloadRoll = 0.0;

        checkPayloadTransform(
            kinematicChain,
            stateVector,
            expectedPayloadSnapPointPosition,
            expectedOpticalAxisDirection,
            expectedPayloadRoll);
    }

    @Test
    void Fixed_Forward_Kinematic_Chain_Has_Correct_Zero_Pitch_Solution() {
        KinematicChain kinematicChain = fixedForwardKinematicChain;

        // zero pitch = horizontal
        GimbalStateSolution sol =
            kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, 0.0, 0.0, null);
        Assertions.assertTrue(sol.exactSolutionFound());

        // empty state vector because there are no controllable segments
        Assertions.assertEquals(((GimbalStateVector)sol.getStateVector()).getAnglesDeg().length, 0);
    }

    @Test
    void Fixed_Forward_Kinematic_Chain_Has_No_Downward_Solution() {
        KinematicChain kinematicChain = fixedForwardKinematicChain;

        // -90° pitch = downward, not possible
        GimbalStateSolution sol =
            kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, -90.0, 0.0, null);
        Assertions.assertFalse(sol.exactSolutionFound());
    }

    @Test
    void Fixed_Downward_Kinematic_Chain_Has_Correct_Attitude_Constraints() {
        KinematicChain kinematicChain = fixedDownwardKinematicChain;
        AttitudeConstraints attitudeConstraints =
            kinematicChain.calculateAttitudeConstraintsAtDefaultRoll(PAYLOAD_SNAP_POINT_ID);

        double toleranceDeg = 1e-3;
        Assertions.assertEquals(0.0, attitudeConstraints.getRollDeg(), toleranceDeg);
        Assertions.assertEquals(-90.0, attitudeConstraints.getPitchRange().getNormalizedStartAngleDeg(), toleranceDeg);
        Assertions.assertEquals(-90.0, attitudeConstraints.getPitchRange().getNormalizedEndAngleDeg(), toleranceDeg);
    }

    @Test
    void Fixed_Downward_Kinematic_Chain_Has_No_Zero_Pitch_Solution() {
        KinematicChain kinematicChain = fixedDownwardKinematicChain;

        // zero pitch = horizontal, not possible
        GimbalStateSolution sol =
            kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, 0.0, 0.0, null);
        Assertions.assertFalse(sol.exactSolutionFound());
    }

    @Test
    void Fixed_Downward_Kinematic_Chain_Has_Correct_Downward_Solution() {
        KinematicChain kinematicChain = fixedDownwardKinematicChain;
        // -90° pitch = downward
        GimbalStateSolution sol =
            kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, -90.0, 0.0, null);
        Assertions.assertTrue(sol.exactSolutionFound());

        // empty state vector because there are no controllable segments
        Assertions.assertEquals(((GimbalStateVector)sol.getStateVector()).getAnglesDeg().length, 0);
    }

    @Test
    void Fixed_Downward_Kinematic_Chain_Has_Consistent_Solution_With_Horizontal_Drone_Orientation() {
        KinematicChain kinematicChain = fixedDownwardKinematicChain;

        // drone attitude:
        EulerAngles droneBodyOrientation = new EulerAngles(-15, 20, 0);

        AffineTransform tEarthToDroneBody = AffineTransform.fromOrientation(droneBodyOrientation);
        AffineTransform tDroneBodyToPayload =
            kinematicChain.getTransformFromBody(PAYLOAD_SNAP_POINT_ID, kinematicChain.getDefaultGimbalStateVector());

        EulerAngles payloadOrientation =
            tEarthToDroneBody.chain(tDroneBodyToPayload).getEulerAngles(AffineTransform.GimbalLockMode.ROLL_ZERO);

        double toleranceDeg = 0.1;
        Assertions.assertEquals(-15.0, payloadOrientation.getYawDeg(), toleranceDeg);
        Assertions.assertEquals(-70.0, payloadOrientation.getPitchDeg(), toleranceDeg);
        Assertions.assertEquals(0.0, payloadOrientation.getRollDeg(), toleranceDeg);

        // Check if solver accepts this:
        GimbalStateSolution sol =
            kinematicChain.solveForEarthFramePayloadOrientation(
                PAYLOAD_SNAP_POINT_ID, payloadOrientation, droneBodyOrientation, null);
        Assertions.assertTrue(sol.exactSolutionFound());

        // empty state vector because there are no controllable segments
        Assertions.assertEquals(((GimbalStateVector)sol.getStateVector()).getAnglesDeg().length, 0);
    }

    @Test
    void Fixed_Downward_Kinematic_Chain_Has_Consistent_Solution_With_Drone_Orientation() {
        KinematicChain kinematicChain = fixedDownwardKinematicChain;

        // drone attitude:
        EulerAngles droneBodyOrientation = new EulerAngles(-15, 20, 5);

        AffineTransform tEarthToDroneBody = AffineTransform.fromOrientation(droneBodyOrientation);
        AffineTransform tDroneBodyToPayload =
            kinematicChain.getTransformFromBody(PAYLOAD_SNAP_POINT_ID, kinematicChain.getDefaultGimbalStateVector());

        EulerAngles payloadOrientation =
            tEarthToDroneBody.chain(tDroneBodyToPayload).getEulerAngles(AffineTransform.GimbalLockMode.ROLL_ZERO);

        // The 5° roll angle of the drone does NOT translate into a 5° roll of the payload, because the payload is
        // mounted at a 90° angle.
        double toleranceDeg = 0.1;
        Assertions.assertEquals(-29.3, payloadOrientation.getYawDeg(), toleranceDeg);
        Assertions.assertEquals(-69.4, payloadOrientation.getPitchDeg(), toleranceDeg);
        Assertions.assertEquals(13.5, payloadOrientation.getRollDeg(), toleranceDeg);

        // Check if solver accepts this:
        GimbalStateSolution sol =
            kinematicChain.solveForEarthFramePayloadOrientation(
                PAYLOAD_SNAP_POINT_ID, payloadOrientation, droneBodyOrientation, null);
        Assertions.assertTrue(sol.exactSolutionFound());

        // empty state vector because there are no controllable segments
        Assertions.assertEquals(((GimbalStateVector)sol.getStateVector()).getAnglesDeg().length, 0);

        // Ensure solver does not accept impossible orientation:
        payloadOrientation = new EulerAngles(-15, -70, 0);
        sol =
            kinematicChain.solveForEarthFramePayloadOrientation(
                PAYLOAD_SNAP_POINT_ID, payloadOrientation, droneBodyOrientation, null);
        Assertions.assertFalse(sol.exactSolutionFound());
    }

    @Test
    void Fixed_Downward_Kinematic_Chain_Transforms_Correctly() {
        KinematicChain kinematicChain = fixedDownwardKinematicChain;
        GimbalStateVector stateVector = kinematicChain.getDefaultGimbalStateVector();

        Vec3 expectedPayloadSnapPointPosition = new Vec3(0.5, 0.0, 0.0);
        Vec3 expectedOpticalAxisDirection = Z_AXIS;
        double expectedPayloadRoll = 0.0;

        checkPayloadTransform(
            kinematicChain,
            stateVector,
            expectedPayloadSnapPointPosition,
            expectedOpticalAxisDirection,
            expectedPayloadRoll);
    }

    @Test
    void Fixed_Downward_Rolled_Kinematic_Chain_Has_Correct_Attitude_Constraints() {
        KinematicChain kinematicChain = fixedDownwardRolledKinematicChain;
        AttitudeConstraints attitudeConstraints =
            kinematicChain.calculateAttitudeConstraintsAtDefaultRoll(PAYLOAD_SNAP_POINT_ID);

        double toleranceDeg = 1e-3;
        Assertions.assertEquals(90.0, attitudeConstraints.getRollDeg(), toleranceDeg);
        Assertions.assertEquals(-90.0, attitudeConstraints.getPitchRange().getNormalizedStartAngleDeg(), toleranceDeg);
        Assertions.assertEquals(-90.0, attitudeConstraints.getPitchRange().getNormalizedEndAngleDeg(), toleranceDeg);
    }

    @Test
    void Fixed_Downward_Rolled_Kinematic_Chain_Has_No_Zero_Pitch_Solution() {
        KinematicChain kinematicChain = fixedDownwardRolledKinematicChain;

        // zero pitch = horizontal, not possible
        GimbalStateSolution sol =
            kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, 0.0, 0.0, null);
        Assertions.assertFalse(sol.exactSolutionFound());
    }

    @Test
    void Fixed_Downward_Rolled_Kinematic_Chain_Has_Correct_Downward_Solution() {
        KinematicChain kinematicChain = fixedDownwardRolledKinematicChain;
        // -90° pitch = downward
        GimbalStateSolution sol =
            kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, -90.0, 0.0, null);
        Assertions.assertTrue(sol.exactSolutionFound());

        // empty state vector because there are no controllable segments
        Assertions.assertEquals(((GimbalStateVector)sol.getStateVector()).getAnglesDeg().length, 0);
    }

    @Test
    void Fixed_Downward_Rolled_Kinematic_Chain_Transforms_Correctly() {
        KinematicChain kinematicChain = fixedDownwardRolledKinematicChain;
        GimbalStateVector stateVector = kinematicChain.getDefaultGimbalStateVector();

        Vec3 expectedPayloadSnapPointPosition = new Vec3(0.5, 0.0, 0.0);
        Vec3 expectedOpticalAxisDirection = Z_AXIS;
        double expectedPayloadRoll = 90.0;

        checkPayloadTransform(
            kinematicChain,
            stateVector,
            expectedPayloadSnapPointPosition,
            expectedOpticalAxisDirection,
            expectedPayloadRoll);
    }

    @Test
    void Kinematic_Chain_Transforms_Are_Self_Consistent() {
        KinematicChain kinematicChain = rollPitchKinematicChain;

        GimbalStateVector stateVector = new GimbalStateVector(new double[] {10.0, 30.0}); // roll, pitch

        KinematicChainState state = kinematicChain.createState(stateVector);

        AffineTransform transform1 = state.getTransformPath(PAYLOAD_SNAP_POINT_ID, SnapPointId.GNSS_ANTENNA);
        AffineTransform transform2 = state.getTransformPath(SnapPointId.GNSS_ANTENNA, PAYLOAD_SNAP_POINT_ID);

        double tolerance = 1e-3;
        Vec3 testVector = new Vec3(11.0, 12.0, 13.0);
        // chain should be identity transform.
        Vec3 resultVector = transform1.chain(transform2).transformPoint(testVector);
        Assertions.assertEquals(testVector.x, resultVector.x, tolerance);
        Assertions.assertEquals(testVector.y, resultVector.y, tolerance);
        Assertions.assertEquals(testVector.z, resultVector.z, tolerance);
    }

    @Test
    void Roll_Pitch_Kinematic_Chain_Default_Transforms_Correctly() {
        KinematicChain kinematicChain = rollPitchKinematicChain;
        // default: zero joint angles
        GimbalStateVector stateVector = kinematicChain.getDefaultGimbalStateVector();

        Vec3 expectedPayloadSnapPointPosition = new Vec3(0.5, 0.0, 0.0);
        Vec3 expectedOpticalAxisDirection = X_AXIS;
        double expectedPayloadRoll = 0.0;

        checkPayloadTransform(
            kinematicChain,
            stateVector,
            expectedPayloadSnapPointPosition,
            expectedOpticalAxisDirection,
            expectedPayloadRoll);
    }

    @Test
    void Roll_Pitch_Kinematic_Chain_Has_Correct_Attitude_Constraints() {
        KinematicChain kinematicChain = rollPitchKinematicChain;
        AttitudeConstraints attitudeConstraints =
            kinematicChain.calculateAttitudeConstraintsAtDefaultRoll(PAYLOAD_SNAP_POINT_ID);

        double toleranceDeg = 0.1;
        Assertions.assertEquals(0.0, attitudeConstraints.getRollDeg(), toleranceDeg);
        Assertions.assertEquals(-90.0, attitudeConstraints.getPitchRange().getNormalizedStartAngleDeg(), toleranceDeg);
        Assertions.assertEquals(30.0, attitudeConstraints.getPitchRange().getNormalizedEndAngleDeg(), toleranceDeg);
    }

    @Test
    void Roll_Pitch_Kinematic_Chain_Has_Correct_Solution1() {
        KinematicChain kinematicChain = rollPitchKinematicChain;
        double toleranceDeg = 0.1;

        // zero pitch = horizontal, both gimbal segment angles zero
        GimbalStateSolution sol =
            kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, 0.0, 0.0, null);
        Assertions.assertTrue(sol.exactSolutionFound());
        double[] anglesDeg = ((GimbalStateVector)sol.getStateVector()).getAnglesDeg();
        Assertions.assertEquals(0.0, anglesDeg[0], toleranceDeg);
        Assertions.assertEquals(0.0, anglesDeg[1], toleranceDeg);
    }

    @Test
    void Roll_Pitch_Kinematic_Chain_Has_Correct_Solution2() {
        KinematicChain kinematicChain = rollPitchKinematicChain;
        double toleranceDeg = 0.1;

        // -90° pitch = vertically down
        GimbalStateSolution sol =
            kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, -90.0, 0.0, null);
        Assertions.assertTrue(sol.exactSolutionFound());
        double[] anglesDeg = ((GimbalStateVector)sol.getStateVector()).getAnglesDeg();
        Assertions.assertEquals(0.0, anglesDeg[0], toleranceDeg);
        Assertions.assertEquals(-90.0, anglesDeg[1], toleranceDeg);
    }

    @Test
    void Roll_Pitch_Kinematic_Chain_Has_Correct_Solution3() {
        KinematicChain kinematicChain = rollPitchKinematicChain;
        double toleranceDeg = 0.1;

        // 30° pitch = slightly up
        GimbalStateSolution sol =
            kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, 30.0, 0.0, null);
        Assertions.assertTrue(sol.exactSolutionFound());
        double[] anglesDeg = ((GimbalStateVector)sol.getStateVector()).getAnglesDeg();
        Assertions.assertEquals(0.0, anglesDeg[0], toleranceDeg);
        Assertions.assertEquals(30.0, anglesDeg[1], toleranceDeg);
    }

    @Test
    void Roll_Pitch_Kinematic_Chain_Has_Correct_Solution4() {
        KinematicChain kinematicChain = rollPitchKinematicChain;
        double toleranceDeg = 0.1;

        // -88° pitch = almost vertically down
        GimbalStateSolution sol =
            kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, -88.0, 0.0, null);
        Assertions.assertTrue(sol.exactSolutionFound());
        double[] anglesDeg = ((GimbalStateVector)sol.getStateVector()).getAnglesDeg();
        Assertions.assertEquals(0.0, anglesDeg[0], toleranceDeg);
        Assertions.assertEquals(-88.0, anglesDeg[1], toleranceDeg);
    }

    @Test
    void Roll_Pitch_Kinematic_Chain_Has_Correct_Solution5() {
        KinematicChain kinematicChain = rollPitchKinematicChain;
        double toleranceDeg = 0.1;

        // 90° pitch = vertically up, not possible
        GimbalStateSolution sol =
            kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, 90.0, 0.0, null);
        Assertions.assertFalse(sol.exactSolutionFound());
    }

    @Test
    void Roll_Pitch_Kinematic_Chain_Has_Correct_Solution6() {
        KinematicChain kinematicChain = rollPitchKinematicChain;
        double toleranceDeg = 0.1;

        // same as yaw = 180, pitch = 0, roll = 0.
        EulerAngles droneBodyOrientation = new EulerAngles(0, 180, 180);
        GimbalStateSolution sol =
            kinematicChain.solveForEarthFramePayloadOrientation(
                PAYLOAD_SNAP_POINT_ID, new EulerAngles(Double.NaN, -90, 0), droneBodyOrientation, null);
        Assertions.assertTrue(sol.exactSolutionFound());
        double[] anglesDeg = ((GimbalStateVector)sol.getStateVector()).getAnglesDeg();
        Assertions.assertEquals(0.0, anglesDeg[0], toleranceDeg);
        Assertions.assertEquals(-90.0, anglesDeg[1], toleranceDeg);
    }

    @Test
    void Pitch_Roll_Kinematic_Chain_Has_Correct_Solution() {
        KinematicChain kinematicChain = pitchRollKinematicChain;

        // zero pitch = horizontal, both gimbal segment angles zero
        GimbalStateSolution sol =
            kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, 0.0, 0.0, null);
        Assertions.assertTrue(sol.exactSolutionFound());
        double toleranceDeg = 0.1;
        double[] anglesDeg = ((GimbalStateVector)sol.getStateVector()).getAnglesDeg();
        Assertions.assertEquals(0.0, anglesDeg[0], toleranceDeg);
        Assertions.assertEquals(0.0, anglesDeg[1], toleranceDeg);

        // -90° pitch = vertically down
        sol = kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, -90.0, 0.0, null);
        Assertions.assertTrue(sol.exactSolutionFound());
        anglesDeg = ((GimbalStateVector)sol.getStateVector()).getAnglesDeg();
        Assertions.assertEquals(-90.0, anglesDeg[0], toleranceDeg);
        Assertions.assertEquals(0.0, anglesDeg[1], toleranceDeg);

        // 30° pitch = slightly up
        sol = kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, 30.0, 0.0, null);
        Assertions.assertTrue(sol.exactSolutionFound());
        anglesDeg = ((GimbalStateVector)sol.getStateVector()).getAnglesDeg();
        Assertions.assertEquals(30.0, anglesDeg[0], toleranceDeg);
        Assertions.assertEquals(0.0, anglesDeg[1], toleranceDeg);

        // 90° pitch = vertically up, not possible
        sol = kinematicChain.solveForBodyFramePayloadPitchAndRoll(PAYLOAD_SNAP_POINT_ID, 90.0, 0.0, null);
        Assertions.assertFalse(sol.exactSolutionFound());
    }

    @Test
    void Roll_Pitch_Kinematic_Chain_Has_Correct_Solution_With_Drone_Orientation() {
        KinematicChain kinematicChain = rollPitchKinematicChain;

        // drone attitude:
        EulerAngles droneAttitude = new EulerAngles(-15, 20, 5);

        // target payload orientation: -30° pitch = looking slightly down from horizontal
        EulerAngles targetPayloadOrientation = new EulerAngles(Double.NaN, -30.0, 0.0);

        GimbalStateSolution sol =
            kinematicChain.solveForEarthFramePayloadOrientation(
                PAYLOAD_SNAP_POINT_ID, targetPayloadOrientation, droneAttitude, null);
        Assertions.assertTrue(sol.exactSolutionFound());
        double toleranceDeg = 0.1;
        double[] anglesDeg = ((GimbalStateVector)sol.getStateVector()).getAnglesDeg();

        // remove drone roll with gimbal
        Assertions.assertEquals(-5.0, anglesDeg[0], toleranceDeg);

        // pitch down from 10° to -30°:
        Assertions.assertEquals(-50.0, anglesDeg[1], toleranceDeg);
    }

    @Test
    void Pitch_Roll_Kinematic_Chain_Has_Correct_Attitude_Constraints() {
        KinematicChain kinematicChain = pitchRollKinematicChain;
        AttitudeConstraints attitudeConstraints =
            kinematicChain.calculateAttitudeConstraintsAtDefaultRoll(PAYLOAD_SNAP_POINT_ID);

        double toleranceDeg = 0.1;
        Assertions.assertEquals(0.0, attitudeConstraints.getRollDeg(), toleranceDeg);
        Assertions.assertEquals(-90.0, attitudeConstraints.getPitchRange().getNormalizedStartAngleDeg(), toleranceDeg);
        Assertions.assertEquals(30.0, attitudeConstraints.getPitchRange().getNormalizedEndAngleDeg(), toleranceDeg);
    }

    /** transform the position of the test payload snap point into the body frame. */
    private Vec3 getPayloadSnapPointPositionInBodyFrame(
            IKinematicChain kinematicChain, IGimbalStateVector gimbalStateVector) {
        return kinematicChain
            .createState(gimbalStateVector)
            .getTransformPath(PAYLOAD_SNAP_POINT_ID, SnapPointId.BODY_ORIGIN)
            .transformPoint(ORIGIN); // origin of the local payload snap point frame
    }

    private Vec3 getPayloadOpticalAxis(IKinematicChain kinematicChain, IGimbalStateVector gimbalStateVector) {
        Vec3 p0 = getPayloadSnapPointPositionInBodyFrame(kinematicChain, gimbalStateVector);
        Vec3 px =
            kinematicChain
                .createState(gimbalStateVector)
                .getTransformPath(PAYLOAD_SNAP_POINT_ID, SnapPointId.BODY_ORIGIN)
                .transformPoint(
                    X_AXIS); // X-Axis = default optical axis direction in the local payload snap point frame

        return px.subtract(p0);
    }

    private double getPayloadRollDeg(IKinematicChain kinematicChain, IGimbalStateVector gimbalStateVector) {
        return kinematicChain
            .createState(gimbalStateVector)
            .getTransformPath(SnapPointId.BODY_ORIGIN, PAYLOAD_SNAP_POINT_ID)
            .getEulerAngles(AffineTransform.GimbalLockMode.YAW_ZERO)
            .getRollDeg();
    }

    private void assertVectorEquals(Vec3 vExpected, Vec3 vActual, double tolerance) {
        if (vExpected.distance(vActual) > tolerance) {
            Assertions.assertEquals(vExpected, vActual); // fails and prints vectors
        }
    }

    private void checkPayloadTransform(
            KinematicChain kinematicChain,
            GimbalStateVector stateVector,
            Vec3 expectedPayloadSnapPointPosition,
            Vec3 expectedOpticalAxisDirection,
            double expectedPayloadRoll) {
        // check payload snap point position:
        double toleranceMeters = 1e-3;
        assertVectorEquals(
            expectedPayloadSnapPointPosition,
            getPayloadSnapPointPositionInBodyFrame(kinematicChain, stateVector),
            toleranceMeters);

        // check optical axis direction:
        double tolerance = 1e-3;
        assertVectorEquals(expectedOpticalAxisDirection, getPayloadOpticalAxis(kinematicChain, stateVector), tolerance);

        // check payload roll about optical axis
        double toleranceDeg = 0.1;
        Assertions.assertEquals(expectedPayloadRoll, getPayloadRollDeg(kinematicChain, stateVector), toleranceDeg);
    }
}
