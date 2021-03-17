/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.camera;

/** which curve is the easiest one.. */
public enum AirplanePreferredTurn {
    LEFT,
    NONE,
    RIGHT,
    BOTH;

    public AirplanePreferredTurn getReverse() {
        switch (this) {
        case LEFT:
            return RIGHT;
        case RIGHT:
            return LEFT;
        default:
            return this;
        }
    }
}
