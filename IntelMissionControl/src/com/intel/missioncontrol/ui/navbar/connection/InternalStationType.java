/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

/** @author Vladimir Iordanov */
public enum InternalStationType {
    MAVINCI("MAVinci Connector"),
    INTEL("Intel Wireless Base Station");

    private final String label;

    InternalStationType(String label) {
        this.label = label;
    }

    public static InternalStationType getDefault() {
        return MAVINCI;
    }

    @Override
    public String toString() {
        return label;
    }
}
