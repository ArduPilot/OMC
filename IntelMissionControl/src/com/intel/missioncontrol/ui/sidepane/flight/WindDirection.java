/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

public enum WindDirection {
    N,
    NNE,
    NE,
    ENE,
    E,
    ESE,
    SE,
    SSE,
    S,
    SSW,
    SW,
    WSW,
    W,
    WNW,
    NW,
    NNW;

    private static final double FULL_CIRCLE = 360.0;
    private static final double STEP_DEGREES = 22.5;

    public static WindDirection getDirection(double angleDegrees) {
        angleDegrees = normalizeAngle(angleDegrees);
        int index = (int)Math.round(angleDegrees / STEP_DEGREES);

        if (index >= values().length) {
            index = 0;
        }

        return values()[index];
    }

    public static double normalizeAngle(double angleDegrees) {
        if ((angleDegrees > FULL_CIRCLE) || (angleDegrees < -FULL_CIRCLE)) {
            angleDegrees %= FULL_CIRCLE;
        }

        if (angleDegrees < 0) {
            angleDegrees += FULL_CIRCLE;
        }

        return angleDegrees;
    }

}
