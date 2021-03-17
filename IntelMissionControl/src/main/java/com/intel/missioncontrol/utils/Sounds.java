/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

public enum Sounds {
    EMERGENCY_ALERT("/com/intel/missioncontrol/sounds/alert.wav");

    private final String uri;

    Sounds(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }
}
