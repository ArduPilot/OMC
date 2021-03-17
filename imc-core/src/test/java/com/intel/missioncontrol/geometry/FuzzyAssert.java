/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

import org.junit.jupiter.api.Assertions;

class FuzzyAssert {

    static void assertClose(float expected, float actual) {
        Assertions.assertTrue(
            FloatHelper.areClose(expected, actual),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

    static void assertClose(double expected, double actual) {
        Assertions.assertTrue(
            DoubleHelper.areClose(expected, actual),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

    static void assertClose(Vec2 expected, Vec2 actual) {
        Assertions.assertTrue(
            DoubleHelper.areClose(expected.x, actual.x) && DoubleHelper.areClose(expected.y, actual.y),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

    static void assertClose(Vec3 expected, Vec3 actual) {
        Assertions.assertTrue(
            DoubleHelper.areClose(expected, actual),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

    static void assertClose(Vec4 expected, Vec4 actual) {
        Assertions.assertTrue(
            DoubleHelper.areClose(expected, actual),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

    static void assertClose(Vec2f expected, Vec2f actual) {
        Assertions.assertTrue(
            FloatHelper.areClose(expected, actual),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

    static void assertClose(Vec3f expected, Vec3f actual) {
        Assertions.assertTrue(
            FloatHelper.areClose(expected, actual),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

    static void assertClose(Vec4f expected, Vec4f actual) {
        Assertions.assertTrue(
            FloatHelper.areClose(expected, actual),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

    static void assertClose(Mat3 expected, Mat3 actual) {
        Assertions.assertTrue(
            DoubleHelper.areClose(expected, actual),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

    static void assertClose(Mat3f expected, Mat3f actual) {
        Assertions.assertTrue(
            FloatHelper.areClose(expected, actual),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

    static void assertClose(Mat4 expected, Mat4 actual) {
        Assertions.assertTrue(
            DoubleHelper.areClose(expected, actual),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

    static void assertClose(Mat4f expected, Mat4f actual) {
        Assertions.assertTrue(
            FloatHelper.areClose(expected, actual),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

    static void assertClose(Quaternion expected, Quaternion actual) {
        Assertions.assertTrue(
            DoubleHelper.areClose(expected, actual),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

    static void assertClose(Quaternionf expected, Quaternionf actual) {
        Assertions.assertTrue(
            FloatHelper.areClose(expected, actual),
            () -> "Values not close: expected = " + expected + ", actual = " + actual);
    }

}
