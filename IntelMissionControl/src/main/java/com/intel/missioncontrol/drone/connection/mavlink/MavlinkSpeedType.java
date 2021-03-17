/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

public enum MavlinkSpeedType {
    AIR_SPEED,
    GROUND_SPEED,
    CLIMB_SPEED,
    DESCENT_SPEED;

    public int getValue() {
        return ordinal();
    }
}
