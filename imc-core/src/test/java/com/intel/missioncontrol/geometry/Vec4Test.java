/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

import static com.intel.missioncontrol.geometry.FuzzyAssert.assertClose;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Vec4Test {

    @Test
    void addVec() {
        assertEquals(new Vec4(2, 4, 6, 8), new Vec4(1, 1, 1, 1).add(new Vec4(1, 3, 5, 7)));
    }

    @Test
    void addScalar() {
        assertEquals(new Vec4(2, 2, 2, 2), new Vec4(1, 1, 1, 1).add(1));
    }

    @Test
    void addVecInplace() {
        assertEquals(new Vec4(2, 4, 6, 8), new Vec4(1, 1, 1, 1).addInplace(new Vec4(1, 3, 5, 7)));
    }

    @Test
    void addScalarInplace() {
        assertEquals(new Vec4(2, 2, 2, 2), new Vec4(1, 1, 1, 1).addInplace(1));
    }

    @Test
    void subtract() {
        assertEquals(new Vec4(2, 4, 6, 8), new Vec4(3, 5, 7, 9).subtract(new Vec4(1, 1, 1, 1)));
    }

    @Test
    void testSubtract() {
        assertEquals(new Vec4(2, 2, 2, 2), new Vec4(3, 3, 3, 3).subtract(1));
    }

    @Test
    void subtractInplace() {
        assertEquals(new Vec4(2, 4, 6, 8), new Vec4(3, 5, 7, 9).subtractInplace(new Vec4(1, 1, 1, 1)));
    }

    @Test
    void testSubtractInplace() {
        assertEquals(new Vec4(2, 2, 2, 2), new Vec4(3, 3, 3, 3).subtractInplace(1));
    }

    @Test
    void multiplyVec() {
        assertEquals(new Vec4(2, 4, 6, 8), new Vec4(1, 2, 3, 4).multiply(new Vec4(2, 2, 2, 2)));
    }

    @Test
    void multiplyScalar() {
        assertEquals(new Vec4(2, 2, 2, 2), new Vec4(1, 1, 1, 1).multiply(2));
    }

    @Test
    void multiplyVecInplace() {
        assertEquals(new Vec4(2, 4, 6, 8), new Vec4(1, 2, 3, 4).multiplyInplace(new Vec4(2, 2, 2, 2)));
    }

    @Test
    void multiplyScalarInplace() {
        assertEquals(new Vec4(2, 2, 2, 2), new Vec4(1, 1, 1, 1).multiplyInplace(2));
    }

    @Test
    void divideVec() {
        assertEquals(new Vec4(1, 2, 3, 4), new Vec4(2, 4, 6, 8).divide(new Vec4(2, 2, 2, 2)));
    }

    @Test
    void divideScalar() {
        assertEquals(new Vec4(1, 1, 1, 1), new Vec4(2, 2, 2, 2).divide(2));
    }

    @Test
    void divideVecInplace() {
        assertEquals(new Vec4(1, 2, 3, 4), new Vec4(2, 4, 6, 8).divideInplace(new Vec4(2, 2, 2, 2)));
    }

    @Test
    void divideScalarInplace() {
        assertEquals(new Vec4(1, 1, 1, 1), new Vec4(2, 2, 2, 2).divideInplace(2));
    }

    @Test
    void normalize() {
        assertClose(
            new Vec4(1 / Math.sqrt(4), 1 / Math.sqrt(4), 1 / Math.sqrt(4), 1 / Math.sqrt(4)),
            new Vec4(5, 5, 5, 5).normalize());
    }

    @Test
    void normalizeInplace() {
        assertClose(
            new Vec4(1 / Math.sqrt(4), 1 / Math.sqrt(4), 1 / Math.sqrt(4), 1 / Math.sqrt(4)),
            new Vec4(5, 5, 5, 5).normalizeInplace());
    }

    @Test
    void length() {
        assertClose(Math.sqrt(4), new Vec4(1, 1, 1, 1).length());
    }

    @Test
    void lengthSq() {
        assertClose(4, new Vec4(1, 1, 1, 1).lengthSq());
    }

    @Test
    void distance() {
        assertClose(Math.sqrt(4), new Vec4(1, 1, 1, 1).distance(new Vec4(2, 2, 2, 2)));
    }

    @Test
    void distanceSq() {
        assertClose(4, new Vec4(1, 1, 1, 1).distanceSq(new Vec4(2, 2, 2, 2)));
    }

    @Test
    void angle() {
        assertClose(Math.PI / 2, new Vec4(1, 0, 0, 0).angle(new Vec4(0, 1, 0, 0)));
    }

    @Test
    void dot() {
        assertClose(60, new Vec4(1, 2, 3, 4).dot(new Vec4(4, 5, 6, 7)));
    }

    @Test
    void project() {
        assertClose(new Vec4(40d / 21, 50d / 21, 20d / 7, 10d / 3), new Vec4(1, 2, 3, 4).project(new Vec4(4, 5, 6, 7)));
    }

    @Test
    void projectInplace() {
        assertClose(
            new Vec4(40d / 21, 50d / 21, 20d / 7, 10d / 3), new Vec4(1, 2, 3, 4).projectInplace(new Vec4(4, 5, 6, 7)));
    }

    @Test
    void clamp() {
        assertClose(new Vec4(1, 1, 1, 1), new Vec4(2, 2, 2, 2).clamp(Vec4.zero(), new Vec4(1, 1, 1, 1)));
    }

    @Test
    void clampInplace() {
        assertClose(new Vec4(1, 1, 1, 1), new Vec4(2, 2, 2, 2).clampInplace(Vec4.zero(), new Vec4(1, 1, 1, 1)));
    }

    @Test
    void interpolate() {
        assertClose(new Vec4(1, 1, 1, 1).multiply(1.5), new Vec4(1, 1, 1, 1).interpolate(new Vec4(2, 2, 2, 2), 0.5));
    }

    @Test
    void interpolateInplace() {
        assertClose(
            new Vec4(1, 1, 1, 1).multiply(1.5), new Vec4(1, 1, 1, 1).interpolateInplace(new Vec4(2, 2, 2, 2), 0.5));
    }

    @Test
    void transform() {
        assertClose(new Vec4(3, 3, 3, 1), new Vec4(1, 1, 1, 1).transform(Mat4.fromTranslation(2, 2, 2)));
    }

    @Test
    void transformInplace() {
        assertClose(new Vec4(3, 3, 3, 1), new Vec4(1, 1, 1, 1).transformInplace(Mat4.fromTranslation(2, 2, 2)));
    }

}
