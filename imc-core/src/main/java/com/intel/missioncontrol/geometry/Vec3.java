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
public class Vec3 implements PrimitiveSerializable, BinarySerializable {

    public double x;
    public double y;
    public double z;

    public Vec3() {}

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(Vec3f v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Vec3(PrimitiveDeserializationContext context) {
        String[] coords = context.read().split(",");
        x = Double.parseDouble(coords[0]);
        y = Double.parseDouble(coords[1]);
        z = Double.parseDouble(coords[2]);
    }

    public Vec3(BinaryDeserializationContext context) {
        x = context.readDouble();
        y = context.readDouble();
        z = context.readDouble();
    }

    public static Vec3 zero() {
        return new Vec3(0, 0, 0);
    }

    public static Vec3 one() {
        return new Vec3(1, 1, 1);
    }

    public Vec3 add(Vec3 v) {
        return new Vec3(x + v.x, y + v.y, z + v.z);
    }

    public Vec3 add(double s) {
        return new Vec3(x + s, y + s, z + s);
    }

    public Vec3 addInplace(Vec3 other) {
        x += other.x;
        y += other.y;
        z += other.z;
        return this;
    }

    public Vec3 addInplace(double scalar) {
        x += scalar;
        y += scalar;
        z += scalar;
        return this;
    }

    public Vec3 subtract(Vec3 v) {
        return new Vec3(x - v.x, y - v.y, z - v.z);
    }

    public Vec3 subtract(double s) {
        return new Vec3(x - s, y - s, z - s);
    }

    public Vec3 subtractInplace(Vec3 other) {
        x -= other.x;
        y -= other.y;
        z -= other.z;
        return this;
    }

    public Vec3 subtractInplace(double scalar) {
        x -= scalar;
        y -= scalar;
        z -= scalar;
        return this;
    }

    public Vec3 multiply(Vec3 v) {
        return new Vec3(x * v.x, y * v.y, z * v.z);
    }

    public Vec3 multiply(double s) {
        return new Vec3(x * s, y * s, z * s);
    }

    public Vec3 multiplyInplace(Vec3 v) {
        x *= v.x;
        y *= v.y;
        z *= v.z;
        return this;
    }

    public Vec3 multiplyInplace(double s) {
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    public Vec3 divide(Vec3 v) {
        return new Vec3(x / v.x, y / v.y, z / v.z);
    }

    public Vec3 divide(double s) {
        return new Vec3(x / s, y / s, z / s);
    }

    public Vec3 divideInplace(Vec3 v) {
        x /= v.x;
        y /= v.y;
        z /= v.z;
        return this;
    }

    public Vec3 divideInplace(double s) {
        x /= s;
        y /= s;
        z /= s;
        return this;
    }

    public Vec3 normalize() {
        double length = length();
        if (length != 0) {
            return new Vec3(x / length, y / length, z / length);
        }

        return new Vec3(x, y, z);
    }

    public Vec3 normalizeInplace() {
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
        return x * x + y * y + z * z;
    }

    public double distance(Vec3 v) {
        return Math.sqrt(distanceSq(v));
    }

    public double distanceSq(Vec3 v) {
        double dx = x - v.x;
        double dy = y - v.y;
        double dz = z - v.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double angle(Vec3 v) {
        return Math.acos(dot(v));
    }

    public double dot(Vec3 vec) {
        return x * vec.x + y * vec.y + z * vec.z;
    }

    public Vec3 cross(Vec3 v) {
        double x = this.y * v.z - this.z * v.y;
        double y = this.z * v.x - this.x * v.z;
        double z = this.x * v.y - this.y * v.x;
        return new Vec3(x, y, z);
    }

    public Vec3 crossInplace(Vec3 v) {
        double x = this.y * v.z - this.z * v.y;
        double y = this.z * v.x - this.x * v.z;
        this.z = this.x * v.y - this.y * v.x;
        this.x = x;
        this.y = y;
        return this;
    }

    public Vec3 project(Vec3 v) {
        return v.multiply(dot(v) / v.lengthSq());
    }

    public Vec3 projectInplace(Vec3 v) {
        double d = dot(v) / v.lengthSq();
        this.x = v.x * d;
        this.y = v.y * d;
        this.z = v.z * d;
        return this;
    }

    public Vec3 reflect(Vec3 normal) {
        double dot = x * normal.x + y * normal.y + z * normal.z;
        double tempX = normal.x * dot * 2;
        double tempY = normal.y * dot * 2;
        double tempZ = normal.z * dot * 2;
        return new Vec3(x - tempX, y - tempY, z - tempZ);
    }

    public Vec3 reflectInplace(Vec3 normal) {
        double dot = x * normal.x + y * normal.y + z * normal.z;
        double tempX = normal.x * dot * 2;
        double tempY = normal.y * dot * 2;
        double tempZ = normal.z * dot * 2;
        x = x - tempX;
        y = y - tempY;
        z = z - tempZ;
        return this;
    }

    public Vec3 clamp(Vec3 min, Vec3 max) {
        double x = this.x;
        x = Math.min(x, max.x);
        x = Math.max(x, min.x);
        double y = this.y;
        y = Math.min(y, max.y);
        y = Math.max(y, min.y);
        double z = this.z;
        z = Math.min(z, max.z);
        z = Math.max(z, min.z);
        return new Vec3(x, y, z);
    }

    public Vec3 clampInplace(Vec3 min, Vec3 max) {
        double x = this.x;
        x = Math.min(x, max.x);
        this.x = Math.max(x, min.x);
        double y = this.y;
        y = Math.min(y, max.y);
        this.y = Math.max(y, min.y);
        double z = this.z;
        z = Math.min(z, max.z);
        this.z = Math.max(z, min.z);
        return this;
    }

    public Vec3 interpolate(Vec3 v, double amount) {
        return new Vec3(x + (v.x - x) * amount, y + (v.y - y) * amount, z + (v.z - z) * amount);
    }

    public Vec3 interpolateInplace(Vec3 v, double amount) {
        x = x + (v.x - x) * amount;
        y = y + (v.y - y) * amount;
        z = z + (v.z - z) * amount;
        return this;
    }

    public Vec3 transform(Mat4 matrix) {
        return new Vec3(
            x * matrix.m11 + y * matrix.m21 + z * matrix.m31 + matrix.m41,
            x * matrix.m12 + y * matrix.m22 + z * matrix.m32 + matrix.m42,
            x * matrix.m13 + y * matrix.m23 + z * matrix.m33 + matrix.m43);
    }

    public Vec3 transformInplace(Mat4 matrix) {
        double x0 = x * matrix.m11 + y * matrix.m21 + z * matrix.m31 + matrix.m41;
        double y0 = x * matrix.m12 + y * matrix.m22 + z * matrix.m32 + matrix.m42;
        double z0 = x * matrix.m13 + y * matrix.m23 + z * matrix.m33 + matrix.m43;
        x = x0;
        y = y0;
        z = z0;
        return this;
    }

    public Vec3 transformNormal(Mat4 matrix) {
        return new Vec3(
            x * matrix.m11 + y * matrix.m21 + z * matrix.m31,
            x * matrix.m12 + y * matrix.m22 + z * matrix.m32,
            x * matrix.m13 + y * matrix.m23 + z * matrix.m33);
    }

    public Vec3 transformNormalInplace(Mat4 matrix) {
        double x0 = x * matrix.m11 + y * matrix.m21 + z * matrix.m31;
        double y0 = x * matrix.m12 + y * matrix.m22 + z * matrix.m32;
        double z0 = x * matrix.m13 + y * matrix.m23 + z * matrix.m33;
        x = x0;
        y = y0;
        z = z0;
        return this;
    }

    @Override
    public void serialize(PrimitiveSerializationContext context) {
        context.write(x + "," + y + "," + z);
    }

    @Override
    public void serialize(BinarySerializationContext context) {
        context.writeDouble(x);
        context.writeDouble(y);
        context.writeDouble(z);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(x) + 31 * Double.hashCode(y) + 31 * Double.hashCode(z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vec3)) {
            return false;
        }

        Vec3 other = (Vec3)obj;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

}
