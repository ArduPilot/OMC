/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.measure;

public class DoubleHelper {

    public static boolean areClose(double a, double b) {
        if (a == b) {
            return true;
        }

        double eps = (Math.abs(a) + Math.abs(b) + 10.0) * 2.2204460492503131e-016;
        double delta = a - b;
        return (-eps < delta) && (eps > delta);
    }

    public static boolean greaterThan(double a, double b) {
        return a > b && !areClose(a, b);
    }

    public static boolean lessThan(double a, double b) {
        return a < b && !areClose(a, b);
    }

}
