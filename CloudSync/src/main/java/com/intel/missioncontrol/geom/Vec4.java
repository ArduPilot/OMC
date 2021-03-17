/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geom;

public class Vec4 {

    public double x;
    public double y;
    public double z;
    public double w;

    public Vec4() {}

    public Vec4(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    Vec4 add(Vec4 other) {
        x += other.x;
        y += other.y;
        z += other.z;
        w += other.w;
        return this;
    }

    Vec4 sub(Vec4 other) {
        x -= other.x;
        y -= other.y;
        z -= other.z;
        w -= other.w;
        return this;
    }

    Vec4 mul(Vec4 other) {
        x *= other.x;
        y *= other.y;
        z *= other.z;
        w *= other.w;
        return this;
    }

    Vec4 div(Vec4 other) {
        x /= other.x;
        y /= other.y;
        z /= other.z;
        w /= other.w;
        return this;
    }

    public static Vec4 add(Vec4 left, Vec4 right) {
        return new Vec4(left.x + right.x, left.y + right.y, left.z + right.z, left.w + right.w);
    }

    public static Vec4 sub(Vec4 left, Vec4 right) {
        return new Vec4(left.x - right.x, left.y - right.y, left.z - right.z, left.w - right.w);
    }

    public static Vec4 mul(Vec4 left, Vec4 right) {
        return new Vec4(left.x * right.x, left.y * right.y, left.z * right.z, left.w * right.w);
    }

    public static Vec4 div(Vec4 left, Vec4 right) {
        return new Vec4(left.x / right.x, left.y / right.y, left.z / right.z, left.w / right.w);
    }

}
