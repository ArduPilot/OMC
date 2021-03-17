/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class Mat4f {

    public float m11, m12, m13, m14;
    public float m21, m22, m23, m24;
    public float m31, m32, m33, m34;
    public float m41, m42, m43, m44;

    public Mat4f() {}

    public Mat4f(
            float m11,
            float m12,
            float m13,
            float m14,
            float m21,
            float m22,
            float m23,
            float m24,
            float m31,
            float m32,
            float m33,
            float m34,
            float m41,
            float m42,
            float m43,
            float m44) {
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m14 = m14;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m24 = m24;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
        this.m34 = m34;
        this.m41 = m41;
        this.m42 = m42;
        this.m43 = m43;
        this.m44 = m44;
    }

    public Mat4f(Mat4 other) {
        this.m11 = (float)other.m11;
        this.m12 = (float)other.m12;
        this.m13 = (float)other.m13;
        this.m14 = (float)other.m14;
        this.m21 = (float)other.m21;
        this.m22 = (float)other.m22;
        this.m23 = (float)other.m23;
        this.m24 = (float)other.m24;
        this.m31 = (float)other.m31;
        this.m32 = (float)other.m32;
        this.m33 = (float)other.m33;
        this.m34 = (float)other.m34;
        this.m41 = (float)other.m41;
        this.m42 = (float)other.m42;
        this.m43 = (float)other.m43;
        this.m44 = (float)other.m44;
    }

    public Mat4f(Mat4f other) {
        this.m11 = other.m11;
        this.m12 = other.m12;
        this.m13 = other.m13;
        this.m14 = other.m14;
        this.m21 = other.m21;
        this.m22 = other.m22;
        this.m23 = other.m23;
        this.m24 = other.m24;
        this.m31 = other.m31;
        this.m32 = other.m32;
        this.m33 = other.m33;
        this.m34 = other.m34;
        this.m41 = other.m41;
        this.m42 = other.m42;
        this.m43 = other.m43;
        this.m44 = other.m44;
    }

    public static Mat4f zero() {
        return new Mat4f(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public static Mat4f identity() {
        return new Mat4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
    }

    public static Mat4f fromAxisAngle(Vec3f axis, float angle) {
        float x = axis.x, y = axis.y, z = axis.z;
        float sa = (float)Math.sin(angle), ca = (float)Math.cos(angle);
        float xx = x * x, yy = y * y, zz = z * z;
        float xy = x * y, xz = x * z, yz = y * z;

        return new Mat4f(
            xx + ca * (1.0f - xx),
            xy - ca * xy + sa * z,
            xz - ca * xz - sa * y,
            0,
            xy - ca * xy - sa * z,
            yy + ca * (1.0f - yy),
            yz - ca * yz + sa * x,
            0,
            xz - ca * xz + sa * y,
            yz - ca * yz - sa * x,
            zz + ca * (1.0f - zz),
            0,
            0,
            0,
            0,
            1);
    }

    public static Mat4f fromQuaternion(Quaternionf q) {
        float xx = q.x * q.x;
        float yy = q.y * q.y;
        float zz = q.z * q.z;
        float xy = q.x * q.y;
        float wz = q.z * q.w;
        float xz = q.z * q.x;
        float wy = q.y * q.w;
        float yz = q.y * q.z;
        float wx = q.x * q.w;

        return new Mat4f(
            1.0f - 2.0f * (yy + zz),
            2.0f * (xy + wz),
            2.0f * (xz - wy),
            0,
            2.0f * (xy - wz),
            1.0f - 2.0f * (zz + xx),
            2.0f * (yz + wx),
            0,
            2.0f * (xz + wy),
            2.0f * (yz - wx),
            1.0f - 2.0f * (yy + xx),
            0,
            0,
            0,
            0,
            1);
    }

    public static Mat4f fromTranslation(Vec3f v) {
        return new Mat4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, v.x, v.y, v.z, 1);
    }

    public static Mat4f fromTranslation(float x, float y, float z) {
        return new Mat4f(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, x, y, z, 1);
    }

    public static Mat4f fromScale(Vec3f scale) {
        return fromScale(scale.x, scale.y, scale.z);
    }

    public static Mat4f fromScale(float scale) {
        return fromScale(scale, scale, scale);
    }

    public static Mat4f fromScale(float x, float y, float z) {
        return new Mat4f(x, 0, 0, 0, 0, y, 0, 0, 0, 0, z, 0, 0, 0, 0, 1);
    }

    public static Mat4f fromScale(Vec3f scale, Vec3f center) {
        return fromScale(scale.x, scale.y, scale.z, center);
    }

    public static Mat4f fromScale(float scale, Vec3f center) {
        return fromScale(scale, scale, scale, center);
    }

    public static Mat4f fromScale(float x, float y, float z, Vec3f center) {
        float tx = center.x * (1 - x);
        float ty = center.y * (1 - y);
        float tz = center.z * (1 - z);
        return new Mat4f(x, 0, 0, 0, 0, y, 0, 0, 0, 0, z, 0, tx, ty, tz, 1);
    }

    public static Mat4f fromRotationX(float radians) {
        float c = (float)Math.cos(radians);
        float s = (float)Math.sin(radians);
        return new Mat4f(1, 0, 0, 0, 0, c, s, 0, 0, -s, c, 0, 0, 0, 0, 1);
    }

    public static Mat4f fromRotationX(float radians, Vec3f center) {
        float c = (float)Math.cos(radians);
        float s = (float)Math.sin(radians);
        float y = center.y * (1 - c) + center.z * s;
        float z = center.z * (1 - c) - center.y * s;
        return new Mat4f(1, 0, 0, 0, 0, c, s, 0, 0, -s, c, 0, 0, y, z, 1);
    }

    public static Mat4f fromRotationY(float radians) {
        float c = (float)Math.cos(radians);
        float s = (float)Math.sin(radians);
        return new Mat4f(c, 0, -s, 0, 0, 1, 0, 0, s, 0, c, 0, 0, 0, 0, 1);
    }

    public static Mat4f fromRotationY(float radians, Vec3f center) {
        float c = (float)Math.cos(radians);
        float s = (float)Math.sin(radians);
        float x = center.x * (1 - c) - center.z * s;
        float z = center.z * (1 - c) + center.x * s;
        return new Mat4f(c, 0, -s, 0, 0, 1, 0, 0, s, 0, c, 0, x, 0, z, 1);
    }

    public static Mat4f fromRotationZ(float radians) {
        float c = (float)Math.cos(radians);
        float s = (float)Math.sin(radians);
        return new Mat4f(c, s, 0, 0, -s, c, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1);
    }

    public static Mat4f fromRotationZ(float radians, Vec3f center) {
        float c = (float)Math.cos(radians);
        float s = (float)Math.sin(radians);
        float x = center.x * (1 - c) + center.y * s;
        float y = center.y * (1 - c) - center.x * s;
        return new Mat4f(c, s, 0, 0, -s, c, 0, 0, 0, 0, 1, 0, x, y, 0, 1);
    }

    public static Mat4f fromYawPitchRoll(float yaw, float pitch, float roll) {
        return fromQuaternion(Quaternionf.fromYawPitchRoll(yaw, pitch, roll));
    }

    public Vec4f column(int i) {
        switch (i) {
        case 0:
            return new Vec4f(m11, m21, m31, m41);
        case 1:
            return new Vec4f(m12, m22, m32, m42);
        case 2:
            return new Vec4f(m13, m23, m33, m43);
        case 3:
            return new Vec4f(m14, m24, m34, m44);
        default:
            throw new IndexOutOfBoundsException("Invalid column: " + i);
        }
    }

    public Vec4f row(int i) {
        switch (i) {
        case 0:
            return new Vec4f(m11, m12, m13, m14);
        case 1:
            return new Vec4f(m21, m22, m23, m24);
        case 2:
            return new Vec4f(m31, m32, m33, m34);
        case 3:
            return new Vec4f(m41, m42, m43, m44);
        default:
            throw new IndexOutOfBoundsException("Invalid row: " + i);
        }
    }

    public Mat4f transpose() {
        return new Mat4f(this).transposeInplace();
    }

    public Mat4f transposeInplace() {
        float t = m12;
        m12 = m21;
        m21 = t;
        t = m13;
        m13 = m31;
        m31 = t;
        t = m14;
        m14 = m41;
        m41 = t;
        t = m23;
        m23 = m32;
        m32 = t;
        t = m24;
        m24 = m42;
        m42 = t;
        t = m34;
        m34 = m43;
        m43 = t;
        return this;
    }

    public boolean isIdentity() {
        return (m11 == 1 && m12 == 0 && m13 == 0 && m14 == 0)
            && (m21 == 0 && m22 == 1 && m23 == 0 && m24 == 0)
            && (m31 == 0 && m32 == 0 && m33 == 1 && m34 == 0)
            && (m41 == 0 && m42 == 0 && m43 == 0 && m44 == 1);
    }

    public Mat4f multiply(float s) {
        return new Mat4f(
            m11 * s, m12 * s, m13 * s, m14 * s, m21 * s, m22 * s, m23 * s, m24 * s, m31 * s, m32 * s, m33 * s, m34 * s,
            m41 * s, m42 * s, m43 * s, m44 * s);
    }

    public Mat4f multiplyInplace(float s) {
        m11 *= s;
        m12 *= s;
        m13 *= s;
        m14 *= s;
        m21 *= s;
        m22 *= s;
        m23 *= s;
        m24 *= s;
        m31 *= s;
        m32 *= s;
        m33 *= s;
        m34 *= s;
        m41 *= s;
        m42 *= s;
        m43 *= s;
        m44 *= s;
        return this;
    }

    public Mat4f multiply(Mat4f m) {
        return new Mat4f(
            m11 * m.m11 + m12 * m.m21 + m13 * m.m31 + m14 * m.m41,
            m11 * m.m12 + m12 * m.m22 + m13 * m.m32 + m14 * m.m42,
            m11 * m.m13 + m12 * m.m23 + m13 * m.m33 + m14 * m.m43,
            m11 * m.m14 + m12 * m.m24 + m13 * m.m34 + m14 * m.m44,
            m21 * m.m11 + m22 * m.m21 + m23 * m.m31 + m24 * m.m41,
            m21 * m.m12 + m22 * m.m22 + m23 * m.m32 + m24 * m.m42,
            m21 * m.m13 + m22 * m.m23 + m23 * m.m33 + m24 * m.m43,
            m21 * m.m14 + m22 * m.m24 + m23 * m.m34 + m24 * m.m44,
            m31 * m.m11 + m32 * m.m21 + m33 * m.m31 + m34 * m.m41,
            m31 * m.m12 + m32 * m.m22 + m33 * m.m32 + m34 * m.m42,
            m31 * m.m13 + m32 * m.m23 + m33 * m.m33 + m34 * m.m43,
            m31 * m.m14 + m32 * m.m24 + m33 * m.m34 + m34 * m.m44,
            m41 * m.m11 + m42 * m.m21 + m43 * m.m31 + m44 * m.m41,
            m41 * m.m12 + m42 * m.m22 + m43 * m.m32 + m44 * m.m42,
            m41 * m.m13 + m42 * m.m23 + m43 * m.m33 + m44 * m.m43,
            m41 * m.m14 + m42 * m.m24 + m43 * m.m34 + m44 * m.m44);
    }

    public Mat4f multiplyInplace(Mat4f m) {
        float t00 = m11 * m.m11 + m12 * m.m21 + m13 * m.m31 + m14 * m.m41;
        float t01 = m11 * m.m12 + m12 * m.m22 + m13 * m.m32 + m14 * m.m42;
        float t02 = m11 * m.m13 + m12 * m.m23 + m13 * m.m33 + m14 * m.m43;
        float t03 = m11 * m.m14 + m12 * m.m24 + m13 * m.m34 + m14 * m.m44;
        float t10 = m21 * m.m11 + m22 * m.m21 + m23 * m.m31 + m24 * m.m41;
        float t11 = m21 * m.m12 + m22 * m.m22 + m23 * m.m32 + m24 * m.m42;
        float t12 = m21 * m.m13 + m22 * m.m23 + m23 * m.m33 + m24 * m.m43;
        float t13 = m21 * m.m14 + m22 * m.m24 + m23 * m.m34 + m24 * m.m44;
        float t20 = m31 * m.m11 + m32 * m.m21 + m33 * m.m31 + m34 * m.m41;
        float t21 = m31 * m.m12 + m32 * m.m22 + m33 * m.m32 + m34 * m.m42;
        float t22 = m31 * m.m13 + m32 * m.m23 + m33 * m.m33 + m34 * m.m43;
        float t23 = m31 * m.m14 + m32 * m.m24 + m33 * m.m34 + m34 * m.m44;
        float t30 = m41 * m.m11 + m42 * m.m21 + m43 * m.m31 + m44 * m.m41;
        float t31 = m41 * m.m12 + m42 * m.m22 + m43 * m.m32 + m44 * m.m42;
        float t32 = m41 * m.m13 + m42 * m.m23 + m43 * m.m33 + m44 * m.m43;
        float t33 = m41 * m.m14 + m42 * m.m24 + m43 * m.m34 + m44 * m.m44;
        m11 = t00;
        m12 = t01;
        m13 = t02;
        m14 = t03;
        m21 = t10;
        m22 = t11;
        m23 = t12;
        m24 = t13;
        m31 = t20;
        m32 = t21;
        m33 = t22;
        m34 = t23;
        m41 = t30;
        m42 = t31;
        m43 = t32;
        m44 = t33;
        return this;
    }

    public Mat4f invert() {
        Mat4f res = new Mat4f();

        float a0 = m11 * m22 - m12 * m21;
        float a1 = m11 * m23 - m13 * m21;
        float a2 = m11 * m24 - m14 * m21;
        float a3 = m12 * m23 - m13 * m22;
        float a4 = m12 * m24 - m14 * m22;
        float a5 = m13 * m24 - m14 * m23;
        float b0 = m31 * m42 - m32 * m41;
        float b1 = m31 * m43 - m33 * m41;
        float b2 = m31 * m44 - m34 * m41;
        float b3 = m32 * m43 - m33 * m42;
        float b4 = m32 * m44 - m34 * m42;
        float b5 = m33 * m44 - m34 * m43;
        float det = a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0;

        if (DoubleHelper.isCloseToZero(Math.abs(det))) {
            throw new ArithmeticException("The matrix is not invertible.");
        }

        res.m11 = +m22 * b5 - m23 * b4 + m24 * b3;
        res.m21 = -m21 * b5 + m23 * b2 - m24 * b1;
        res.m31 = +m21 * b4 - m22 * b2 + m24 * b0;
        res.m41 = -m21 * b3 + m22 * b1 - m23 * b0;
        res.m12 = -m12 * b5 + m13 * b4 - m14 * b3;
        res.m22 = +m11 * b5 - m13 * b2 + m14 * b1;
        res.m32 = -m11 * b4 + m12 * b2 - m14 * b0;
        res.m42 = +m11 * b3 - m12 * b1 + m13 * b0;
        res.m13 = +m42 * a5 - m43 * a4 + m44 * a3;
        res.m23 = -m41 * a5 + m43 * a2 - m44 * a1;
        res.m33 = +m41 * a4 - m42 * a2 + m44 * a0;
        res.m43 = -m41 * a3 + m42 * a1 - m43 * a0;
        res.m14 = -m32 * a5 + m33 * a4 - m34 * a3;
        res.m24 = +m31 * a5 - m33 * a2 + m34 * a1;
        res.m34 = -m31 * a4 + m32 * a2 - m34 * a0;
        res.m44 = +m31 * a3 - m32 * a1 + m33 * a0;
        res.multiplyInplace(1.0f / det);

        return res;
    }

    public Mat4f invertInplace() {
        float a0 = m11 * m22 - m12 * m21;
        float a1 = m11 * m23 - m13 * m21;
        float a2 = m11 * m24 - m14 * m21;
        float a3 = m12 * m23 - m13 * m22;
        float a4 = m12 * m24 - m14 * m22;
        float a5 = m13 * m24 - m14 * m23;
        float b0 = m31 * m42 - m32 * m41;
        float b1 = m31 * m43 - m33 * m41;
        float b2 = m31 * m44 - m34 * m41;
        float b3 = m32 * m43 - m33 * m42;
        float b4 = m32 * m44 - m34 * m42;
        float b5 = m33 * m44 - m34 * m43;
        float det = a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0;

        if (DoubleHelper.isCloseToZero(Math.abs(det))) {
            throw new ArithmeticException("The matrix is not invertible.");
        }

        float t00 = +m22 * b5 - m23 * b4 + m24 * b3;
        float t10 = -m21 * b5 + m23 * b2 - m24 * b1;
        float t20 = +m21 * b4 - m22 * b2 + m24 * b0;
        float t30 = -m21 * b3 + m22 * b1 - m23 * b0;
        float t01 = -m12 * b5 + m13 * b4 - m14 * b3;
        float t11 = +m11 * b5 - m13 * b2 + m14 * b1;
        float t21 = -m11 * b4 + m12 * b2 - m14 * b0;
        float t31 = +m11 * b3 - m12 * b1 + m13 * b0;
        float t02 = +m42 * a5 - m43 * a4 + m44 * a3;
        float t12 = -m41 * a5 + m43 * a2 - m44 * a1;
        float t22 = +m41 * a4 - m42 * a2 + m44 * a0;
        float t32 = -m41 * a3 + m42 * a1 - m43 * a0;
        float t03 = -m32 * a5 + m33 * a4 - m34 * a3;
        float t13 = +m31 * a5 - m33 * a2 + m34 * a1;
        float t23 = -m31 * a4 + m32 * a2 - m34 * a0;
        float t33 = +m31 * a3 - m32 * a1 + m33 * a0;

        m11 = t00;
        m12 = t01;
        m13 = t02;
        m14 = t03;
        m21 = t10;
        m22 = t11;
        m23 = t12;
        m24 = t13;
        m31 = t20;
        m32 = t21;
        m33 = t22;
        m34 = t23;
        m41 = t30;
        m42 = t31;
        m43 = t32;
        m44 = t33;

        return multiplyInplace(1.0f / det);
    }

    public Mat4f adjugate() {
        Mat4f res = new Mat4f();
        float a0 = m11 * m22 - m12 * m21;
        float a1 = m11 * m23 - m13 * m21;
        float a2 = m11 * m24 - m14 * m21;
        float a3 = m12 * m23 - m13 * m22;
        float a4 = m12 * m24 - m14 * m22;
        float a5 = m13 * m24 - m14 * m23;
        float b0 = m31 * m42 - m32 * m41;
        float b1 = m31 * m43 - m33 * m41;
        float b2 = m31 * m44 - m34 * m41;
        float b3 = m32 * m43 - m33 * m42;
        float b4 = m32 * m44 - m34 * m42;
        float b5 = m33 * m44 - m34 * m43;
        res.m11 = +m22 * b5 - m23 * b4 + m24 * b3;
        res.m21 = -m21 * b5 + m23 * b2 - m24 * b1;
        res.m31 = +m21 * b4 - m22 * b2 + m24 * b0;
        res.m41 = -m21 * b3 + m22 * b1 - m23 * b0;
        res.m12 = -m12 * b5 + m13 * b4 - m14 * b3;
        res.m22 = +m11 * b5 - m13 * b2 + m14 * b1;
        res.m32 = -m11 * b4 + m12 * b2 - m14 * b0;
        res.m42 = +m11 * b3 - m12 * b1 + m13 * b0;
        res.m13 = +m42 * a5 - m43 * a4 + m44 * a3;
        res.m23 = -m41 * a5 + m43 * a2 - m44 * a1;
        res.m33 = +m41 * a4 - m42 * a2 + m44 * a0;
        res.m43 = -m41 * a3 + m42 * a1 - m43 * a0;
        res.m14 = -m32 * a5 + m33 * a4 - m34 * a3;
        res.m24 = +m31 * a5 - m33 * a2 + m34 * a1;
        res.m34 = -m31 * a4 + m32 * a2 - m34 * a0;
        res.m44 = +m31 * a3 - m32 * a1 + m33 * a0;
        return res;
    }

    public Mat4f adjugateInplace() {
        float a0 = m11 * m22 - m12 * m21;
        float a1 = m11 * m23 - m13 * m21;
        float a2 = m11 * m24 - m14 * m21;
        float a3 = m12 * m23 - m13 * m22;
        float a4 = m12 * m24 - m14 * m22;
        float a5 = m13 * m24 - m14 * m23;
        float b0 = m31 * m42 - m32 * m41;
        float b1 = m31 * m43 - m33 * m41;
        float b2 = m31 * m44 - m34 * m41;
        float b3 = m32 * m43 - m33 * m42;
        float b4 = m32 * m44 - m34 * m42;
        float b5 = m33 * m44 - m34 * m43;
        float t00 = +m22 * b5 - m23 * b4 + m24 * b3;
        float t10 = -m21 * b5 + m23 * b2 - m24 * b1;
        float t20 = +m21 * b4 - m22 * b2 + m24 * b0;
        float t30 = -m21 * b3 + m22 * b1 - m23 * b0;
        float t01 = -m12 * b5 + m13 * b4 - m14 * b3;
        float t11 = +m11 * b5 - m13 * b2 + m14 * b1;
        float t21 = -m11 * b4 + m12 * b2 - m14 * b0;
        float t31 = +m11 * b3 - m12 * b1 + m13 * b0;
        float t02 = +m42 * a5 - m43 * a4 + m44 * a3;
        float t12 = -m41 * a5 + m43 * a2 - m44 * a1;
        float t22 = +m41 * a4 - m42 * a2 + m44 * a0;
        float t32 = -m41 * a3 + m42 * a1 - m43 * a0;
        float t03 = -m32 * a5 + m33 * a4 - m34 * a3;
        float t13 = +m31 * a5 - m33 * a2 + m34 * a1;
        float t23 = -m31 * a4 + m32 * a2 - m34 * a0;
        float t33 = +m31 * a3 - m32 * a1 + m33 * a0;
        m11 = t00;
        m12 = t01;
        m13 = t02;
        m14 = t03;
        m21 = t10;
        m22 = t11;
        m23 = t12;
        m24 = t13;
        m31 = t20;
        m32 = t21;
        m33 = t22;
        m34 = t23;
        m41 = t30;
        m42 = t31;
        m43 = t32;
        m44 = t33;

        return this;
    }

    public float det() {
        float a0 = m11 * m22 - m12 * m21;
        float a1 = m11 * m23 - m13 * m21;
        float a2 = m11 * m24 - m14 * m21;
        float a3 = m12 * m23 - m13 * m22;
        float a4 = m12 * m24 - m14 * m22;
        float a5 = m13 * m24 - m14 * m23;
        float b0 = m31 * m42 - m32 * m41;
        float b1 = m31 * m43 - m33 * m41;
        float b2 = m31 * m44 - m34 * m41;
        float b3 = m32 * m43 - m33 * m42;
        float b4 = m32 * m44 - m34 * m42;
        float b5 = m33 * m44 - m34 * m43;
        return a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0;
    }

    public Mat4f add(Mat4f m) {
        Mat4f result = new Mat4f();
        result.m11 = m11 + m.m11;
        result.m12 = m12 + m.m12;
        result.m13 = m13 + m.m13;
        result.m14 = m14 + m.m14;
        result.m21 = m21 + m.m21;
        result.m22 = m22 + m.m22;
        result.m23 = m23 + m.m23;
        result.m24 = m24 + m.m24;
        result.m31 = m31 + m.m31;
        result.m32 = m32 + m.m32;
        result.m33 = m33 + m.m33;
        result.m34 = m34 + m.m34;
        result.m41 = m41 + m.m41;
        result.m42 = m42 + m.m42;
        result.m43 = m43 + m.m43;
        result.m44 = m44 + m.m44;
        return result;
    }

    public Mat4f addInplace(Mat4f m) {
        m11 += m.m11;
        m12 += m.m12;
        m13 += m.m13;
        m14 += m.m14;
        m21 += m.m21;
        m22 += m.m22;
        m23 += m.m23;
        m24 += m.m24;
        m31 += m.m31;
        m32 += m.m32;
        m33 += m.m33;
        m34 += m.m34;
        m41 += m.m41;
        m42 += m.m42;
        m43 += m.m43;
        m44 += m.m44;
        return this;
    }

    @Override
    public String toString() {
        return "[[" + m11 + ", " + m12 + ", " + m13 + ", " + m14 + "] [" + m21 + ", " + m22 + ", " + m23 + ", " + m24
            + "] [" + m31 + ", " + m32 + ", " + m33 + ", " + m34 + "] [" + m41 + ", " + m42 + ", " + m43 + ", " + m44
            + "]]";
    }

    @Override
    public int hashCode() {
        int hash = 37;
        hash = 37 * hash + Float.floatToIntBits(m11);
        hash = 37 * hash + Float.floatToIntBits(m12);
        hash = 37 * hash + Float.floatToIntBits(m13);
        hash = 37 * hash + Float.floatToIntBits(m14);
        hash = 37 * hash + Float.floatToIntBits(m21);
        hash = 37 * hash + Float.floatToIntBits(m22);
        hash = 37 * hash + Float.floatToIntBits(m23);
        hash = 37 * hash + Float.floatToIntBits(m24);
        hash = 37 * hash + Float.floatToIntBits(m31);
        hash = 37 * hash + Float.floatToIntBits(m32);
        hash = 37 * hash + Float.floatToIntBits(m33);
        hash = 37 * hash + Float.floatToIntBits(m34);
        hash = 37 * hash + Float.floatToIntBits(m41);
        hash = 37 * hash + Float.floatToIntBits(m42);
        hash = 37 * hash + Float.floatToIntBits(m43);
        hash = 37 * hash + Float.floatToIntBits(m44);
        return Long.hashCode(hash);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Mat4f)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        Mat4f mat = (Mat4f)obj;
        return m11 == mat.m11
            && m12 == mat.m12
            && m13 == mat.m13
            && m14 == mat.m14
            && m21 == mat.m21
            && m22 == mat.m22
            && m23 == mat.m23
            && m24 == mat.m24
            && m31 == mat.m31
            && m32 == mat.m32
            && m33 == mat.m33
            && m34 == mat.m34
            && m41 == mat.m41
            && m42 == mat.m42
            && m43 == mat.m43
            && m44 == mat.m44;
    }

}
