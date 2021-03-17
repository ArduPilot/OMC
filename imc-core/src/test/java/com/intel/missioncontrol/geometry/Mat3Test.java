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

class Mat3Test {

    @Test
    void fromAxisAngle() {
        assertClose(
            new Mat3(
                1,
                2.707106781186547,
                -0.5355339059327378,
                -1.5355339059327375,
                1.8786796564403572,
                2.464466094067262,
                2.292893218813452,
                1.0502525316941669,
                3.3431457505076194),
            Mat3.fromAxisAngle(new Vec3(1, 2, 3), Math.PI / 4));
    }

    @Test
    void fromRotationX() {
        assertClose(
            new Mat3(1, 0, 0, 0, 0.7071067811865476, 0.7071067811865476, 0, -0.7071067811865476, 0.7071067811865476),
            Mat3.fromRotationX(Math.PI / 4));
    }

    @Test
    void fromRotationY() {
        assertClose(
            new Mat3(0.7071067811865476, 0, -0.7071067811865476, 0, 1, 0, 0.7071067811865476, 0, 0.7071067811865476),
            Mat3.fromRotationY(Math.PI / 4));
    }

    @Test
    void fromRotationZ() {
        assertClose(
            new Mat3(0.7071067811865476, 0.7071067811865476, 0, -0.7071067811865476, 0.7071067811865476, 0, 0, 0, 1),
            Mat3.fromRotationZ(Math.PI / 4));
    }

    @Test
    void fromYawPitchRoll() {
        assertClose(
            new Mat3(
                -0.4269176212762078,
                -0.058726644927620864,
                0.902381585483331,
                -0.8337376517741568,
                0.41198224566568287,
                -0.3676304629248995,
                -0.35017548837401474,
                -0.9092974268256819,
                -0.22484509536615316),
            Mat3.fromYawPitchRoll(1, 2, 3));
    }

    @Test
    void column() {
        assertEquals(new Vec3(1, 4, 7), new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9).column(0));
        assertEquals(new Vec3(2, 5, 8), new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9).column(1));
        assertEquals(new Vec3(3, 6, 9), new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9).column(2));
    }

    @Test
    void row() {
        assertEquals(new Vec3(1, 2, 3), new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9).row(0));
        assertEquals(new Vec3(4, 5, 6), new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9).row(1));
        assertEquals(new Vec3(7, 8, 9), new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9).row(2));
    }

    @Test
    void isIdentity() {
        assertTrue(new Mat3(1, 0, 0, 0, 1, 0, 0, 0, 1).isIdentity());
        assertFalse(new Mat3(1, 1, 0, 0, 1, 0, 0, 0, 1).isIdentity());
    }

    @Test
    void multiplyMat() {
        assertClose(
            new Mat3(30, 36, 42, 66, 81, 96, 102, 126, 150),
            new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9).multiply(new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9)));
    }

    @Test
    void multiplyMatInplace() {
        assertClose(
            new Mat3(30, 36, 42, 66, 81, 96, 102, 126, 150),
            new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9).multiplyInplace(new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9)));
    }

    @Test
    void multiplyScalar() {
        assertClose(new Mat3(2, 2, 2, 2, 2, 2, 2, 2, 2), new Mat3(1, 1, 1, 1, 1, 1, 1, 1, 1).multiply(2));
    }

    @Test
    void multiplyScalarInplace() {
        assertClose(new Mat3(2, 2, 2, 2, 2, 2, 2, 2, 2), new Mat3(1, 1, 1, 1, 1, 1, 1, 1, 1).multiplyInplace(2));
    }

    @Test
    void transpose() {
        assertClose(new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9), new Mat3(1, 4, 7, 2, 5, 8, 3, 6, 9).transpose());
    }

    @Test
    void transposeInplace() {
        assertClose(new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9), new Mat3(1, 4, 7, 2, 5, 8, 3, 6, 9).transposeInplace());
    }

    @Test
    void invert() {
        assertThrows(ArithmeticException.class, () -> new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9).invert());
        assertClose(new Mat3(1, -1, -1, 0, 1, 0, 0, 0, 1), new Mat3(1, 1, 1, 0, 1, 0, 0, 0, 1).invert());
    }

    @Test
    void invertInplace() {
        assertThrows(ArithmeticException.class, () -> new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9).invertInplace());
        assertClose(new Mat3(1, -1, -1, 0, 1, 0, 0, 0, 1), new Mat3(1, 1, 1, 0, 1, 0, 0, 0, 1).invertInplace());
    }

    @Test
    void adjugate() {
        assertClose(new Mat3(-8, 18, -4, -5, 12, -1, 4, -6, 2), new Mat3(-3, 2, -5, -1, 0, -2, 3, -4, 1).adjugate());
    }

    @Test
    void adjugateInplace() {
        assertClose(
            new Mat3(-8, 18, -4, -5, 12, -1, 4, -6, 2), new Mat3(-3, 2, -5, -1, 0, -2, 3, -4, 1).adjugateInplace());
    }

    @Test
    void det() {
        assertClose(0, new Mat3(1, 2, 3, 4, 5, 6, 7, 8, 9).det());
        assertClose(1, new Mat3(1, 0, 0, 0, 1, 0, 0, 0, 1).det());
    }

}
