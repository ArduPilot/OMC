/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.math;

public class MathHelper {
    public static boolean isDifferenceTiny(double x1, double x2) {
        if(Math.abs(x1-x2) < 1e-6) {
            return true;
        } else {
            return false;
        }
    }
}
