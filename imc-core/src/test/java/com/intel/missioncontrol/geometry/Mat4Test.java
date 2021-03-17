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

class Mat4Test {

    @Test
    void fromAxisAngle() {
        assertClose(
            new Mat4(
                1,
                2.707106781186547,
                -0.5355339059327378,
                0,
                -1.5355339059327375,
                1.8786796564403572,
                2.464466094067262,
                0,
                2.292893218813452,
                1.0502525316941669,
                3.3431457505076194,
                0,
                0,
                0,
                0,
                1),
            Mat4.fromAxisAngle(new Vec3(1, 2, 3), Math.PI / 4));
    }

    @Test
    void fromQuaternion() {
        assertClose(
            new Mat4(-25, 28, -10, 0, -20, -19, 20, 0, 22, 4, -9, 0, 0, 0, 0, 1),
            Mat4.fromQuaternion(new Quaternion(1, 2, 3, 4)));
    }

    @Test
    void fromTranslation() {
        assertClose(
            new Mat4(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 10, 20, 30, 1), Mat4.fromTranslation(new Vec3(10, 20, 30)));
    }

    @Test
    void fromTranslationXyz() {
        assertClose(new Mat4(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 10, 20, 30, 1), Mat4.fromTranslation(10, 20, 30));
    }

    @Test
    void fromScale() {
        assertClose(new Mat4(1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 1), Mat4.fromScale(1, 2, 3));
    }

    @Test
    void fromScaleOffCenter() {
        assertClose(
            new Mat4(1, 0, 0, 0, 0, 2, 0, 0, 0, 0, 3, 0, 0, -2, -6, 1), Mat4.fromScale(1, 2, 3, new Vec3(1, 2, 3)));
    }

    @Test
    void fromRotationX() {
        assertClose(
            new Mat4(
                1,
                0,
                0,
                0,
                0,
                0.7071067811865476,
                0.7071067811865476,
                0,
                0,
                -0.7071067811865476,
                0.7071067811865476,
                0,
                0,
                0,
                0,
                1),
            Mat4.fromRotationX(Math.PI / 4));
    }

    @Test
    void fromRotationOffCenterX() {
        assertClose(
            new Mat4(
                1,
                0,
                0,
                0,
                0,
                0.7071067811865476,
                0.7071067811865476,
                0,
                0,
                -0.7071067811865476,
                0.7071067811865476,
                0,
                0,
                2.707106781186547,
                -0.5355339059327376,
                1),
            Mat4.fromRotationX(Math.PI / 4, new Vec3(1, 2, 3)));
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
                -1.8284271247461898,
                0,
                1.5857864376269046,
                1),
            Mat4.fromRotationY(Math.PI / 4, new Vec3(1, 2, 3)));
    }

    @Test
    void fromRotationZ() {
        assertClose(
            new Mat4(
                0.7071067811865476,
                0.7071067811865476,
                0,
                0,
                -0.7071067811865476,
                0.7071067811865476,
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
            Mat4.fromRotationZ(Math.PI / 4));
    }

    @Test
    void fromRotationOffCenterZ() {
        assertClose(
            new Mat4(
                0.7071067811865476,
                0.7071067811865476,
                0,
                0,
                -0.7071067811865476,
                0.7071067811865476,
                0,
                0,
                0,
                0,
                1,
                0,
                1.7071067811865475,
                -0.12132034355964261,
                0,
                1),
            Mat4.fromRotationZ(Math.PI / 4, new Vec3(1, 2, 3)));
    }

    @Test
    void fromYawPitchRoll() {
        assertClose(
            new Mat4(
                -0.4269176212762078,
                -0.058726644927620864,
                0.902381585483331,
                0,
                -0.8337376517741568,
                0.41198224566568287,
                -0.3676304629248995,
                0,
                -0.35017548837401474,
                -0.9092974268256819,
                -0.22484509536615316,
                0,
                0,
                0,
                0,
                1),
            Mat4.fromYawPitchRoll(1, 2, 3));
    }

    @Test
    void column() {
        assertEquals(new Vec4(1, 5, 9, 13), new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).column(0));
        assertEquals(new Vec4(2, 6, 10, 14), new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).column(1));
        assertEquals(new Vec4(3, 7, 11, 15), new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).column(2));
        assertEquals(new Vec4(4, 8, 12, 16), new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).column(3));
    }

    @Test
    void row() {
        assertEquals(new Vec4(1, 2, 3, 4), new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).row(0));
        assertEquals(new Vec4(5, 6, 7, 8), new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).row(1));
        assertEquals(new Vec4(9, 10, 11, 12), new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).row(2));
        assertEquals(new Vec4(13, 14, 15, 16), new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).row(3));
    }

    @Test
    void isIdentity() {
        assertTrue(new Mat4(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1).isIdentity());
        assertFalse(new Mat4(1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1).isIdentity());
    }

    @Test
    void transpose() {
        assertEquals(
            new Mat4(1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15, 4, 8, 12, 16),
            new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).transpose());
    }

    @Test
    void transposeInplace() {
        assertEquals(
            new Mat4(1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15, 4, 8, 12, 16),
            new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).transposeInplace());
    }

    @Test
    void multiplyMat() {
        assertClose(
            new Mat4(90, 100, 110, 120, 202, 228, 254, 280, 314, 356, 398, 440, 426, 484, 542, 600),
            new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
                .multiply(new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)));
    }

    @Test
    void multiplyMatInplace() {
        assertClose(
            new Mat4(90, 100, 110, 120, 202, 228, 254, 280, 314, 356, 398, 440, 426, 484, 542, 600),
            new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
                .multiplyInplace(new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)));
    }

    @Test
    void multiplyScalar() {
        assertClose(
            new Mat4(2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32),
            new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).multiply(2));
    }

    @Test
    void multiplyScalarInplace() {
        assertClose(
            new Mat4(2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32),
            new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).multiplyInplace(2));
    }

    @Test
    void invert() {
        assertThrows(
            ArithmeticException.class, () -> new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).invert());
        assertClose(
            new Mat4(1, -1, -1, -1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1),
            new Mat4(1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1).invert());
    }

    @Test
    void invertInplace() {
        assertThrows(
            ArithmeticException.class,
            () -> new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).invertInplace());
        assertClose(
            new Mat4(1, -1, -1, -1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1),
            new Mat4(1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1).invertInplace());
    }

    @Test
    void adjugate() {
        assertClose(
            new Mat4(-138, -114, 108, -36, 610, 460, -170, 124, 632, 566, -196, 200, -255, -237, 81, -27),
            new Mat4(1, 6, -3, 4, 2, -6, 3, -8, 9, 1, 1, 0, 0, -1, 5, 10).adjugate());
    }

    @Test
    void adjugateInplace() {
        assertClose(
            new Mat4(-138, -114, 108, -36, 610, 460, -170, 124, 632, 566, -196, 200, -255, -237, 81, -27),
            new Mat4(1, 6, -3, 4, 2, -6, 3, -8, 9, 1, 1, 0, 0, -1, 5, 10).adjugateInplace());
    }

    @Test
    void det() {
        assertClose(0, new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).det());
        assertClose(1, new Mat4(1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1).det());
        assertClose(606, new Mat4(1, 6, -3, 4, 2, -6, 3, -8, 9, 1, 1, 0, 0, -1, 5, 10).det());
    }

    @Test
    void add() {
        assertClose(
            new Mat4(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17),
            new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
                .add(new Mat4(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)));
    }

    @Test
    void addInplace() {
        assertClose(
            new Mat4(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17),
            new Mat4(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
                .addInplace(new Mat4(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)));
    }

}
