/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geom;

public class Vec3 {

    public double x;
    public double y;
    public double z;

    public Vec3() {}

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Vec3 add(Vec3 other) {
        x += other.x;
        y += other.y;
        z += other.z;
        return this;
    }

    Vec3 sub(Vec3 other) {
        x -= other.x;
        y -= other.y;
        z -= other.z;
        return this;
    }

    Vec3 mul(Vec3 other) {
        x *= other.x;
        y *= other.y;
        z *= other.z;
        return this;
    }

    Vec3 div(Vec3 other) {
        x /= other.x;
        y /= other.y;
        z /= other.z;
        return this;
    }

    public static Vec3 add(Vec3 left, Vec3 right) {
        return new Vec3(left.x + right.x, left.y + right.y, left.z + right.z);
    }

    public static Vec3 sub(Vec3 left, Vec3 right) {
        return new Vec3(left.x - right.x, left.y - right.y, left.z - right.z);
    }

    public static Vec3 mul(Vec3 left, Vec3 right) {
        return new Vec3(left.x * right.x, left.y * right.y, left.z * right.z);
    }

    public static Vec3 div(Vec3 left, Vec3 right) {
        return new Vec3(left.x / right.x, left.y / right.y, left.z / right.z);
    }

}
