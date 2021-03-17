/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geometry;

import static com.intel.missioncontrol.geometry.FuzzyAssert.assertClose;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class QuaternionTest {

    @Test
    void fromRotationMatrix3() {
        assertClose(
            new Quaternion(0.2796113282313911, 0.5592226564627822, 0.8388339846941733, 1.5047154743879707),
            Quaternion.fromRotationMatrix(Mat3.fromAxisAngle(new Vec3(1, 2, 3), 1)));
    }

    @Test
    void fromRotationMatrix4() {
        assertClose(
            new Quaternion(0.2796113282313911, 0.5592226564627822, 0.8388339846941733, 1.5047154743879707),
            Quaternion.fromRotationMatrix(Mat4.fromAxisAngle(new Vec3(1, 2, 3), 1)));

        assertClose(
            new Quaternion(-0.3106224510657039, 0.7182870182434115, -0.4444351134430008, -0.43595284407356577),
            Quaternion.fromRotationMatrix(Mat4.fromYawPitchRoll(1, 2, 3)));
    }

    @Test
    void fromAxisAngle() {
        assertClose(
            new Quaternion(0.479425538604203, 0.958851077208406, 1.438276615812609, 0.8775825618903728),
            Quaternion.fromAxisAngle(new Vec3(1, 2, 3), 1));
    }

    @Test
    void fromYawPitchRoll() {
        assertClose(
            new Quaternion(0.3106224510657039, -0.7182870182434115, 0.44443511344300074, 0.4359528440735657),
            Quaternion.fromYawPitchRoll(1, 2, 3));
    }

    @Test
    void isIdentity() {
        assertTrue(new Quaternion(0, 0, 0, 1).isIdentity());
        assertFalse(new Quaternion(1, 0, 0, 1).isIdentity());
    }

    @Test
    void slerp() {
        assertClose(
            new Quaternion(3.5, 4.5, 5.5, 6.5), new Quaternion(1, 2, 3, 4).slerp(new Quaternion(6, 7, 8, 9), 0.5));
    }

    @Test
    void add() {
        assertClose(new Quaternion(2, 2, 2, 2), new Quaternion(1, 1, 1, 1).add(new Quaternion(1, 1, 1, 1)));
    }

    @Test
    void addInplace() {
        assertClose(new Quaternion(2, 2, 2, 2), new Quaternion(1, 1, 1, 1).addInplace(new Quaternion(1, 1, 1, 1)));
    }

    @Test
    void subtract() {
        assertClose(new Quaternion(2, 2, 2, 2), new Quaternion(3, 3, 3, 3).subtract(new Quaternion(1, 1, 1, 1)));
    }

    @Test
    void subtractInplace() {
        assertClose(new Quaternion(2, 2, 2, 2), new Quaternion(3, 3, 3, 3).subtractInplace(new Quaternion(1, 1, 1, 1)));
    }

    @Test
    void multiply() {
        assertClose(new Quaternion(8, 16, 12, -4), new Quaternion(1, 2, 3, 4).multiply(new Quaternion(2, 2, 2, 2)));
        assertClose(new Quaternion(2, 4, 6, 8), new Quaternion(1, 2, 3, 4).multiply(2));
    }

    @Test
    void multiplyInplace() {
        assertClose(
            new Quaternion(8, 16, 12, -4), new Quaternion(1, 2, 3, 4).multiplyInplace(new Quaternion(2, 2, 2, 2)));
        assertClose(new Quaternion(2, 4, 6, 8), new Quaternion(1, 2, 3, 4).multiplyInplace(2));
    }

    @Test
    void divide() {
        assertClose(
            new Quaternion(-0.25, -0.5, 0, 1.25), new Quaternion(1, 2, 3, 4).divide(new Quaternion(2, 2, 2, 2)));
        assertClose(new Quaternion(0.5, 1, 1.5, 2), new Quaternion(1, 2, 3, 4).divide(2));
    }

    @Test
    void divideInplace() {
        assertClose(
            new Quaternion(-0.25, -0.5, 0, 1.25), new Quaternion(1, 2, 3, 4).divideInplace(new Quaternion(2, 2, 2, 2)));
        assertClose(new Quaternion(0.5, 1, 1.5, 2), new Quaternion(1, 2, 3, 4).divideInplace(2));
    }

    @Test
    void dot() {
        assertClose(20, new Quaternion(1, 2, 3, 4).dot(new Quaternion(2, 2, 2, 2)));
    }

    @Test
    void normalize() {
        assertClose(
            new Quaternion(0.18257418583505536, 0.3651483716701107, 0.5477225575051661, 0.7302967433402214),
            new Quaternion(1, 2, 3, 4).normalize());
    }

    @Test
    void normalizeInplace() {
        assertClose(
            new Quaternion(0.18257418583505536, 0.3651483716701107, 0.5477225575051661, 0.7302967433402214),
            new Quaternion(1, 2, 3, 4).normalizeInplace());
    }

    @Test
    void invert() {
        assertClose(
            new Quaternion(-0.03333333333333333, -0.06666666666666667, -0.1, 0.13333333333333333),
            new Quaternion(1, 2, 3, 4).invert());
    }

    @Test
    void invertInplace() {
        assertClose(
            new Quaternion(-0.03333333333333333, -0.06666666666666667, -0.1, 0.13333333333333333),
            new Quaternion(1, 2, 3, 4).invertInplace());
    }

}
