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

class QuaternionfTest {

    @Test
    void fromRotationMatrix3() {
        assertClose(
            new Quaternionf(0.2796113282313911f, 0.5592226564627822f, 0.8388339846941733f, 1.5047154743879707f),
            Quaternionf.fromRotationMatrix(Mat3f.fromAxisAngle(new Vec3f(1, 2, 3), 1)));
    }

    @Test
    void fromRotationMatrix4() {
        assertClose(
            new Quaternionf(0.2796113282313911f, 0.5592226564627822f, 0.8388339846941733f, 1.5047154743879707f),
            Quaternionf.fromRotationMatrix(Mat4f.fromAxisAngle(new Vec3f(1, 2, 3), 1)));

        assertClose(
            new Quaternionf(-0.3106224510657039f, 0.7182870182434115f, -0.4444351134430008f, -0.43595284407356577f),
            Quaternionf.fromRotationMatrix(Mat4f.fromYawPitchRoll(1, 2, 3)));
    }

    @Test
    void fromAxisAngle() {
        assertClose(
            new Quaternionf(0.479425538604203f, 0.958851077208406f, 1.438276615812609f, 0.8775825618903728f),
            Quaternionf.fromAxisAngle(new Vec3f(1, 2, 3), 1));
    }

    @Test
    void fromYawPitchRoll() {
        assertClose(
            new Quaternionf(0.3106224510657039f, -0.7182870182434115f, 0.44443511344300074f, 0.4359528440735657f),
            Quaternionf.fromYawPitchRoll(1, 2, 3));
    }

    @Test
    void isIdentity() {
        assertTrue(new Quaternionf(0, 0, 0, 1).isIdentity());
        assertFalse(new Quaternionf(1, 0, 0, 1).isIdentity());
    }

    @Test
    void slerp() {
        assertClose(
            new Quaternionf(3.5f, 4.5f, 5.5f, 6.5f),
            new Quaternionf(1, 2, 3, 4).slerp(new Quaternionf(6, 7, 8, 9), 0.5f));
    }

    @Test
    void add() {
        assertClose(new Quaternionf(2, 2, 2, 2), new Quaternionf(1, 1, 1, 1).add(new Quaternionf(1, 1, 1, 1)));
    }

    @Test
    void addInplace() {
        assertClose(new Quaternionf(2, 2, 2, 2), new Quaternionf(1, 1, 1, 1).addInplace(new Quaternionf(1, 1, 1, 1)));
    }

    @Test
    void subtract() {
        assertClose(new Quaternionf(2, 2, 2, 2), new Quaternionf(3, 3, 3, 3).subtract(new Quaternionf(1, 1, 1, 1)));
    }

    @Test
    void subtractInplace() {
        assertClose(
            new Quaternionf(2, 2, 2, 2), new Quaternionf(3, 3, 3, 3).subtractInplace(new Quaternionf(1, 1, 1, 1)));
    }

    @Test
    void multiply() {
        assertClose(new Quaternionf(8, 16, 12, -4), new Quaternionf(1, 2, 3, 4).multiply(new Quaternionf(2, 2, 2, 2)));
        assertClose(new Quaternionf(2, 4, 6, 8), new Quaternionf(1, 2, 3, 4).multiply(2));
    }

    @Test
    void multiplyInplace() {
        assertClose(
            new Quaternionf(8, 16, 12, -4), new Quaternionf(1, 2, 3, 4).multiplyInplace(new Quaternionf(2, 2, 2, 2)));
        assertClose(new Quaternionf(2, 4, 6, 8), new Quaternionf(1, 2, 3, 4).multiplyInplace(2));
    }

    @Test
    void divide() {
        assertClose(
            new Quaternionf(-0.25f, -0.5f, 0, 1.25f), new Quaternionf(1, 2, 3, 4).divide(new Quaternionf(2, 2, 2, 2)));
        assertClose(new Quaternionf(0.5f, 1, 1.5f, 2), new Quaternionf(1, 2, 3, 4).divide(2));
    }

    @Test
    void divideInplace() {
        assertClose(
            new Quaternionf(-0.25f, -0.5f, 0, 1.25f),
            new Quaternionf(1, 2, 3, 4).divideInplace(new Quaternionf(2, 2, 2, 2)));
        assertClose(new Quaternionf(0.5f, 1, 1.5f, 2), new Quaternionf(1, 2, 3, 4).divideInplace(2));
    }

    @Test
    void dot() {
        assertClose(20, new Quaternionf(1, 2, 3, 4).dot(new Quaternionf(2, 2, 2, 2)));
    }

    @Test
    void normalize() {
        assertClose(
            new Quaternionf(0.18257418583505536f, 0.3651483716701107f, 0.5477225575051661f, 0.7302967433402214f),
            new Quaternionf(1, 2, 3, 4).normalize());
    }

    @Test
    void normalizeInplace() {
        assertClose(
            new Quaternionf(0.18257418583505536f, 0.3651483716701107f, 0.5477225575051661f, 0.7302967433402214f),
            new Quaternionf(1, 2, 3, 4).normalizeInplace());
    }

    @Test
    void invert() {
        assertClose(
            new Quaternionf(-0.03333333333333333f, -0.06666666666666667f, -0.1f, 0.13333333333333333f),
            new Quaternionf(1, 2, 3, 4).invert());
    }

    @Test
    void invertInplace() {
        assertClose(
            new Quaternionf(-0.03333333333333333f, -0.06666666666666667f, -0.1f, 0.13333333333333333f),
            new Quaternionf(1, 2, 3, 4).invertInplace());
    }

}
