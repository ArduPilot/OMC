/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

import static com.intel.missioncontrol.geometry.FuzzyAssert.assertClose;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Mat4fTest {

    @Test
    void fromAxisAngle() {
        assertClose(
            new Mat4f(
                1,
                2.7071066f,
                -0.5355338f,
                0,
                -1.5355338f,
                1.8786798f,
                2.4644663f,
                0,
                2.2928934f,
                1.0502527f,
                3.3431458f,
                0,
                0,
                0,
                0,
                1),
            Mat4f.fromAxisAngle(new Vec3f(1, 2, 3), (float)Math.PI / 4));
    }

    @Test
    void fromQuaternion() {
        assertClose(
            new Mat4f(-25, 28, -10, 0, -20, -19, 20, 0, 22, 4, -9, 0, 0, 0, 0, 1),
            Mat4f.fromQuaternion(new Quaternionf(1, 2, 3, 4)));
    }

    @Test
    void fromTranslation() {
        assertClose(
            new Mat4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 10, 20, 30, 1), Mat4f.fromTranslation(new Vec3f(10, 20, 30)));
    }

    @Test
    void fromTranslationXyz() {
        assertClose(new Mat4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 10, 20, 30, 1), Mat4f.fromTranslation(10, 20, 30));
    }

    @Test
    void fromScale() {
        assertClose(new Mat4f(1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 1), Mat4f.fromScale(1, 2, 3));
    }

    @Test
    void fromScaleOffCenter() {
        assertClose(
            new Mat4f(1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, -2, -6, 1), Mat4f.fromScale(1, 2, 3, new Vec3f(1, 2, 3)));
    }

    @Test
    void fromRotationX() {
        assertClose(
            new Mat4f(
                1,
                0,
                0,
                0,
                0,
                0.7071067811865476f,
                0.7071067811865476f,
                0,
                0,
                -0.7071067811865476f,
                0.7071067811865476f,
                0,
                0,
                0,
                0,
                1),
            Mat4f.fromRotationX((float)Math.PI / 4));
    }

    @Test
    void fromRotationOffCenterX() {
        assertClose(
            new Mat4f(
                1,
                0,
                0,
                0,
                0,
                0.7071067811865476f,
                0.7071067811865476f,
                0,
                0,
                -0.7071067811865476f,
                0.7071067811865476f,
                0,
                0,
                2.707106781186547f,
                -0.5355339059327376f,
                1),
            Mat4f.fromRotationX((float)Math.PI / 4, new Vec3f(1, 2, 3)));
    }

    @Test
    void fromRotationY() {
        assertClose(
            new Mat4(
                0.7071067811865476,
                0,
                -0.7071067811865476,
                0,
                0,
                1,
                0,
                0,
                0.7071067811865476,
                0,
                0.7071067811865476,
                0,
                0,
                0,
                0,
                1),
            Mat4.fromRotationY(Math.PI / 4));
    }

    @Test
    void fromRotationOffCenterY() {
        assertClose(
            new Mat4f(
                0.7071067811865476f,
                0,
                -0.7071067811865476f,
                0,
                0,
                1,
                0,
                0,
                0.7071067811865476f,
                0,
                0.7071067811865476f,
                0,
                -1.8284271247461898f,
                0,
                1.5857864376269046f,
                1),
            Mat4f.fromRotationY((float)Math.PI / 4, new Vec3f(1, 2, 3)));
    }

    @Test
    void fromRotationZ() {
        assertClose(
            new Mat4f(
                0.7071067811865476f,
                0.7071067811865476f,
                0,
                0,
                -0.7071067811865476f,
                0.7071067811865476f,
                0,
                0,
                0,
                0,
                1,
                0,
                0,
                0,
                0,
                1),
            Mat4f.fromRotationZ((float)Math.PI / 4));
    }

    @Test
    void fromRotationOffCenterZ() {
        assertClose(
            new Mat4f(
                0.7071067811865476f,
                0.7071067811865476f,
                0,
                0,
                -0.7071067811865476f,
                0.7071067811865476f,
                0,
                0,
                0,
                0,
                1,
                0,
                1.7071067811865475f,
                -0.12132034355964261f,
                0,
                1),
            Mat4f.fromRotationZ((float)Math.PI / 4, new Vec3f(1, 2, 3)));
    }

    @Test
    void fromYawPitchRoll() {
        assertClose(
            new Mat4f(
                -0.4269176212762078f,
                -0.058726644927620864f,
                0.902381585483331f,
                0,
                -0.8337376517741568f,
                0.41198224566568287f,
                -0.3676304629248995f,
                0,
                -0.35017548837401474f,
                -0.9092974268256819f,
                -0.22484509536615316f,
                0,
                0,
                0,
                0,
                1),
            Mat4f.fromYawPitchRoll(1, 2, 3));
    }

    @Test
    void column() {
        assertEquals(
            new Vec4f(1, 5, 9, 13), new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).column(0));
        assertEquals(
            new Vec4f(2, 6, 10, 14), new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).column(1));
        assertEquals(
            new Vec4f(3, 7, 11, 15), new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).column(2));
        assertEquals(
            new Vec4f(4, 8, 12, 16), new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).column(3));
    }

    @Test
    void row() {
        assertEquals(new Vec4f(1, 2, 3, 4), new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).row(0));
        assertEquals(new Vec4f(5, 6, 7, 8), new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).row(1));
        assertEquals(new Vec4f(9, 10, 11, 12), new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).row(2));
        assertEquals(
            new Vec4f(13, 14, 15, 16), new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).row(3));
    }

    @Test
    void isIdentity() {
        assertTrue(new Mat4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1).isIdentity());
        assertFalse(new Mat4f(1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1).isIdentity());
    }

    @Test
    void transpose() {
        assertEquals(
            new Mat4f(1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15, 4, 8, 12, 16),
            new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).transpose());
    }

    @Test
    void transposeInplace() {
        assertEquals(
            new Mat4f(1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15, 4, 8, 12, 16),
            new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).transposeInplace());
    }

    @Test
    void multiplyMat() {
        assertClose(
            new Mat4f(90, 100, 110, 120, 202, 228, 254, 280, 314, 356, 398, 440, 426, 484, 542, 600),
            new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
                .multiply(new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)));
    }

    @Test
    void multiplyMatInplace() {
        assertClose(
            new Mat4f(90, 100, 110, 120, 202, 228, 254, 280, 314, 356, 398, 440, 426, 484, 542, 600),
            new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
                .multiplyInplace(new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)));
    }

    @Test
    void multiplyScalar() {
        assertClose(
            new Mat4f(2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32),
            new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).multiply(2));
    }

    @Test
    void multiplyScalarInplace() {
        assertClose(
            new Mat4f(2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32),
            new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).multiplyInplace(2));
    }

    @Test
    void invert() {
        assertThrows(
            ArithmeticException.class, () -> new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).invert());
        assertClose(
            new Mat4f(1, -1, -1, -1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1),
            new Mat4f(1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1).invert());
    }

    @Test
    void invertInplace() {
        assertThrows(
            ArithmeticException.class,
            () -> new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).invertInplace());
        assertClose(
            new Mat4f(1, -1, -1, -1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1),
            new Mat4f(1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1).invertInplace());
    }

    @Test
    void adjugate() {
        assertClose(
            new Mat4f(-138, -114, 108, -36, 610, 460, -170, 124, 632, 566, -196, 200, -255, -237, 81, -27),
            new Mat4f(1, 6, -3, 4, 2, -6, 3, -8, 9, 1, 1, 0, 0, -1, 5, 10).adjugate());
    }

    @Test
    void adjugateInplace() {
        assertClose(
            new Mat4f(-138, -114, 108, -36, 610, 460, -170, 124, 632, 566, -196, 200, -255, -237, 81, -27),
            new Mat4f(1, 6, -3, 4, 2, -6, 3, -8, 9, 1, 1, 0, 0, -1, 5, 10).adjugateInplace());
    }

    @Test
    void det() {
        assertClose(0, new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).det());
        assertClose(1, new Mat4f(1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1).det());
        assertClose(606, new Mat4f(1, 6, -3, 4, 2, -6, 3, -8, 9, 1, 1, 0, 0, -1, 5, 10).det());
    }

    @Test
    void add() {
        assertClose(
            new Mat4f(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17),
            new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
                .add(new Mat4f(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)));
    }

    @Test
    void addInplace() {
        assertClose(
            new Mat4f(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17),
            new Mat4f(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
                .addInplace(new Mat4f(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)));
    }

}
