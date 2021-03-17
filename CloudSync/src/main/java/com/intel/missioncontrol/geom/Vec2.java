/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geom;

public class Vec2 {

    public double x;
    public double y;

    public Vec2() {}

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    Vec2 add(Vec2 other) {
        x += other.x;
        y += other.y;
        return this;
    }

    Vec2 sub(Vec2 other) {
        x -= other.x;
        y -= other.y;
        return this;
    }

    Vec2 mul(Vec2 other) {
        x *= other.x;
        y *= other.y;
        return this;
    }

    Vec2 div(Vec2 other) {
        x /= other.x;
        y /= other.y;
        return this;
    }

    public static Vec2 add(Vec2 left, Vec2 right) {
        return new Vec2(left.x + right.x, left.y + right.y);
    }

    public static Vec2 sub(Vec2 left, Vec2 right) {
        return new Vec2(left.x - right.x, left.y - right.y);
    }

    public static Vec2 mul(Vec2 left, Vec2 right) {
        return new Vec2(left.x * right.x, left.y * right.y);
    }

    public static Vec2 div(Vec2 left, Vec2 right) {
        return new Vec2(left.x / right.x, left.y / right.y);
    }

}
