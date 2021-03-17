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
public class Vec3f implements PrimitiveSerializable, BinarySerializable {

    public float x;
    public float y;
    public float z;

    public Vec3f() {}

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3f(Vec3 v) {
        this.x = (float)v.x;
        this.y = (float)v.y;
        this.z = (float)v.z;
    }

    public Vec3f(PrimitiveDeserializationContext context) {
        String[] coords = context.read().split(",");
        x = Float.parseFloat(coords[0]);
        y = Float.parseFloat(coords[1]);
        z = Float.parseFloat(coords[2]);
    }

    public Vec3f(BinaryDeserializationContext context) {
        x = context.readFloat();
        y = context.readFloat();
        z = context.readFloat();
    }

    public static Vec3f zero() {
        return new Vec3f(0, 0, 0);
    }

    public static Vec3f one() {
        return new Vec3f(1, 1, 1);
    }

    public Vec3f add(Vec3f v) {
        return new Vec3f(x + v.x, y + v.y, z + v.z);
    }

    public Vec3f add(float s) {
        return new Vec3f(x + s, y + s, z + s);
    }

    public Vec3f addInplace(Vec3f other) {
        x += other.x;
        y += other.y;
        z += other.z;
        return this;
    }

    public Vec3f addInplace(float scalar) {
        x += scalar;
        y += scalar;
        z += scalar;
        return this;
    }

    public Vec3f subtract(Vec3f v) {
        return new Vec3f(x - v.x, y - v.y, z - v.z);
    }

    public Vec3f subtract(float s) {
        return new Vec3f(x - s, y - s, z - s);
    }

    public Vec3f subtractInplace(Vec3f other) {
        x -= other.x;
        y -= other.y;
        z -= other.z;
        return this;
    }

    public Vec3f subtractInplace(float scalar) {
        x -= scalar;
        y -= scalar;
        z -= scalar;
        return this;
    }

    public Vec3f multiply(Vec3f v) {
        return new Vec3f(x * v.x, y * v.y, z * v.z);
    }

    public Vec3f multiply(float s) {
        return new Vec3f(x * s, y * s, z * s);
    }

    public Vec3f multiplyInplace(Vec3f v) {
        x *= v.x;
        y *= v.y;
        z *= v.z;
        return this;
    }

    public Vec3f multiplyInplace(float s) {
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    public Vec3f divide(Vec3f v) {
        return new Vec3f(x / v.x, y / v.y, z / v.z);
    }

    public Vec3f divide(float s) {
        return new Vec3f(x / s, y / s, z / s);
    }

    public Vec3f divideInplace(Vec3f v) {
        x /= v.x;
        y /= v.y;
        z /= v.z;
        return this;
    }

    public Vec3f divideInplace(float s) {
        x /= s;
        y /= s;
        z /= s;
        return this;
    }

    public Vec3f normalize() {
        float length = length();
        if (length != 0) {
            return new Vec3f(x / length, y / length, z / length);
        }

        return new Vec3f(x, y, z);
    }

    public Vec3f normalizeInplace() {
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
        return x * x + y * y + z * z;
    }

    public float distance(Vec3f v) {
        return (float)Math.sqrt(distanceSq(v));
    }

    public float distanceSq(Vec3f v) {
        float dx = x - v.x;
        float dy = y - v.y;
        float dz = z - v.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public float angle(Vec3f v) {
        return (float)Math.acos(dot(v));
    }

    public float dot(Vec3f vec) {
        return x * vec.x + y * vec.y + z * vec.z;
    }

    public Vec3f cross(Vec3f v) {
        float x = this.y * v.z - this.z * v.y;
        float y = this.z * v.x - this.x * v.z;
        float z = this.x * v.y - this.y * v.x;
        return new Vec3f(x, y, z);
    }

    public Vec3f crossInplace(Vec3f v) {
        float x = this.y * v.z - this.z * v.y;
        float y = this.z * v.x - this.x * v.z;
        this.z = this.x * v.y - this.y * v.x;
        this.x = x;
        this.y = y;
        return this;
    }

    public Vec3f project(Vec3f v) {
        return v.multiply(dot(v) / v.lengthSq());
    }

    public Vec3f projectInplace(Vec3f v) {
        float d = dot(v) / v.lengthSq();
        this.x = v.x * d;
        this.y = v.y * d;
        this.z = v.z * d;
        return this;
    }

    public Vec3f reflect(Vec3f normal) {
        float dot = x * normal.x + y * normal.y + z * normal.z;
        float tempX = normal.x * dot * 2;
        float tempY = normal.y * dot * 2;
        float tempZ = normal.z * dot * 2;
        return new Vec3f(x - tempX, y - tempY, z - tempZ);
    }

    public Vec3f reflectInplace(Vec3f normal) {
        float dot = x * normal.x + y * normal.y + z * normal.z;
        float tempX = normal.x * dot * 2;
        float tempY = normal.y * dot * 2;
        float tempZ = normal.z * dot * 2;
        x = x - tempX;
        y = y - tempY;
        z = z - tempZ;
        return this;
    }

    public Vec3f clamp(Vec3f min, Vec3f max) {
        float x = this.x;
        x = Math.min(x, max.x);
        x = Math.max(x, min.x);
        float y = this.y;
        y = Math.min(y, max.y);
        y = Math.max(y, min.y);
        float z = this.z;
        z = Math.min(z, max.z);
        z = Math.max(z, min.z);
        return new Vec3f(x, y, z);
    }

    public Vec3f clampInplace(Vec3f min, Vec3f max) {
        float x = this.x;
        x = Math.min(x, max.x);
        this.x = Math.max(x, min.x);
        float y = this.y;
        y = Math.min(y, max.y);
        this.y = Math.max(y, min.y);
        float z = this.z;
        z = Math.min(z, max.z);
        this.z = Math.max(z, min.z);
        return this;
    }

    public Vec3f interpolate(Vec3f v, float amount) {
        return new Vec3f(x + (v.x - x) * amount, y + (v.y - y) * amount, z + (v.z - z) * amount);
    }

    public Vec3f interpolateInplace(Vec3f v, float amount) {
        x = x + (v.x - x) * amount;
        y = y + (v.y - y) * amount;
        z = z + (v.z - z) * amount;
        return this;
    }

    public Vec3f transform(Mat4f matrix) {
        return new Vec3f(
            x * matrix.m11 + y * matrix.m21 + z * matrix.m31 + matrix.m41,
            x * matrix.m12 + y * matrix.m22 + z * matrix.m32 + matrix.m42,
            x * matrix.m13 + y * matrix.m23 + z * matrix.m33 + matrix.m43);
    }

    public Vec3f transformInplace(Mat4f matrix) {
        float x0 = x * matrix.m11 + y * matrix.m21 + z * matrix.m31 + matrix.m41;
        float y0 = x * matrix.m12 + y * matrix.m22 + z * matrix.m32 + matrix.m42;
        float z0 = x * matrix.m13 + y * matrix.m23 + z * matrix.m33 + matrix.m43;
        x = x0;
        y = y0;
        z = z0;
        return this;
    }

    public Vec3f transformNormal(Mat4f matrix) {
        return new Vec3f(
            x * matrix.m11 + y * matrix.m21 + z * matrix.m31,
            x * matrix.m12 + y * matrix.m22 + z * matrix.m32,
            x * matrix.m13 + y * matrix.m23 + z * matrix.m33);
    }

    public Vec3f transformNormalInplace(Mat4f matrix) {
        float x0 = x * matrix.m11 + y * matrix.m21 + z * matrix.m31;
        float y0 = x * matrix.m12 + y * matrix.m22 + z * matrix.m32;
        float z0 = x * matrix.m13 + y * matrix.m23 + z * matrix.m33;
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
        context.writeFloat(x);
        context.writeFloat(y);
        context.writeFloat(z);
    }

    @Override
    public int hashCode() {
        return Float.hashCode(x) + 31 * Float.hashCode(y) + 31 * Float.hashCode(z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vec3f)) {
            return false;
        }

        Vec3f other = (Vec3f)obj;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

}
