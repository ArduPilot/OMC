/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.landing;

public enum UavStartDirection {
    NORTH(0),
    EAST(90),
    SOUTH(180),
    WEST(270),
    CUSTOM(-1);

    private int angles;

    private UavStartDirection(int angles) {
        this.angles = angles;
    }

    public int getAngles() {
        return angles;
    }
}
