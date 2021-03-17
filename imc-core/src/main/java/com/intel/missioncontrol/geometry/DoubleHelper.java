/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

@SuppressWarnings({"unused", "WeakerAccess"})
public class DoubleHelper {

    public static boolean areClose(double a, double b) {
        if (a == b) {
            return true;
        }

        double eps = (Math.abs(a) + Math.abs(b) + 10.0) * 2.2204460492503131e-016;
        double delta = a - b;
        return -eps < delta && eps > delta;
    }

    public static boolean isCloseToZero(double v) {
        return Math.abs(v) < 10.0 * 2.2204460492503131e-016;
    }

    public static boolean greaterThan(double a, double b) {
        return a > b && !areClose(a, b);
    }

    public static boolean greaterThanOrClose(double a, double b) {
        return a > b || areClose(a, b);
    }

    public static boolean lessThan(double a, double b) {
        return a < b && !areClose(a, b);
    }

    public static boolean lessThanOrClose(double a, double b) {
        return a < b || areClose(a, b);
    }

    public static boolean areClose(Vec2 a, Vec2 b) {
        return areClose(a.x, b.x) && areClose(a.y, b.y);
    }

    public static boolean areClose(Vec3 a, Vec3 b) {
        return areClose(a.x, b.x) && areClose(a.y, b.y) && areClose(a.z, b.z);
    }

    public static boolean areClose(Vec4 a, Vec4 b) {
        return areClose(a.x, b.x) && areClose(a.y, b.y) && areClose(a.z, b.z) && areClose(a.w, b.w);
    }

    public static boolean areClose(Mat3 a, Mat3 b) {
        return areClose(a.m11, b.m11)
            && areClose(a.m12, b.m12)
            && areClose(a.m13, b.m13)
            && areClose(a.m21, b.m21)
            && areClose(a.m22, b.m22)
            && areClose(a.m23, b.m23)
            && areClose(a.m31, b.m31)
            && areClose(a.m32, b.m32)
            && areClose(a.m33, b.m33);
    }

    public static boolean areClose(Mat4 a, Mat4 b) {
        return areClose(a.m11, b.m11)
            && areClose(a.m12, b.m12)
            && areClose(a.m13, b.m13)
            && areClose(a.m14, b.m14)
            && areClose(a.m21, b.m21)
            && areClose(a.m22, b.m22)
            && areClose(a.m23, b.m23)
            && areClose(a.m24, b.m24)
            && areClose(a.m31, b.m31)
            && areClose(a.m32, b.m32)
            && areClose(a.m33, b.m33)
            && areClose(a.m34, b.m34)
            && areClose(a.m41, b.m41)
            && areClose(a.m42, b.m42)
            && areClose(a.m43, b.m43)
            && areClose(a.m44, b.m44);
    }

    public static boolean areClose(Quaternion a, Quaternion b) {
        return areClose(a.x, b.x) && areClose(a.y, b.y) && areClose(a.z, b.z) && areClose(a.w, b.w);
    }

}
