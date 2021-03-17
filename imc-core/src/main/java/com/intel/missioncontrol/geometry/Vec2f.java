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
public class Vec2f implements PrimitiveSerializable, BinarySerializable {

    public float x;
    public float y;

    public Vec2f() {}

    public Vec2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2f(Vec2 v) {
        this.x = (float)v.x;
        this.y = (float)v.y;
    }

    public Vec2f(PrimitiveDeserializationContext context) {
        String[] coords = context.read().split(",");
        x = Float.parseFloat(coords[0]);
        y = Float.parseFloat(coords[1]);
    }

    public Vec2f(BinaryDeserializationContext context) {
        x = context.readFloat();
        y = context.readFloat();
    }

    public static Vec2f zero() {
        return new Vec2f(0, 0);
    }

    public static Vec2f one() {
        return new Vec2f(1, 1);
    }

    public Vec2f add(Vec2f v) {
        return new Vec2f(x + v.x, y + v.y);
    }

    public Vec2f add(float s) {
        return new Vec2f(x + s, y + s);
    }

    public Vec2f addInplace(Vec2f v) {
        x += v.x;
        y += v.y;
        return this;
    }

    public Vec2f addInplace(float s) {
        x += s;
        y += s;
        return this;
    }

    public Vec2f subtract(Vec2f v) {
        return new Vec2f(x - v.x, y - v.y);
    }

    public Vec2f subtract(float s) {
        return new Vec2f(x - s, y - s);
    }

    public Vec2f subtractInplace(Vec2f v) {
        x -= v.x;
        y -= v.y;
        return this;
    }

    public Vec2f subtractInplace(float s) {
        x -= s;
        y -= s;
        return this;
    }

    public Vec2f multiply(Vec2f v) {
        return new Vec2f(x * v.x, y * v.y);
    }

    public Vec2f multiply(float s) {
        return new Vec2f(x * s, y * s);
    }

    public Vec2f multiplyInplace(Vec2f v) {
        x *= v.x;
        y *= v.y;
        return this;
    }

    public Vec2f multiplyInplace(float s) {
        x *= s;
        y *= s;
        return this;
    }

    public Vec2f divide(Vec2f v) {
        return new Vec2f(x / v.x, y / v.y);
    }

    public Vec2f divide(float s) {
        return new Vec2f(x / s, y / s);
    }

    public Vec2f divideInplace(Vec2f v) {
        x /= v.x;
        y /= v.y;
        return this;
    }

    public Vec2f divideInplace(float s) {
        x /= s;
        y /= s;
        return this;
    }

    public Vec2f normalize() {
        float length = length();
        if (length != 0) {
            return new Vec2f(x / length, y / length);
        }

        return new Vec2f(x, y);
    }

    public Vec2f normalizeInplace() {
        float length = length();
        if (length != 0) {
            return divideInplace(length);
        }

        return this;
    }

    public float length() {
        return (float)Math.sqrt(lengthSq());
    }

    public float lengthSq() {
        return x * x + y * y;
    }

    public float distance(Vec2f v) {
        return (float)Math.sqrt(distanceSq(v));
    }

    public float distanceSq(Vec2f v) {
        float dx = x - v.x;
        float dy = y - v.y;
        return dx * dx + dy * dy;
    }

    public float angle(Vec2f v) {
        return (float)(Math.atan2(v.y, v.x) - Math.atan2(y, x));
    }

    @Override
    public void serialize(PrimitiveSerializationContext context) {
        context.write(x + "," + y);
    }

    @Override
    public void serialize(BinarySerializationContext context) {
        context.writeFloat(x);
        context.writeFloat(y);
    }

    @Override
    public int hashCode() {
        return Float.hashCode(x) + 31 * Float.hashCode(y);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vec2f)) {
            return false;
        }

        Vec2f other = (Vec2f)obj;
        return x == other.x && y == other.y;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

}
