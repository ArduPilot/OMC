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
public class Vec4 implements PrimitiveSerializable, BinarySerializable {

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

    public Vec4(Vec4f v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.w = v.w;
    }

    public Vec4(PrimitiveDeserializationContext context) {
        String[] coords = context.read().split(",");
        x = Double.parseDouble(coords[0]);
        y = Double.parseDouble(coords[1]);
        z = Double.parseDouble(coords[2]);
        w = Double.parseDouble(coords[3]);
    }

    public Vec4(BinaryDeserializationContext context) {
        x = context.readDouble();
        y = context.readDouble();
        z = context.readDouble();
        w = context.readDouble();
    }

    public static Vec4 zero() {
        return new Vec4(0, 0, 0, 0);
    }

    public static Vec4 one() {
        return new Vec4(1, 1, 1, 1);
    }

    public Vec4 add(Vec4 v) {
        return new Vec4(x + v.x, y + v.y, z + v.z, w + v.w);
    }

    public Vec4 add(double s) {
        return new Vec4(x + s, y + s, z + s, w + s);
    }

    public Vec4 addInplace(Vec4 v) {
        x += v.x;
        y += v.y;
        z += v.z;
        w += v.w;
        return this;
    }

    public Vec4 addInplace(double s) {
        x += s;
        y += s;
        z += s;
        w += s;
        return this;
    }

    public Vec4 subtract(Vec4 v) {
        return new Vec4(x - v.x, y - v.y, z - v.z, w - v.w);
    }

    public Vec4 subtract(double s) {
        return new Vec4(x - s, y - s, z - s, w - s);
    }

    public Vec4 subtractInplace(Vec4 v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
        w -= v.w;
        return this;
    }

    public Vec4 subtractInplace(double s) {
        x -= s;
        y -= s;
        z -= s;
        w -= s;
        return this;
    }

    public Vec4 multiply(Vec4 v) {
        return new Vec4(x * v.x, y * v.y, z * v.z, w * v.w);
    }

    public Vec4 multiply(double s) {
        return new Vec4(x * s, y * s, z * s, w * s);
    }

    public Vec4 multiplyInplace(Vec4 v) {
        x *= v.x;
        y *= v.y;
        z *= v.z;
        w *= v.w;
        return this;
    }

    public Vec4 multiplyInplace(double s) {
        x *= s;
        y *= s;
        z *= s;
        w *= s;
        return this;
    }

    public Vec4 divide(Vec4 v) {
        return new Vec4(x / v.x, y / v.y, z / v.z, w / v.w);
    }

    public Vec4 divide(double s) {
        return new Vec4(x / s, y / s, z / s, w / s);
    }

    public Vec4 divideInplace(Vec4 v) {
        x /= v.x;
        y /= v.y;
        z /= v.z;
        w /= v.w;
        return this;
    }

    public Vec4 divideInplace(double s) {
        x /= s;
        y /= s;
        z /= s;
        w /= s;
        return this;
    }

    public Vec4 normalize() {
        double length = length();
        if (length != 0) {
            return new Vec4(x / length, y / length, z / length, w / length);
        }

        return new Vec4(x, y, z, w);
    }

    public Vec4 normalizeInplace() {
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
        return x * x + y * y + z * z + w * w;
    }

    public double distance(Vec4 v) {
        return Math.sqrt(distanceSq(v));
    }

    public double distanceSq(Vec4 v) {
        double dx = x - v.x;
        double dy = y - v.y;
        double dz = z - v.z;
        double dw = w - v.w;
        return dx * dx + dy * dy + dz * dz + dw * dw;
    }

    public double angle(Vec4 v) {
        return Math.acos(dot(v));
    }

    public double dot(Vec4 v) {
        return x * v.x + y * v.y + z * v.z + w * v.w;
    }

    public Vec4 project(Vec4 v) {
        return v.multiply(dot(v) / v.lengthSq());
    }

    public Vec4 projectInplace(Vec4 v) {
        double d = dot(v) / v.lengthSq();
        this.x = v.x * d;
        this.y = v.y * d;
        this.z = v.z * d;
        this.w = v.w * d;
        return this;
    }

    public Vec4 clamp(Vec4 min, Vec4 max) {
        double x = this.x;
        x = Math.min(x, max.x);
        x = Math.max(x, min.x);
        double y = this.y;
        y = Math.min(y, max.y);
        y = Math.max(y, min.y);
        double z = this.z;
        z = Math.min(z, max.z);
        z = Math.max(z, min.z);
        double w = this.w;
        w = Math.min(w, max.w);
        w = Math.max(w, min.w);
        return new Vec4(x, y, z, w);
    }

    public Vec4 clampInplace(Vec4 min, Vec4 max) {
        double x = this.x;
        x = Math.min(x, max.x);
        this.x = Math.max(x, min.x);
        double y = this.y;
        y = Math.min(y, max.y);
        this.y = Math.max(y, min.y);
        double z = this.z;
        z = Math.min(z, max.z);
        this.z = Math.max(z, min.z);
        double w = this.w;
        w = Math.min(w, max.w);
        this.w = Math.max(w, min.w);
        return this;
    }

    public Vec4 interpolate(Vec4 v, double amount) {
        return new Vec4(x + (v.x - x) * amount, y + (v.y - y) * amount, z + (v.z - z) * amount, w + (v.w - w) * amount);
    }

    public Vec4 interpolateInplace(Vec4 v, double amount) {
        x = x + (v.x - x) * amount;
        y = y + (v.y - y) * amount;
        z = z + (v.z - z) * amount;
        w = w + (v.w - w) * amount;
        return this;
    }

    public Vec4 transform(Mat4 matrix) {
        return new Vec4(
            x * matrix.m11 + y * matrix.m21 + z * matrix.m31 + w * matrix.m41,
            x * matrix.m12 + y * matrix.m22 + z * matrix.m32 + w * matrix.m42,
            x * matrix.m13 + y * matrix.m23 + z * matrix.m33 + w * matrix.m43,
            x * matrix.m14 + y * matrix.m24 + z * matrix.m34 + w * matrix.m44);
    }

    public Vec4 transformInplace(Mat4 matrix) {
        double t0 = x * matrix.m11 + y * matrix.m21 + z * matrix.m31 + w * matrix.m41;
        double t1 = x * matrix.m12 + y * matrix.m22 + z * matrix.m32 + w * matrix.m42;
        double t2 = x * matrix.m13 + y * matrix.m23 + z * matrix.m33 + w * matrix.m43;
        double t3 = x * matrix.m14 + y * matrix.m24 + z * matrix.m34 + w * matrix.m44;
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
        context.writeDouble(x);
        context.writeDouble(y);
        context.writeDouble(z);
        context.writeDouble(w);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(x) + 31 * Double.hashCode(y) + 31 * Double.hashCode(z) + 31 * Double.hashCode(w);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vec4)) {
            return false;
        }

        Vec4 other = (Vec4)obj;
        return x == other.x && y == other.y && z == other.z && w == other.w;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + ", " + w + "]";
    }

}
