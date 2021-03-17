/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class Mat3f {

    public float m11, m12, m13;
    public float m21, m22, m23;
    public float m31, m32, m33;

    public Mat3f() {}

    public Mat3f(float m11, float m12, float m13, float m21, float m22, float m23, float m31, float m32, float m33) {
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    public Mat3f(Mat3f other) {
        m11 = other.m11;
        m12 = other.m12;
        m13 = other.m13;
        m21 = other.m21;
        m22 = other.m22;
        m23 = other.m23;
        m31 = other.m31;
        m32 = other.m32;
        m33 = other.m33;
    }

    public Mat3f(Mat3 other) {
        m11 = (float)other.m11;
        m12 = (float)other.m12;
        m13 = (float)other.m13;
        m21 = (float)other.m21;
        m22 = (float)other.m22;
        m23 = (float)other.m23;
        m31 = (float)other.m31;
        m32 = (float)other.m32;
        m33 = (float)other.m33;
    }

    public static Mat3f zero() {
        return new Mat3f(0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public static Mat3f identity() {
        return new Mat3f(1, 0, 0, 0, 1, 0, 0, 0, 1);
    }

    public static Mat3f fromQuaternion(Quaternionf q) {
        float xx = q.x * q.x;
        float yy = q.y * q.y;
        float zz = q.z * q.z;
        float xy = q.x * q.y;
        float wz = q.z * q.w;
        float xz = q.z * q.x;
        float wy = q.y * q.w;
        float yz = q.y * q.z;
        float wx = q.x * q.w;

        return new Mat3f(
            1.0f - 2.0f * (yy + zz),
            2.0f * (xy + wz),
            2.0f * (xz - wy),
            2.0f * (xy - wz),
            1.0f - 2.0f * (zz + xx),
            2.0f * (yz + wx),
            2.0f * (xz + wy),
            2.0f * (yz - wx),
            1.0f - 2.0f * (yy + xx));
    }

    public static Mat3f fromAxisAngle(Vec3f axis, float angle) {
        float x = axis.x, y = axis.y, z = axis.z;
        float sa = (float)Math.sin(angle), ca = (float)Math.cos(angle);
        float xx = x * x, yy = y * y, zz = z * z;
        float xy = x * y, xz = x * z, yz = y * z;

        return new Mat3f(
            xx + ca * (1.0f - xx),
            xy - ca * xy + sa * z,
            xz - ca * xz - sa * y,
            xy - ca * xy - sa * z,
            yy + ca * (1.0f - yy),
            yz - ca * yz + sa * x,
            xz - ca * xz + sa * y,
            yz - ca * yz - sa * x,
            zz + ca * (1.0f - zz));
    }

    public static Mat3f fromRotationX(float radians) {
        float c = (float)Math.cos(radians);
        float s = (float)Math.sin(radians);
        return new Mat3f(1, 0, 0, 0, c, s, 0, -s, c);
    }

    public static Mat3f fromRotationY(float radians) {
        float c = (float)Math.cos(radians);
        float s = (float)Math.sin(radians);
        return new Mat3f(c, 0, -s, 0, 1, 0, s, 0, c);
    }

    public static Mat3f fromRotationZ(float radians) {
        float c = (float)Math.cos(radians);
        float s = (float)Math.sin(radians);
        return new Mat3f(c, s, 0, -s, c, 0, 0, 0, 1);
    }

    public static Mat3f fromYawPitchRoll(float yaw, float pitch, float roll) {
        return fromQuaternion(Quaternionf.fromYawPitchRoll(yaw, pitch, roll));
    }

    public Vec3f column(int i) {
        switch (i) {
        case 0:
            return new Vec3f(m11, m21, m31);
        case 1:
            return new Vec3f(m12, m22, m32);
        case 2:
            return new Vec3f(m13, m23, m33);
        default:
            throw new IndexOutOfBoundsException("Invalid column: " + i);
        }
    }

    public Vec3f row(int i) {
        switch (i) {
        case 0:
            return new Vec3f(m11, m12, m13);
        case 1:
            return new Vec3f(m21, m22, m23);
        case 2:
            return new Vec3f(m31, m32, m33);
        default:
            throw new IndexOutOfBoundsException("Invalid row: " + i);
        }
    }

    public double trace() {
        return m11 + m22 + m33;
    }

    public boolean isIdentity() {
        return (m11 == 1 && m12 == 0 && m13 == 0)
            && (m21 == 0 && m22 == 1 && m23 == 0)
            && (m31 == 0 && m32 == 0 && m33 == 1);
    }

    public Mat3f multiply(Mat3f m) {
        return new Mat3f(
            m11 * m.m11 + m12 * m.m21 + m13 * m.m31,
            m11 * m.m12 + m12 * m.m22 + m13 * m.m32,
            m11 * m.m13 + m12 * m.m23 + m13 * m.m33,
            m21 * m.m11 + m22 * m.m21 + m23 * m.m31,
            m21 * m.m12 + m22 * m.m22 + m23 * m.m32,
            m21 * m.m13 + m22 * m.m23 + m23 * m.m33,
            m31 * m.m11 + m32 * m.m21 + m33 * m.m31,
            m31 * m.m12 + m32 * m.m22 + m33 * m.m32,
            m31 * m.m13 + m32 * m.m23 + m33 * m.m33);
    }

    public Mat3f multiplyInplace(Mat3f m) {
        float t00 = m11 * m.m11 + m12 * m.m21 + m13 * m.m31;
        float t01 = m11 * m.m12 + m12 * m.m22 + m13 * m.m32;
        float t02 = m11 * m.m13 + m12 * m.m23 + m13 * m.m33;
        float t10 = m21 * m.m11 + m22 * m.m21 + m23 * m.m31;
        float t11 = m21 * m.m12 + m22 * m.m22 + m23 * m.m32;
        float t12 = m21 * m.m13 + m22 * m.m23 + m23 * m.m33;
        float t20 = m31 * m.m11 + m32 * m.m21 + m33 * m.m31;
        float t21 = m31 * m.m12 + m32 * m.m22 + m33 * m.m32;
        float t22 = m31 * m.m13 + m32 * m.m23 + m33 * m.m33;
        m11 = t00;
        m12 = t01;
        m13 = t02;
        m21 = t10;
        m22 = t11;
        m23 = t12;
        m31 = t20;
        m32 = t21;
        m33 = t22;
        return this;
    }

    public Mat3f multiply(float scalar) {
        return new Mat3f(
            m11 * scalar,
            m12 * scalar,
            m13 * scalar,
            m21 * scalar,
            m22 * scalar,
            m23 * scalar,
            m31 * scalar,
            m32 * scalar,
            m33 * scalar);
    }

    public Mat3f multiplyInplace(float s) {
        m11 *= s;
        m12 *= s;
        m13 *= s;
        m21 *= s;
        m22 *= s;
        m23 *= s;
        m31 *= s;
        m32 *= s;
        m33 *= s;
        return this;
    }

    public Mat3f transpose() {
        return new Mat3f(this).transposeInplace();
    }

    public Mat3f transposeInplace() {
        float t = m12;
        m12 = m21;
        m21 = t;
        t = m13;
        m13 = m31;
        m31 = t;
        t = m23;
        m23 = m32;
        m32 = t;
        return this;
    }

    public Mat3f invert() {
        return new Mat3f(this).invertInplace();
    }

    public Mat3f invertInplace() {
        float det = det();
        if (DoubleHelper.isCloseToZero(Math.abs(det))) {
            throw new ArithmeticException("The matrix is not invertible.");
        }

        float f00 = m22 * m33 - m23 * m32;
        float f01 = m13 * m32 - m12 * m33;
        float f02 = m12 * m23 - m13 * m22;
        float f10 = m23 * m31 - m21 * m33;
        float f11 = m11 * m33 - m13 * m31;
        float f12 = m13 * m21 - m11 * m23;
        float f20 = m21 * m32 - m22 * m31;
        float f21 = m12 * m31 - m11 * m32;
        float f22 = m11 * m22 - m12 * m21;
        m11 = f00;
        m12 = f01;
        m13 = f02;
        m21 = f10;
        m22 = f11;
        m23 = f12;
        m31 = f20;
        m32 = f21;
        m33 = f22;

        return multiplyInplace(1.0f / det);
    }

    public Mat3f adjugate() {
        return new Mat3f(this).adjugateInplace();
    }

    public Mat3f adjugateInplace() {
        float t00 = m22 * m33 - m23 * m32;
        float t01 = m13 * m32 - m12 * m33;
        float t02 = m12 * m23 - m13 * m22;
        float t10 = m23 * m31 - m21 * m33;
        float t11 = m11 * m33 - m13 * m31;
        float t12 = m13 * m21 - m11 * m23;
        float t20 = m21 * m32 - m22 * m31;
        float t21 = m12 * m31 - m11 * m32;
        float t22 = m11 * m22 - m12 * m21;
        m11 = t00;
        m12 = t01;
        m13 = t02;
        m21 = t10;
        m22 = t11;
        m23 = t12;
        m31 = t20;
        m32 = t21;
        m33 = t22;
        return this;
    }

    public float det() {
        float t0 = m22 * m33 - m23 * m32;
        float t1 = m23 * m31 - m21 * m33;
        float t2 = m21 * m32 - m22 * m31;
        return m11 * t0 + m12 * t1 + m13 * t2;
    }

    @Override
    public String toString() {
        return "[[" + m11 + ", " + m12 + ", " + m13 + "] [" + m21 + ", " + m22 + ", " + m23 + "] [" + m31 + ", " + m32
            + ", " + m33 + "]]";
    }

    @Override
    public int hashCode() {
        int hash = 37;
        hash = 37 * hash + Float.floatToIntBits(m11);
        hash = 37 * hash + Float.floatToIntBits(m12);
        hash = 37 * hash + Float.floatToIntBits(m13);
        hash = 37 * hash + Float.floatToIntBits(m21);
        hash = 37 * hash + Float.floatToIntBits(m22);
        hash = 37 * hash + Float.floatToIntBits(m23);
        hash = 37 * hash + Float.floatToIntBits(m31);
        hash = 37 * hash + Float.floatToIntBits(m32);
        hash = 37 * hash + Float.floatToIntBits(m33);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Mat3f)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        Mat3f other = (Mat3f)obj;
        return m11 == other.m11
            && m12 == other.m12
            && m12 == other.m13
            && m21 == other.m21
            && m22 == other.m22
            && m23 == other.m23
            && m31 == other.m31
            && m32 == other.m32
            && m33 == other.m33;
    }

}
