/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

public enum RtkType {
    INTERNAL_BASE_STATION("Internal Base Station", "Internal"),
    EXTERNAL_BASE_STATION("External Base Station", "External"),
    NTRIP("NTRIP", "NTRIP"),
    NONE("None", "");

    private final String label;
    private final String menuText;

    RtkType(String label, String menuText) {
        this.label = label;
        this.menuText = menuText;
    }

    public static RtkType getDefault() {
        return NTRIP;
    }

    public String getMenuText() {
        return menuText;
    }

    @Override
    public String toString() {
        return label;
    }
}
