/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

import static com.intel.missioncontrol.geometry.FuzzyAssert.assertClose;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Vec2fTest {

    @Test
    void addVec() {
        assertEquals(new Vec2f(2, 4), new Vec2f(1, 1).add(new Vec2f(1, 3)));
    }

    @Test
    void addScalar() {
        assertEquals(new Vec2f(2, 2), new Vec2f(1, 1).add(1));
    }

    @Test
    void addVecInplace() {
        assertEquals(new Vec2f(2, 4), new Vec2f(1, 1).addInplace(new Vec2f(1, 3)));
    }

    @Test
    void addScalarInplace() {
        assertEquals(new Vec2f(2, 2), new Vec2f(1, 1).addInplace(1));
    }

    @Test
    void subtract() {
        assertEquals(new Vec2f(2, 4), new Vec2f(3, 5).subtract(new Vec2f(1, 1)));
    }

    @Test
    void testSubtract() {
        assertEquals(new Vec2f(2, 2), new Vec2f(3, 3).subtract(1));
    }

    @Test
    void subtractInplace() {
        assertEquals(new Vec2f(2, 4), new Vec2f(3, 5).subtractInplace(new Vec2f(1, 1)));
    }

    @Test
    void testSubtractInplace() {
        assertEquals(new Vec2f(2, 2), new Vec2f(3, 3).subtractInplace(1));
    }

    @Test
    void multiplyVec() {
        assertEquals(new Vec2f(2, 4), new Vec2f(1, 2).multiply(new Vec2f(2, 2)));
    }

    @Test
    void multiplyScalar() {
        assertEquals(new Vec2f(2, 2), new Vec2f(1, 1).multiply(2));
    }

    @Test
    void multiplyVecInplace() {
        assertEquals(new Vec2f(2, 4), new Vec2f(1, 2).multiplyInplace(new Vec2f(2, 2)));
    }

    @Test
    void multiplyScalarInplace() {
        assertEquals(new Vec2f(2, 2), new Vec2f(1, 1).multiplyInplace(2));
    }

    @Test
    void divideVec() {
        assertEquals(new Vec2f(1, 2), new Vec2f(2, 4).divide(new Vec2f(2, 2)));
    }

    @Test
    void divideScalar() {
        assertEquals(new Vec2f(1, 1), new Vec2f(2, 2).divide(2));
    }

    @Test
    void divideVecInplace() {
        assertEquals(new Vec2f(1, 2), new Vec2f(2, 4).divideInplace(new Vec2f(2, 2)));
    }

    @Test
    void divideScalarInplace() {
        assertEquals(new Vec2f(1, 1), new Vec2f(2, 2).divideInplace(2));
    }

    @Test
    void normalize() {
        assertClose(new Vec2f((float)Math.sqrt(2) / 2, (float)Math.sqrt(2) / 2), new Vec2f(5, 5).normalize());
    }

    @Test
    void normalizeInplace() {
        assertClose(new Vec2f((float)Math.sqrt(2) / 2, (float)Math.sqrt(2) / 2), new Vec2f(5, 5).normalizeInplace());
    }

    @Test
    void length() {
        assertClose((float)Math.sqrt(2), new Vec2f(1, 1).length());
    }

    @Test
    void lengthSq() {
        assertClose(2, new Vec2f(1, 1).lengthSq());
    }

    @Test
    void distance() {
        assertClose((float)Math.sqrt(2), new Vec2f(1, 1).distance(new Vec2f(2, 2)));
    }

    @Test
    void distanceSq() {
        assertClose(2, new Vec2f(1, 1).distanceSq(new Vec2f(2, 2)));
    }

    @Test
    void angle() {
        assertClose((float)Math.PI / 2, new Vec2f(1, 0).angle(new Vec2f(0, 1)));
    }

}
