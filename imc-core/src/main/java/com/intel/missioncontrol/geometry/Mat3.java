/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class Mat3 {

    public double m11, m12, m13;
    public double m21, m22, m23;
    public double m31, m32, m33;

    public Mat3() {}

    public Mat3(
            double m11,
            double m12,
            double m13,
            double m21,
            double m22,
            double m23,
            double m31,
            double m32,
            double m33) {
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

    public Mat3(Mat3 other) {
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

    public Mat3(Mat3f other) {
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

    public static Mat3 zero() {
        return new Mat3(0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public static Mat3 identity() {
        return new Mat3(1, 0, 0, 0, 1, 0, 0, 0, 1);
    }

    public static Mat3 fromQuaternion(Quaternion q) {
        double xx = q.x * q.x;
        double yy = q.y * q.y;
        double zz = q.z * q.z;
        double xy = q.x * q.y;
        double wz = q.z * q.w;
        double xz = q.z * q.x;
        double wy = q.y * q.w;
        double yz = q.y * q.z;
        double wx = q.x * q.w;

        return new Mat3(
            1.0 - 2.0 * (yy + zz),
            2.0 * (xy + wz),
            2.0 * (xz - wy),
            2.0 * (xy - wz),
            1.0 - 2.0 * (zz + xx),
            2.0 * (yz + wx),
            2.0 * (xz + wy),
            2.0 * (yz - wx),
            1.0 - 2.0 * (yy + xx));
    }

    public static Mat3 fromAxisAngle(Vec3 axis, double angle) {
        double x = axis.x, y = axis.y, z = axis.z;
        double sa = Math.sin(angle), ca = Math.cos(angle);
        double xx = x * x, yy = y * y, zz = z * z;
        double xy = x * y, xz = x * z, yz = y * z;

        return new Mat3(
            xx + ca * (1.0 - xx),
            xy - ca * xy + sa * z,
            xz - ca * xz - sa * y,
            xy - ca * xy - sa * z,
            yy + ca * (1.0 - yy),
            yz - ca * yz + sa * x,
            xz - ca * xz + sa * y,
            yz - ca * yz - sa * x,
            zz + ca * (1.0 - zz));
    }

    public static Mat3 fromRotationX(double radians) {
        double c = Math.cos(radians);
        double s = Math.sin(radians);
        return new Mat3(1, 0, 0, 0, c, s, 0, -s, c);
    }

    public static Mat3 fromRotationY(double radians) {
        double c = Math.cos(radians);
        double s = Math.sin(radians);
        return new Mat3(c, 0, -s, 0, 1, 0, s, 0, c);
    }

    public static Mat3 fromRotationZ(double radians) {
        double c = Math.cos(radians);
        double s = Math.sin(radians);
        return new Mat3(c, s, 0, -s, c, 0, 0, 0, 1);
    }

    public static Mat3 fromYawPitchRoll(double yaw, double pitch, double roll) {
        return fromQuaternion(Quaternion.fromYawPitchRoll(yaw, pitch, roll));
    }

    public static Mat3 fromMat4(Mat4 mat4) {
        return new Mat3(mat4.m11, mat4.m12, mat4.m13, mat4.m21, mat4.m22, mat4.m23, mat4.m31, mat4.m32, mat4.m33);
    }

    public Vec3 column(int i) {
        switch (i) {
        case 0:
            return new Vec3(m11, m21, m31);
        case 1:
            return new Vec3(m12, m22, m32);
        case 2:
            return new Vec3(m13, m23, m33);
        default:
            throw new IndexOutOfBoundsException("Invalid column: " + i);
        }
    }

    public Vec3 row(int i) {
        switch (i) {
        case 0:
            return new Vec3(m11, m12, m13);
        case 1:
            return new Vec3(m21, m22, m23);
        case 2:
            return new Vec3(m31, m32, m33);
        default:
            throw new IndexOutOfBoundsException("Invalid row: " + i);
        }
    }

    public boolean isIdentity() {
        return (m11 == 1 && m12 == 0 && m13 == 0)
            && (m21 == 0 && m22 == 1 && m23 == 0)
            && (m31 == 0 && m32 == 0 && m33 == 1);
    }

    public double trace() {
        return m11 + m22 + m33;
    }

    public Mat3 multiply(Mat3 m) {
        return new Mat3(
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

    public Mat3 multiplyInplace(Mat3 m) {
        double t00 = m11 * m.m11 + m12 * m.m21 + m13 * m.m31;
        double t01 = m11 * m.m12 + m12 * m.m22 + m13 * m.m32;
        double t02 = m11 * m.m13 + m12 * m.m23 + m13 * m.m33;
        double t10 = m21 * m.m11 + m22 * m.m21 + m23 * m.m31;
        double t11 = m21 * m.m12 + m22 * m.m22 + m23 * m.m32;
        double t12 = m21 * m.m13 + m22 * m.m23 + m23 * m.m33;
        double t20 = m31 * m.m11 + m32 * m.m21 + m33 * m.m31;
        double t21 = m31 * m.m12 + m32 * m.m22 + m33 * m.m32;
        double t22 = m31 * m.m13 + m32 * m.m23 + m33 * m.m33;
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

    public Mat3 multiply(double scalar) {
        return new Mat3(
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

    public Mat3 multiplyInplace(double s) {
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

    public Mat3 transpose() {
        return new Mat3(this).transposeInplace();
    }

    public Mat3 transposeInplace() {
        double t = m12;
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

    public Mat3 invert() {
        return new Mat3(this).invertInplace();
    }

    public Mat3 invertInplace() {
        double det = det();
        if (DoubleHelper.isCloseToZero(Math.abs(det))) {
            throw new ArithmeticException("The matrix is not invertible.");
        }

        double f00 = m22 * m33 - m23 * m32;
        double f01 = m13 * m32 - m12 * m33;
        double f02 = m12 * m23 - m13 * m22;
        double f10 = m23 * m31 - m21 * m33;
        double f11 = m11 * m33 - m13 * m31;
        double f12 = m13 * m21 - m11 * m23;
        double f20 = m21 * m32 - m22 * m31;
        double f21 = m12 * m31 - m11 * m32;
        double f22 = m11 * m22 - m12 * m21;
        m11 = f00;
        m12 = f01;
        m13 = f02;
        m21 = f10;
        m22 = f11;
        m23 = f12;
        m31 = f20;
        m32 = f21;
        m33 = f22;

        return multiplyInplace(1.0 / det);
    }

    public Mat3 adjugate() {
        return new Mat3(this).adjugateInplace();
    }

    public Mat3 adjugateInplace() {
        double t00 = m22 * m33 - m23 * m32;
        double t01 = m13 * m32 - m12 * m33;
        double t02 = m12 * m23 - m13 * m22;
        double t10 = m23 * m31 - m21 * m33;
        double t11 = m11 * m33 - m13 * m31;
        double t12 = m13 * m21 - m11 * m23;
        double t20 = m21 * m32 - m22 * m31;
        double t21 = m12 * m31 - m11 * m32;
        double t22 = m11 * m22 - m12 * m21;
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

    public double det() {
        double t0 = m22 * m33 - m23 * m32;
        double t1 = m23 * m31 - m21 * m33;
        double t2 = m21 * m32 - m22 * m31;
        return m11 * t0 + m12 * t1 + m13 * t2;
    }

    @Override
    public String toString() {
        return "[[" + m11 + ", " + m12 + ", " + m13 + "] [" + m21 + ", " + m22 + ", " + m23 + "] [" + m31 + ", " + m32
            + ", " + m33 + "]]";
    }

    @Override
    public int hashCode() {
        long hash = 37;
        hash = 37 * hash + Double.doubleToLongBits(m11);
        hash = 37 * hash + Double.doubleToLongBits(m12);
        hash = 37 * hash + Double.doubleToLongBits(m13);
        hash = 37 * hash + Double.doubleToLongBits(m21);
        hash = 37 * hash + Double.doubleToLongBits(m22);
        hash = 37 * hash + Double.doubleToLongBits(m23);
        hash = 37 * hash + Double.doubleToLongBits(m31);
        hash = 37 * hash + Double.doubleToLongBits(m32);
        hash = 37 * hash + Double.doubleToLongBits(m33);
        return Long.hashCode(hash);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Mat3)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        Mat3 other = (Mat3)obj;
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
