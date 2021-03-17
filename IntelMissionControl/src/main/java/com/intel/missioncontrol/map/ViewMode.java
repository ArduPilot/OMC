/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map;

public enum ViewMode {
    DEFAULT,
    FOLLOW,
    COCKPIT,
    PAYLOAD;

    public boolean isPlaneCentered() {
        return this == PAYLOAD || this == COCKPIT;
    }
}
