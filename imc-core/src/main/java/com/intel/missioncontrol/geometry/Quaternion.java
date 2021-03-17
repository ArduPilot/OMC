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
public final class Quaternion implements PrimitiveSerializable, BinarySerializable {

    public double x, y, z, w;

    public Quaternion() {
        x = 0;
        y = 0;
        z = 0;
        w = 1;
    }

    public Quaternion(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quaternion(Quaternion q) {
        this.x = q.x;
        this.y = q.y;
        this.z = q.z;
        this.w = q.w;
    }

    public Quaternion(PrimitiveDeserializationContext context) {
        String[] coords = context.read().split(",");
        x = Double.parseDouble(coords[0]);
        y = Double.parseDouble(coords[1]);
        z = Double.parseDouble(coords[2]);
        w = Double.parseDouble(coords[3]);
    }

    public Quaternion(BinaryDeserializationContext context) {
        x = context.readDouble();
        y = context.readDouble();
        z = context.readDouble();
        w = context.readDouble();
    }

    public static Quaternion fromRotationMatrix(Mat3 matrix) {
        return fromRotationMatrix(
            matrix.m11, matrix.m12, matrix.m13, matrix.m21, matrix.m22, matrix.m23, matrix.m31, matrix.m32, matrix.m33);
    }

    public static Quaternion fromRotationMatrix(Mat4 matrix) {
        return fromRotationMatrix(
            matrix.m11, matrix.m12, matrix.m13, matrix.m21, matrix.m22, matrix.m23, matrix.m31, matrix.m32, matrix.m33);
    }

    private static Quaternion fromRotationMatrix(
            double m11,
            double m12,
            double m13,
            double m21,
            double m22,
            double m23,
            double m31,
            double m32,
            double m33) {
        double trace = m11 + m22 + m33;

        if (trace > 0.0) {
            double s = Math.sqrt(trace + 1.0);
            double w = s * 0.5;
            s = 0.5 / s;
            return new Quaternion((m23 - m32) * s, (m31 - m13) * s, (m12 - m21) * s, w);
        }

        if (m11 >= m22 && m11 >= m33) {
            double s = Math.sqrt(1.0 + m11 - m22 - m33);
            double invS = 0.5 / s;
            return new Quaternion(0.5 * s, (m12 + m21) * invS, (m13 + m31) * invS, (m23 - m32) * invS);
        } else if (m22 > m33) {
            double s = Math.sqrt(1.0 + m22 - m11 - m33);
            double invS = 0.5 / s;
            return new Quaternion((m21 + m12) * invS, 0.5 * s, (m32 + m23) * invS, (m31 - m13) * invS);
        }

        double s = Math.sqrt(1.0 + m33 - m11 - m22);
        double invS = 0.5 / s;
        return new Quaternion((m31 + m13) * invS, (m32 + m23) * invS, 0.5 * s, (m12 - m21) * invS);
    }

    public static Quaternion fromAxisAngle(Vec3 axis, double angle) {
        double halfAngle = angle * 0.5;
        double s = Math.sin(halfAngle);
        double c = Math.cos(halfAngle);
        return new Quaternion(axis.x * s, axis.y * s, axis.z * s, c);
    }

    public static Quaternion fromYawPitchRoll(double yaw, double pitch, double roll) {
        double sr, cr, sp, cp, sy, cy;
        double halfRoll = roll * 0.5;
        sr = Math.sin(halfRoll);
        cr = Math.cos(halfRoll);
        double halfPitch = pitch * 0.5;
        sp = Math.sin(halfPitch);
        cp = Math.cos(halfPitch);
        double halfYaw = yaw * 0.5;
        sy = Math.sin(halfYaw);
        cy = Math.cos(halfYaw);
        return new Quaternion(
            cy * sp * cr + sy * cp * sr,
            sy * cp * cr - cy * sp * sr,
            cy * cp * sr - sy * sp * cr,
            cy * cp * cr + sy * sp * sr);
    }

    public boolean isIdentity() {
        return x == 0 && y == 0 && z == 0 && w == 1;
    }

    public Quaternion slerp(Quaternion q, double amount) {
        double cosOmega = x * q.x + y * q.y + z * q.z + w * q.w;
        boolean flip = false;

        if (cosOmega < 0.0) {
            flip = true;
            cosOmega = -cosOmega;
        }

        double s1, s2;

        if (cosOmega > (1.0 - 1e-6)) {
            s1 = 1.0 - amount;
            s2 = (flip) ? -amount : amount;
        } else {
            double omega = Math.acos(cosOmega);
            double invSinOmega = 1.0 / Math.sin(omega);

            s1 = Math.sin((1.0 - amount) * omega) * invSinOmega;
            s2 = (flip) ? -Math.sin(amount * omega) * invSinOmega : Math.sin(amount * omega) * invSinOmega;
        }

        return new Quaternion(s1 * x + s2 * q.x, s1 * y + s2 * q.y, s1 * z + s2 * q.z, s1 * w + s2 * q.w);
    }

    public Quaternion add(Quaternion q) {
        return new Quaternion(x + q.x, y + q.y, z + q.z, w + q.w);
    }

    public Quaternion addInplace(Quaternion q) {
        this.x += q.x;
        this.y += q.y;
        this.z += q.z;
        this.w += q.w;
        return this;
    }

    public Quaternion subtract(Quaternion q) {
        return new Quaternion(x - q.x, y - q.y, z - q.z, w - q.w);
    }

    public Quaternion subtractInplace(Quaternion q) {
        this.x -= q.x;
        this.y -= q.y;
        this.z -= q.z;
        this.w -= q.w;
        return this;
    }

    public Quaternion multiply(Quaternion q) {
        return new Quaternion(
            x * q.w + y * q.z - z * q.y + w * q.x,
            -x * q.z + y * q.w + z * q.x + w * q.y,
            x * q.y - y * q.x + z * q.w + w * q.z,
            -x * q.x - y * q.y - z * q.z + w * q.w);
    }

    public Quaternion multiplyInplace(Quaternion q) {
        double t0 = x * q.w + y * q.z - z * q.y + w * q.x;
        double t1 = -x * q.z + y * q.w + z * q.x + w * q.y;
        double t2 = x * q.y - y * q.x + z * q.w + w * q.z;
        w = -x * q.x - y * q.y - z * q.z + w * q.w;
        x = t0;
        y = t1;
        z = t2;
        return this;
    }

    public Quaternion multiply(double s) {
        return new Quaternion(s * x, s * y, s * z, s * w);
    }

    public Quaternion multiplyInplace(double s) {
        w *= s;
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    public Quaternion divide(Quaternion q) {
        double ls = q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w;
        double invNorm = 1.0 / ls;
        double q2x = -q.x * invNorm;
        double q2y = -q.y * invNorm;
        double q2z = -q.z * invNorm;
        double q2w = q.w * invNorm;
        double cx = y * q2z - z * q2y;
        double cy = z * q2x - x * q2z;
        double cz = x * q2y - y * q2x;
        double dot = x * q2x + y * q2y + z * q2z;
        return new Quaternion(x * q2w + q2x * w + cx, y * q2w + q2y * w + cy, z * q2w + q2z * w + cz, w * q2w - dot);
    }

    public Quaternion divideInplace(Quaternion q) {
        double ls = q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w;
        double invNorm = 1.0 / ls;
        double q2x = -q.x * invNorm;
        double q2y = -q.y * invNorm;
        double q2z = -q.z * invNorm;
        double q2w = q.w * invNorm;
        double cx = y * q2z - z * q2y;
        double cy = z * q2x - x * q2z;
        double cz = x * q2y - y * q2x;
        double dot = x * q2x + y * q2y + z * q2z;
        x = x * q2w + q2x * w + cx;
        y = y * q2w + q2y * w + cy;
        z = z * q2w + q2z * w + cz;
        w = w * q2w - dot;
        return this;
    }

    public Quaternion divide(double s) {
        return new Quaternion(x / s, y / s, z / s, w / s);
    }

    public Quaternion divideInplace(double s) {
        w /= s;
        x /= s;
        y /= s;
        z /= s;
        return this;
    }

    public double dot(Quaternion q) {
        return w * q.w + x * q.x + y * q.y + z * q.z;
    }

    public Quaternion normalize() {
        double n = 1.0 / Math.sqrt(x * x + y * y + z * z + w * w);
        return new Quaternion(x * n, y * n, z * n, w * n);
    }

    public Quaternion normalizeInplace() {
        double n = 1.0 / Math.sqrt(x * x + y * y + z * z + w * w);
        x *= n;
        y *= n;
        z *= n;
        w *= n;
        return this;
    }

    public Quaternion invert() {
        double norm = x * x + y * y + z * z + w * w;
        if (DoubleHelper.isCloseToZero(Math.abs(norm))) {
            throw new ArithmeticException("The quaternion is not invertible.");
        }

        double invNorm = 1.0 / norm;
        return new Quaternion(-x * invNorm, -y * invNorm, -z * invNorm, w * invNorm);
    }

    public Quaternion invertInplace() {
        double norm = x * x + y * y + z * z + w * w;
        if (DoubleHelper.isCloseToZero(Math.abs(norm))) {
            throw new ArithmeticException("The quaternion is not invertible.");
        }

        double invNorm = 1.0 / norm;
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
        return Double.hashCode(x) + 31 * Double.hashCode(y) + 31 * Double.hashCode(z) + 31 * Double.hashCode(w);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Quaternion)) {
            return false;
        }

        Quaternion other = (Quaternion)obj;
        return x == other.x && y == other.y && z == other.z && w == other.w;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + ", " + w + "]";
    }

}
