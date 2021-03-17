/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware.kinematics;

import com.intel.missioncontrol.geometry.EulerAngles;
import com.intel.missioncontrol.geometry.Mat4;
import com.intel.missioncontrol.geometry.Vec3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AffineTransformTest {

    @Test
    void Matrix_Representation_Is_Correct_For_Offset() {
        double tolerance = 1e-3;

        AffineTransform t = AffineTransform.fromOffset(10, 11, 12);
        Mat4 mat = t.getTransformMatrix();
        assertMatrixEquals(new double[][] {{1, 0, 0, 10}, {0, 1, 0, 11}, {0, 0, 1, 12}, {0, 0, 0, 1}}, mat, tolerance);
    }

    @Test
    void Matrix_Representation_Is_Correct_For_Offset_And_AxisAngle() {
        double tolerance = 1e-3;

        AffineTransform t1 = AffineTransform.fromAxisAngleDeg(new Vec3(1, 0, 0), 90);
        AffineTransform t2 = AffineTransform.fromOffset(10, 11, 12);
        AffineTransform t = t1.chain(t2); // first rotate, then offset -> offset is not rotated.
        Mat4 mat = t.getTransformMatrix();
        assertMatrixEquals(new double[][] {{1, 0, 0, 10}, {0, 0, -1, 11}, {0, 1, 0, 12}, {0, 0, 0, 1}}, mat, tolerance);
    }

    @Test
    void Matrix_Representation_Is_Correct_For_YawPitchRoll() {
        double tolerance = 0.1;

        AffineTransform t = AffineTransform.fromYawPitchRollDeg(10, 20, 30);
        Mat4 mat = t.getTransformMatrix();
        assertMatrixEquals(
            new double[][] {{0.93, 0.16, -0.34, 0}, {0.02, 0.88, 0.47, 0}, {0.38, -0.44, 0.81, 0}, {0, 0, 0, 1}},
            mat,
            tolerance);
    }

    @Test
    void Vector_Multiplication_Is_Correct_For_YawPitchRoll() {
        AffineTransform t = AffineTransform.fromYawPitchRollDeg(5, 0, 0);
        double tolerance = 1e-3;
        assertVectorEquals(new Vec3(0.996, -0.087, 0), t.transformPoint(new Vec3(1, 0, 0)), tolerance);
    }

    @Test
    void Yaw_Angle_Is_Consistent() {
        AffineTransform t = AffineTransform.fromYawPitchRollDeg(11.0, 0.0, 0.0);

        EulerAngles eulerAngles = t.getEulerAngles(AffineTransform.GimbalLockMode.ROLL_ZERO);

        Assertions.assertEquals(11.0, eulerAngles.getYawDeg(), 1e-6);
    }

    @Test
    void Pitch_Angle_Is_Consistent() {
        AffineTransform t = AffineTransform.fromYawPitchRollDeg(0.0, 12.0, 0.0);

        EulerAngles eulerAngles = t.getEulerAngles(AffineTransform.GimbalLockMode.ROLL_ZERO);

        Assertions.assertEquals(12.0, eulerAngles.getPitchDeg(), 1e-6);
    }

    @Test
    void Roll_Angle_Is_Consistent() {
        AffineTransform t = AffineTransform.fromYawPitchRollDeg(0.0, 0.0, 13.0);

        EulerAngles eulerAngles = t.getEulerAngles(AffineTransform.GimbalLockMode.ROLL_ZERO);

        Assertions.assertEquals(13.0, eulerAngles.getRollDeg(), 1e-6);
    }

    @Test
    void Euler_Angles_Are_Consistent() {
        AffineTransform t = AffineTransform.fromYawPitchRollDeg(10.0, 12.3, 23.4);

        EulerAngles eulerAngles = t.getEulerAngles(AffineTransform.GimbalLockMode.ROLL_ZERO);

        Assertions.assertEquals(10.0, eulerAngles.getYawDeg(), 1e-6);
        Assertions.assertEquals(12.3, eulerAngles.getPitchDeg(), 1e-6);
        Assertions.assertEquals(23.4, eulerAngles.getRollDeg(), 1e-6);
    }

    @Test
    void Chaining_Order_Is_Correct() {
        AffineTransform t1 = AffineTransform.fromOffset(1.0, 0.0, 0.0);
        AffineTransform t2 = AffineTransform.fromScale(2.0);

        AffineTransform t12 = t1.chain(t2); // first offset a vector, then scale -> offsets are scaled.
        AffineTransform t21 = t2.chain(t1); // first scale a vector, then offset -> offsets are not scaled.

        double tolerance = 1e-3;
        assertVectorEquals(new Vec3(22.0, 0, 0), t12.transformPoint(new Vec3(10.0, 0, 0)), tolerance);
        assertVectorEquals(new Vec3(21.0, 0, 0), t21.transformPoint(new Vec3(10.0, 0, 0)), tolerance);
    }

    @Test
    void Chaining_Is_Associative() {
        AffineTransform t1 = AffineTransform.fromYawPitchRollDeg(10.0, 12.3, 23.4);
        AffineTransform t2 = AffineTransform.fromYawPitchRollDeg(12.0, 14.3, 25.4);
        AffineTransform t3 = AffineTransform.fromYawPitchRollDeg(14.0, 16.3, 27.4);

        AffineTransform t12_3 = (t1.chain(t2)).chain(t3);
        AffineTransform t1_23 = t1.chain(t2.chain(t3));
        AffineTransform t1_2_3 = t1.chain(t2).chain(t3);

        double tolerance = 1e-12;
        Vec3 v = new Vec3(1.1, 2.2, 3.3);

        assertVectorEquals(t12_3.transformPoint(v), t1_23.transformPoint(v), tolerance);
        assertVectorEquals(t12_3.transformPoint(v), t1_2_3.transformPoint(v), tolerance);
        assertVectorEquals(t1_23.transformPoint(v), t1_2_3.transformPoint(v), tolerance);
    }

    @Test
    void Rotational_Distance_Is_Correct() {
        double toleranceDeg = 1e-3;

        AffineTransform t1 = AffineTransform.fromYawPitchRollDeg(10, 20, 30);
        AffineTransform t2 = AffineTransform.fromYawPitchRollDeg(12, 20, 30);
        Assertions.assertEquals(2.000, t1.calculateRotationalDistanceDeg(t2), toleranceDeg);

        t1 = AffineTransform.fromYawPitchRollDeg(10, 20, 30);
        t2 = AffineTransform.fromYawPitchRollDeg(-10, 20, 30);
        Assertions.assertEquals(20.000, t1.calculateRotationalDistanceDeg(t2), toleranceDeg);

        t1 = AffineTransform.fromYawPitchRollDeg(10, 20, 30);
        t2 = AffineTransform.fromYawPitchRollDeg(12, 22, 28);
        Assertions.assertEquals(3.856, t1.calculateRotationalDistanceDeg(t2), toleranceDeg);

        t1 = AffineTransform.fromYawPitchRollDeg(0, 90, 0);
        t2 = AffineTransform.fromYawPitchRollDeg(170, 88, 170);
        Assertions.assertEquals(2.000, t1.calculateRotationalDistanceDeg(t2), toleranceDeg);
    }

    @Test
    void Rotational_Distance_Is_Correct_When_Ignoring_Yaw() {
        double toleranceDeg = 1e-3;

        AffineTransform t1 = AffineTransform.fromYawPitchRollDeg(10, 20, 30);
        AffineTransform t2 = AffineTransform.fromYawPitchRollDeg(12, 20, 30);
        Assertions.assertEquals(0.000, t1.calculateRotationalDistanceDeg(t2, true), toleranceDeg);

        t1 = AffineTransform.fromYawPitchRollDeg(10, 20, 30);
        t2 = AffineTransform.fromYawPitchRollDeg(-10, 20, 30);
        Assertions.assertEquals(0.000, t1.calculateRotationalDistanceDeg(t2, true), toleranceDeg);

        t1 = AffineTransform.fromYawPitchRollDeg(10, 20, 30);
        t2 = AffineTransform.fromYawPitchRollDeg(12, 22, 30);
        Assertions.assertEquals(2.000, t1.calculateRotationalDistanceDeg(t2, true), toleranceDeg);

        t1 = AffineTransform.fromYawPitchRollDeg(0, 90, 0);
        t2 = AffineTransform.fromYawPitchRollDeg(170, 88, 170);
        Assertions.assertEquals(2.000, t1.calculateRotationalDistanceDeg(t2, true), toleranceDeg);

        t1 = AffineTransform.fromYawPitchRollDeg(0, 90, 0);
        t2 = AffineTransform.fromYawPitchRollDeg(155, 88, 170);
        Assertions.assertEquals(2.000, t1.calculateRotationalDistanceDeg(t2, true), toleranceDeg);
    }

    private void assertVectorEquals(Vec3 vExpected, Vec3 vActual, double tolerance) {
        Assertions.assertEquals(vExpected.x, vActual.x, tolerance);
        Assertions.assertEquals(vExpected.y, vActual.y, tolerance);
        Assertions.assertEquals(vExpected.z, vActual.z, tolerance);
    }

    private void assertMatrixEquals(double[][] expected, Mat4 actual, double tolerance) {
        Assertions.assertEquals(expected.length, 4);

        Assertions.assertEquals(expected[0][0], actual.m11, tolerance);
        Assertions.assertEquals(expected[0][1], actual.m12, tolerance);
        Assertions.assertEquals(expected[0][2], actual.m13, tolerance);
        Assertions.assertEquals(expected[0][3], actual.m14, tolerance);

        Assertions.assertEquals(expected[1][0], actual.m21, tolerance);
        Assertions.assertEquals(expected[1][1], actual.m22, tolerance);
        Assertions.assertEquals(expected[1][2], actual.m23, tolerance);
        Assertions.assertEquals(expected[1][3], actual.m24, tolerance);

        Assertions.assertEquals(expected[2][0], actual.m31, tolerance);
        Assertions.assertEquals(expected[2][1], actual.m32, tolerance);
        Assertions.assertEquals(expected[2][2], actual.m33, tolerance);
        Assertions.assertEquals(expected[2][3], actual.m34, tolerance);

        Assertions.assertEquals(expected[3][0], actual.m41, tolerance);
        Assertions.assertEquals(expected[3][1], actual.m42, tolerance);
        Assertions.assertEquals(expected[3][2], actual.m43, tolerance);
        Assertions.assertEquals(expected[3][3], actual.m44, tolerance);
    }

}
