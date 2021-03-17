/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

import static com.intel.missioncontrol.geometry.FuzzyAssert.assertClose;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Vec3fTest {

    @Test
    void addVec() {
        assertEquals(new Vec3f(2, 4, 6), new Vec3f(1, 1, 1).add(new Vec3f(1, 3, 5)));
    }

    @Test
    void addScalar() {
        assertEquals(new Vec3f(2, 2, 2), new Vec3f(1, 1, 1).add(1));
    }

    @Test
    void addVecInplace() {
        assertEquals(new Vec3f(2, 4, 6), new Vec3f(1, 1, 1).addInplace(new Vec3f(1, 3, 5)));
    }

    @Test
    void addScalarInplace() {
        assertEquals(new Vec3f(2, 2, 2), new Vec3f(1, 1, 1).addInplace(1));
    }

    @Test
    void subtract() {
        assertEquals(new Vec3f(2, 4, 6), new Vec3f(3, 5, 7).subtract(new Vec3f(1, 1, 1)));
    }

    @Test
    void testSubtract() {
        assertEquals(new Vec3f(2, 2, 2), new Vec3f(3, 3, 3).subtract(1));
    }

    @Test
    void subtractInplace() {
        assertEquals(new Vec3f(2, 4, 6), new Vec3f(3, 5, 7).subtractInplace(new Vec3f(1, 1, 1)));
    }

    @Test
    void testSubtractInplace() {
        assertEquals(new Vec3f(2, 2, 2), new Vec3f(3, 3, 3).subtractInplace(1));
    }

    @Test
    void multiplyVec() {
        assertEquals(new Vec3f(2, 4, 6), new Vec3f(1, 2, 3).multiply(new Vec3f(2, 2, 2)));
    }

    @Test
    void multiplyScalar() {
        assertEquals(new Vec3f(2, 2, 2), new Vec3f(1, 1, 1).multiply(2));
    }

    @Test
    void multiplyVecInplace() {
        assertEquals(new Vec3f(2, 4, 6), new Vec3f(1, 2, 3).multiplyInplace(new Vec3f(2, 2, 2)));
    }

    @Test
    void multiplyScalarInplace() {
        assertEquals(new Vec3f(2, 2, 2), new Vec3f(1, 1, 1).multiplyInplace(2));
    }

    @Test
    void divideVec() {
        assertEquals(new Vec3f(1, 2, 3), new Vec3f(2, 4, 6).divide(new Vec3f(2, 2, 2)));
    }

    @Test
    void divideScalar() {
        assertEquals(new Vec3f(1, 1, 1), new Vec3f(2, 2, 2).divide(2));
    }

    @Test
    void divideVecInplace() {
        assertEquals(new Vec3f(1, 2, 3), new Vec3f(2, 4, 6).divideInplace(new Vec3f(2, 2, 2)));
    }

    @Test
    void divideScalarInplace() {
        assertEquals(new Vec3f(1, 1, 1), new Vec3f(2, 2, 2).divideInplace(2));
    }

    @Test
    void normalize() {
        assertClose(
            new Vec3f(1 / (float)Math.sqrt(3), 1 / (float)Math.sqrt(3), 1 / (float)Math.sqrt(3)),
            new Vec3f(5, 5, 5).normalize());
    }

    @Test
    void normalizeInplace() {
        assertClose(
            new Vec3f(1 / (float)Math.sqrt(3), 1 / (float)Math.sqrt(3), 1 / (float)Math.sqrt(3)),
            new Vec3f(5, 5, 5).normalizeInplace());
    }

    @Test
    void length() {
        assertClose((float)Math.sqrt(3), new Vec3f(1, 1, 1).length());
    }

    @Test
    void lengthSq() {
        assertClose(3, new Vec3f(1, 1, 1).lengthSq());
    }

    @Test
    void distance() {
        assertClose((float)Math.sqrt(3), new Vec3f(1, 1, 1).distance(new Vec3f(2, 2, 2)));
    }

    @Test
    void distanceSq() {
        assertClose(3, new Vec3f(1, 1, 1).distanceSq(new Vec3f(2, 2, 2)));
    }

    @Test
    void angle() {
        assertClose((float)Math.PI / 2, new Vec3f(1, 0, 0).angle(new Vec3f(0, 1, 0)));
    }

    @Test
    void dot() {
        assertClose(32, new Vec3f(1, 2, 3).dot(new Vec3f(4, 5, 6)));
    }

    @Test
    void cross() {
        assertClose(new Vec3f(-3, 6, -3), new Vec3f(1, 2, 3).cross(new Vec3f(4, 5, 6)));
    }

    @Test
    void crossInplace() {
        assertClose(new Vec3f(-3, 6, -3), new Vec3f(1, 2, 3).crossInplace(new Vec3f(4, 5, 6)));
    }

    @Test
    void project() {
        assertClose(new Vec3f(128f / 77, 160f / 77, 192f / 77), new Vec3f(1, 2, 3).project(new Vec3f(4, 5, 6)));
    }

    @Test
    void projectInplace() {
        assertClose(new Vec3f(128f / 77, 160f / 77, 192f / 77), new Vec3f(1, 2, 3).projectInplace(new Vec3f(4, 5, 6)));
    }

    @Test
    void reflect() {
        assertClose(new Vec3f(1, 1, -1), new Vec3f(1, 1, 1).reflect(new Vec3f(0, 0, 1)));
    }

    @Test
    void reflectInplace() {
        assertClose(new Vec3f(1, 1, -1), new Vec3f(1, 1, 1).reflectInplace(new Vec3f(0, 0, 1)));
    }

    @Test
    void clamp() {
        assertClose(new Vec3f(1, 1, 1), new Vec3f(2, 2, 2).clamp(Vec3f.zero(), new Vec3f(1, 1, 1)));
    }

    @Test
    void clampInplace() {
        assertClose(new Vec3f(1, 1, 1), new Vec3f(2, 2, 2).clampInplace(Vec3f.zero(), new Vec3f(1, 1, 1)));
    }

    @Test
    void interpolate() {
        assertClose(new Vec3f(1, 1, 1).multiply(1.5f), new Vec3f(1, 1, 1).interpolate(new Vec3f(2, 2, 2), 0.5f));
    }

    @Test
    void interpolateInplace() {
        assertClose(new Vec3f(1, 1, 1).multiply(1.5f), new Vec3f(1, 1, 1).interpolateInplace(new Vec3f(2, 2, 2), 0.5f));
    }

    @Test
    void transform() {
        assertClose(new Vec3f(3, 3, 3), new Vec3f(1, 1, 1).transform(Mat4f.fromTranslation(2, 2, 2)));
    }

    @Test
    void transformInplace() {
        assertClose(new Vec3f(3, 3, 3), new Vec3f(1, 1, 1).transformInplace(Mat4f.fromTranslation(2, 2, 2)));
    }

    @Test
    void transformNormal() {
        assertClose(new Vec3f(-1, 1, 1), new Vec3f(1, 1, 1).transformNormal(Mat4f.fromRotationZ((float)Math.PI / 2)));
        assertClose(new Vec3f(1, -1, 1), new Vec3f(1, 1, 1).transformNormal(Mat4f.fromRotationX((float)Math.PI / 2)));
        assertClose(new Vec3f(1, 1, -1), new Vec3f(1, 1, 1).transformNormal(Mat4f.fromRotationY((float)Math.PI / 2)));
    }

    @Test
    void transformNormalInplace() {
        assertClose(
            new Vec3f(-1, 1, 1), new Vec3f(1, 1, 1).transformNormalInplace(Mat4f.fromRotationZ((float)Math.PI / 2)));
        assertClose(
            new Vec3f(1, -1, 1), new Vec3f(1, 1, 1).transformNormalInplace(Mat4f.fromRotationX((float)Math.PI / 2)));
        assertClose(
            new Vec3f(1, 1, -1), new Vec3f(1, 1, 1).transformNormalInplace(Mat4f.fromRotationY((float)Math.PI / 2)));
    }

}
