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
public final class Quaternionf implements PrimitiveSerializable, BinarySerializable {

    public float x, y, z, w;

    public Quaternionf() {
        x = 0;
        y = 0;
        z = 0;
        w = 1;
    }

    public Quaternionf(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quaternionf(Quaternion q) {
        this.x = (float)q.x;
        this.y = (float)q.y;
        this.z = (float)q.z;
        this.w = (float)q.w;
    }

    public Quaternionf(PrimitiveDeserializationContext context) {
        String[] coords = context.read().split(",");
        x = Float.parseFloat(coords[0]);
        y = Float.parseFloat(coords[1]);
        z = Float.parseFloat(coords[2]);
        w = Float.parseFloat(coords[3]);
    }

    public Quaternionf(BinaryDeserializationContext context) {
        x = context.readFloat();
        y = context.readFloat();
        z = context.readFloat();
        w = context.readFloat();
    }

    public static Quaternionf fromRotationMatrix(Mat3f matrix) {
        return fromRotationMatrix(
            matrix.m11, matrix.m12, matrix.m13, matrix.m21, matrix.m22, matrix.m23, matrix.m31, matrix.m32, matrix.m33);
    }

    public static Quaternionf fromRotationMatrix(Mat4f matrix) {
        return fromRotationMatrix(
            matrix.m11, matrix.m12, matrix.m13, matrix.m21, matrix.m22, matrix.m23, matrix.m31, matrix.m32, matrix.m33);
    }

    private static Quaternionf fromRotationMatrix(
            float m11, float m12, float m13, float m21, float m22, float m23, float m31, float m32, float m33) {
        float trace = m11 + m22 + m33;

        if (trace > 0.0) {
            float s = (float)Math.sqrt(trace + 1.0f);
            float w = s * 0.5f;
            s = 0.5f / s;
            return new Quaternionf((m23 - m32) * s, (m31 - m13) * s, (m12 - m21) * s, w);
        }

        if (m11 >= m22 && m11 >= m33) {
            float s = (float)Math.sqrt(1.0f + m11 - m22 - m33);
            float invS = 0.5f / s;
            return new Quaternionf(0.5f * s, (m12 + m21) * invS, (m13 + m31) * invS, (m23 - m32) * invS);
        } else if (m22 > m33) {
            float s = (float)Math.sqrt(1.0f + m22 - m11 - m33);
            float invS = 0.5f / s;
            return new Quaternionf((m21 + m12) * invS, 0.5f * s, (m32 + m23) * invS, (m31 - m13) * invS);
        }

        float s = (float)Math.sqrt(1.0f + m33 - m11 - m22);
        float invS = 0.5f / s;
        return new Quaternionf((m31 + m13) * invS, (m32 + m23) * invS, 0.5f * s, (m12 - m21) * invS);
    }

    public static Quaternionf fromAxisAngle(Vec3f axis, float angle) {
        float halfAngle = angle * 0.5f;
        float s = (float)Math.sin(halfAngle);
        float c = (float)Math.cos(halfAngle);
        return new Quaternionf(axis.x * s, axis.y * s, axis.z * s, c);
    }

    public static Quaternionf fromYawPitchRoll(float yaw, float pitch, float roll) {
        float sr, cr, sp, cp, sy, cy;
        float halfRoll = roll * 0.5f;
        sr = (float)Math.sin(halfRoll);
        cr = (float)Math.cos(halfRoll);
        float halfPitch = pitch * 0.5f;
        sp = (float)Math.sin(halfPitch);
        cp = (float)Math.cos(halfPitch);
        float halfYaw = yaw * 0.5f;
        sy = (float)Math.sin(halfYaw);
        cy = (float)Math.cos(halfYaw);
        return new Quaternionf(
            cy * sp * cr + sy * cp * sr,
            sy * cp * cr - cy * sp * sr,
            cy * cp * sr - sy * sp * cr,
            cy * cp * cr + sy * sp * sr);
    }

    public boolean isIdentity() {
        return x == 0 && y == 0 && z == 0 && w == 1;
    }

    public Quaternionf slerp(Quaternionf q, float amount) {
        float cosOmega = x * q.x + y * q.y + z * q.z + w * q.w;
        boolean flip = false;

        if (cosOmega < 0.0) {
            flip = true;
            cosOmega = -cosOmega;
        }

        float s1, s2;

        if (cosOmega > (1.0f - 1e-6)) {
            s1 = 1.0f - amount;
            s2 = (flip) ? -amount : amount;
        } else {
            float omega = (float)Math.acos(cosOmega);
            float invSinOmega = 1.0f / (float)Math.sin(omega);

            s1 = (float)Math.sin((1.0f - amount) * omega) * invSinOmega;
            s2 =
                (flip) ? -(float)Math.sin(amount * omega) * invSinOmega : (float)Math.sin(amount * omega) * invSinOmega;
        }

        return new Quaternionf(s1 * x + s2 * q.x, s1 * y + s2 * q.y, s1 * z + s2 * q.z, s1 * w + s2 * q.w);
    }

    public Quaternionf add(Quaternionf q) {
        return new Quaternionf(x + q.x, y + q.y, z + q.z, w + q.w);
    }

    public Quaternionf addInplace(Quaternionf q) {
        this.x += q.x;
        this.y += q.y;
        this.z += q.z;
        this.w += q.w;
        return this;
    }

    public Quaternionf subtract(Quaternionf q) {
        return new Quaternionf(x - q.x, y - q.y, z - q.z, w - q.w);
    }

    public Quaternionf subtractInplace(Quaternionf q) {
        this.x -= q.x;
        this.y -= q.y;
        this.z -= q.z;
        this.w -= q.w;
        return this;
    }

    public Quaternionf multiply(Quaternionf q) {
        return new Quaternionf(
            x * q.w + y * q.z - z * q.y + w * q.x,
            -x * q.z + y * q.w + z * q.x + w * q.y,
            x * q.y - y * q.x + z * q.w + w * q.z,
            -x * q.x - y * q.y - z * q.z + w * q.w);
    }

    public Quaternionf multiplyInplace(Quaternionf q) {
        float t0 = x * q.w + y * q.z - z * q.y + w * q.x;
        float t1 = -x * q.z + y * q.w + z * q.x + w * q.y;
        float t2 = x * q.y - y * q.x + z * q.w + w * q.z;
        w = -x * q.x - y * q.y - z * q.z + w * q.w;
        x = t0;
        y = t1;
        z = t2;
        return this;
    }

    public Quaternionf multiply(float s) {
        return new Quaternionf(s * x, s * y, s * z, s * w);
    }

    public Quaternionf multiplyInplace(float s) {
        w *= s;
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    public Quaternionf divide(Quaternionf q) {
        float ls = q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w;
        float invNorm = 1.0f / ls;
        float q2x = -q.x * invNorm;
        float q2y = -q.y * invNorm;
        float q2z = -q.z * invNorm;
        float q2w = q.w * invNorm;
        float cx = y * q2z - z * q2y;
        float cy = z * q2x - x * q2z;
        float cz = x * q2y - y * q2x;
        float dot = x * q2x + y * q2y + z * q2z;
        return new Quaternionf(x * q2w + q2x * w + cx, y * q2w + q2y * w + cy, z * q2w + q2z * w + cz, w * q2w - dot);
    }

    public Quaternionf divideInplace(Quaternionf q) {
        float ls = q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w;
        float invNorm = 1.0f / ls;
        float q2x = -q.x * invNorm;
        float q2y = -q.y * invNorm;
        float q2z = -q.z * invNorm;
        float q2w = q.w * invNorm;
        float cx = y * q2z - z * q2y;
        float cy = z * q2x - x * q2z;
        float cz = x * q2y - y * q2x;
        float dot = x * q2x + y * q2y + z * q2z;
        x = x * q2w + q2x * w + cx;
        y = y * q2w + q2y * w + cy;
        z = z * q2w + q2z * w + cz;
        w = w * q2w - dot;
        return this;
    }

    public Quaternionf divide(float s) {
        return new Quaternionf(x / s, y / s, z / s, w / s);
    }

    public Quaternionf divideInplace(float s) {
        w /= s;
        x /= s;
        y /= s;
        z /= s;
        return this;
    }

    public float dot(Quaternionf q) {
        return w * q.w + x * q.x + y * q.y + z * q.z;
    }

    public Quaternionf normalize() {
        float n = 1.0f / (float)Math.sqrt(x * x + y * y + z * z + w * w);
        return new Quaternionf(x * n, y * n, z * n, w * n);
    }

    public Quaternionf normalizeInplace() {
        float n = 1.0f / (float)Math.sqrt(x * x + y * y + z * z + w * w);
        x *= n;
        y *= n;
        z *= n;
        w *= n;
        return this;
    }

    public Quaternionf invert() {
        float norm = x * x + y * y + z * z + w * w;
        if (DoubleHelper.isCloseToZero(Math.abs(norm))) {
            throw new ArithmeticException("The quaternion is not invertible.");
        }

        float invNorm = 1.0f / norm;
        return new Quaternionf(-x * invNorm, -y * invNorm, -z * invNorm, w * invNorm);
    }

    public Quaternionf invertInplace() {
        float norm = x * x + y * y + z * z + w * w;
        if (DoubleHelper.isCloseToZero(Math.abs(norm))) {
            throw new ArithmeticException("The quaternion is not invertible.");
        }

        float invNorm = 1.0f / norm;
        x *= -invNorm;
        y *= -invNorm;
        z *= -invNorm;
        w *= invNorm;
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
        return Float.hashCode(x) + 31 * Float.hashCode(y) + 31 * Float.hashCode(z) + 31 * Float.hashCode(w);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Quaternionf)) {
            return false;
        }

        Quaternionf other = (Quaternionf)obj;
        return x == other.x && y == other.y && z == other.z && w == other.w;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + ", " + w + "]";
    }

}
