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
public class Vec4f implements PrimitiveSerializable, BinarySerializable {

    public float x;
    public float y;
    public float z;
    public float w;

    public Vec4f() {}

    public Vec4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4f(Vec4 v) {
        this.x = (float)v.x;
        this.y = (float)v.y;
        this.z = (float)v.z;
        this.w = (float)v.w;
    }

    public Vec4f(PrimitiveDeserializationContext context) {
        String[] coords = context.read().split(",");
        x = Float.parseFloat(coords[0]);
        y = Float.parseFloat(coords[1]);
        z = Float.parseFloat(coords[2]);
        w = Float.parseFloat(coords[3]);
    }

    public Vec4f(BinaryDeserializationContext context) {
        x = context.readFloat();
        y = context.readFloat();
        z = context.readFloat();
        w = context.readFloat();
    }

    public static Vec4f zero() {
        return new Vec4f(0, 0, 0, 0);
    }

    public static Vec4f one() {
        return new Vec4f(1, 1, 1, 1);
    }

    public Vec4f add(Vec4f v) {
        return new Vec4f(x + v.x, y + v.y, z + v.z, w + v.w);
    }

    public Vec4f add(float s) {
        return new Vec4f(x + s, y + s, z + s, w + s);
    }

    public Vec4f addInplace(Vec4f v) {
        x += v.x;
        y += v.y;
        z += v.z;
        w += v.w;
        return this;
    }

    public Vec4f addInplace(float s) {
        x += s;
        y += s;
        z += s;
        w += s;
        return this;
    }

    public Vec4f subtract(Vec4f v) {
        return new Vec4f(x - v.x, y - v.y, z - v.z, w - v.w);
    }

    public Vec4f subtract(float s) {
        return new Vec4f(x - s, y - s, z - s, w - s);
    }

    public Vec4f subtractInplace(Vec4f v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
        w -= v.w;
        return this;
    }

    public Vec4f subtractInplace(float s) {
        x -= s;
        y -= s;
        z -= s;
        w -= s;
        return this;
    }

    public Vec4f multiply(Vec4f v) {
        return new Vec4f(x * v.x, y * v.y, z * v.z, w * v.w);
    }

    public Vec4f multiply(float s) {
        return new Vec4f(x * s, y * s, z * s, w * s);
    }

    public Vec4f multiplyInplace(Vec4f v) {
        x *= v.x;
        y *= v.y;
        z *= v.z;
        w *= v.w;
        return this;
    }

    public Vec4f multiplyInplace(float s) {
        x *= s;
        y *= s;
        z *= s;
        w *= s;
        return this;
    }

    public Vec4f divide(Vec4f v) {
        return new Vec4f(x / v.x, y / v.y, z / v.z, w / v.w);
    }

    public Vec4f divide(float s) {
        return new Vec4f(x / s, y / s, z / s, w / s);
    }

    public Vec4f divideInplace(Vec4f v) {
        x /= v.x;
        y /= v.y;
        z /= v.z;
        w /= v.w;
        return this;
    }

    public Vec4f divideInplace(float s) {
        x /= s;
        y /= s;
        z /= s;
        w /= s;
        return this;
    }

    public Vec4f normalize() {
        float length = length();
        if (length != 0) {
            return new Vec4f(x / length, y / length, z / length, w / length);
        }

        return new Vec4f(x, y, z, w);
    }

    public Vec4f normalizeInplace() {
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
        return x * x + y * y + z * z + w * w;
    }

    public float distance(Vec4f v) {
        return (float)Math.sqrt(distanceSq(v));
    }

    public float distanceSq(Vec4f v) {
        float dx = x - v.x;
        float dy = y - v.y;
        float dz = z - v.z;
        float dw = w - v.w;
        return dx * dx + dy * dy + dz * dz + dw * dw;
    }

    public float angle(Vec4f v) {
        return (float)Math.acos(dot(v));
    }

    public float dot(Vec4f v) {
        return x * v.x + y * v.y + z * v.z + w * v.w;
    }

    public Vec4f project(Vec4f v) {
        return v.multiply(dot(v) / v.lengthSq());
    }

    public Vec4f projectInplace(Vec4f v) {
        float d = dot(v) / v.lengthSq();
        this.x = v.x * d;
        this.y = v.y * d;
        this.z = v.z * d;
        this.w = v.w * d;
        return this;
    }

    public Vec4f clamp(Vec4f min, Vec4f max) {
        float x = this.x;
        x = Math.min(x, max.x);
        x = Math.max(x, min.x);
        float y = this.y;
        y = Math.min(y, max.y);
        y = Math.max(y, min.y);
        float z = this.z;
        z = Math.min(z, max.z);
        z = Math.max(z, min.z);
        float w = this.w;
        w = Math.min(w, max.w);
        w = Math.max(w, min.w);
        return new Vec4f(x, y, z, w);
    }

    public Vec4f clampInplace(Vec4f min, Vec4f max) {
        float x = this.x;
        x = Math.min(x, max.x);
        this.x = Math.max(x, min.x);
        float y = this.y;
        y = Math.min(y, max.y);
        this.y = Math.max(y, min.y);
        float z = this.z;
        z = Math.min(z, max.z);
        this.z = Math.max(z, min.z);
        float w = this.w;
        w = Math.min(w, max.w);
        this.w = Math.max(w, min.w);
        return this;
    }

    public Vec4f interpolate(Vec4f v, float amount) {
        return new Vec4f(
            x + (v.x - x) * amount, y + (v.y - y) * amount, z + (v.z - z) * amount, w + (v.w - w) * amount);
    }

    public Vec4f interpolateInplace(Vec4f v, float amount) {
        x = x + (v.x - x) * amount;
        y = y + (v.y - y) * amount;
        z = z + (v.z - z) * amount;
        w = w + (v.w - w) * amount;
        return this;
    }

    public Vec4f transform(Mat4f matrix) {
        return new Vec4f(
            x * matrix.m11 + y * matrix.m21 + z * matrix.m31 + w * matrix.m41,
            x * matrix.m12 + y * matrix.m22 + z * matrix.m32 + w * matrix.m42,
            x * matrix.m13 + y * matrix.m23 + z * matrix.m33 + w * matrix.m43,
            x * matrix.m14 + y * matrix.m24 + z * matrix.m34 + w * matrix.m44);
    }

    public Vec4f transformInplace(Mat4f matrix) {
        float t0 = x * matrix.m11 + y * matrix.m21 + z * matrix.m31 + w * matrix.m41;
        float t1 = x * matrix.m12 + y * matrix.m22 + z * matrix.m32 + w * matrix.m42;
        float t2 = x * matrix.m13 + y * matrix.m23 + z * matrix.m33 + w * matrix.m43;
        float t3 = x * matrix.m14 + y * matrix.m24 + z * matrix.m34 + w * matrix.m44;
        x = t0;
        y = t1;
        z = t2;
        w = t3;
        return this;
    }

    @Override
    public void serialize(PrimitiveSerializationContext context) {
        context.write(x + "," + y + "," + z + "," + w);
    }

    @Override
    public void serialize(BinarySerializationContext context) {
        context.writeFloat(x);
        context.writeFloat(y);
        context.writeFloat(z);
        context.writeFloat(w);
    }

    @Override
    public int hashCode() {
        return Float.hashCode(x) + 31 * Float.hashCode(y) + 31 * Float.hashCode(z) + 31 * Float.hashCode(w);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vec4f)) {
            return false;
        }

        Vec4f other = (Vec4f)obj;
        return x == other.x && y == other.y && z == other.z && w == other.w;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + ", " + w + "]";
    }

}
