/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

import com.intel.missioncontrol.serialization.BinaryDeserializationContext;
import com.intel.missioncontrol.serialization.BinarySerializable;
import com.intel.missioncontrol.serialization.BinarySerializationContext;
import com.intel.missioncontrol.serialization.PrimitiveDeserializationContext;
import com.intel.missioncontrol.serialization.PrimitiveSerializable;
import com.intel.missioncontrol.serialization.PrimitiveSerializationContext;

@SuppressWarnings("unused")
public class Vec2 implements PrimitiveSerializable, BinarySerializable {

    public double x;
    public double y;

    public Vec2() {}

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(Vec2f v) {
        this.x = v.x;
        this.y = v.y;
    }

    public Vec2(PrimitiveDeserializationContext context) {
        String[] coords = context.read().split(",");
        x = Double.parseDouble(coords[0]);
        y = Double.parseDouble(coords[1]);
    }

    public Vec2(BinaryDeserializationContext context) {
        x = context.readDouble();
        y = context.readDouble();
    }

    public static Vec2 zero() {
        return new Vec2(0, 0);
    }

    public static Vec2 one() {
        return new Vec2(1, 1);
    }

    public Vec2 add(Vec2 v) {
        return new Vec2(x + v.x, y + v.y);
    }

    public Vec2 add(double s) {
        return new Vec2(x + s, y + s);
    }

    public Vec2 addInplace(Vec2 v) {
        x += v.x;
        y += v.y;
        return this;
    }

    public Vec2 addInplace(double s) {
        x += s;
        y += s;
        return this;
    }

    public Vec2 subtract(Vec2 v) {
        return new Vec2(x - v.x, y - v.y);
    }

    public Vec2 subtract(double s) {
        return new Vec2(x - s, y - s);
    }

    public Vec2 subtractInplace(Vec2 v) {
        x -= v.x;
        y -= v.y;
        return this;
    }

    public Vec2 subtractInplace(double s) {
        x -= s;
        y -= s;
        return this;
    }

    public Vec2 multiply(Vec2 v) {
        return new Vec2(x * v.x, y * v.y);
    }

    public Vec2 multiply(double s) {
        return new Vec2(x * s, y * s);
    }

    public Vec2 multiplyInplace(Vec2 v) {
        x *= v.x;
        y *= v.y;
        return this;
    }

    public Vec2 multiplyInplace(double s) {
        x *= s;
        y *= s;
        return this;
    }

    public Vec2 divide(Vec2 v) {
        return new Vec2(x / v.x, y / v.y);
    }

    public Vec2 divide(double s) {
        return new Vec2(x / s, y / s);
    }

    public Vec2 divideInplace(Vec2 v) {
        x /= v.x;
        y /= v.y;
        return this;
    }

    public Vec2 divideInplace(double s) {
        x /= s;
        y /= s;
        return this;
    }

    public Vec2 normalize() {
        double length = length();
        if (length != 0) {
            return new Vec2(x / length, y / length);
        }

        return new Vec2(x, y);
    }

    public Vec2 normalizeInplace() {
        double length = length();
        if (length != 0) {
            return divideInplace(length);
        }

        return this;
    }

    public double length() {
        return Math.sqrt(lengthSq());
    }

    public double lengthSq() {
        return x * x + y * y;
    }

    public double distance(Vec2 v) {
        return Math.sqrt(distanceSq(v));
    }

    public double distanceSq(Vec2 v) {
        double dx = x - v.x;
        double dy = y - v.y;
        return dx * dx + dy * dy;
    }

    public double angle(Vec2 v) {
        return Math.atan2(v.y, v.x) - Math.atan2(y, x);
    }

    @Override
    public void serialize(PrimitiveSerializationContext context) {
        context.write(x + "," + y);
    }

    @Override
    public void serialize(BinarySerializationContext context) {
        context.writeDouble(x);
        context.writeDouble(y);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(x) + 31 * Double.hashCode(y);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vec2)) {
            return false;
        }

        Vec2 other = (Vec2)obj;
        return x == other.x && y == other.y;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

}
