/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import io.dronefleet.mavlink.ardupilotmega.CopterMode;
import io.dronefleet.mavlink.util.reflection.MavlinkReflection;

class ArduCopterCustomMode {
    private final CopterMode copterMode;

    public static final ArduCopterCustomMode undefined = new ArduCopterCustomMode(CopterMode.COPTER_MODE_STABILIZE);

    private ArduCopterCustomMode(CopterMode copterMode) {
        this.copterMode = copterMode;
    }

    CopterMode getCopterMode() {
        return copterMode;
    }

    static ArduCopterCustomMode fromCustomMode(long customMode) {
        return MavlinkReflection.getEntryByValue(CopterMode.class, (int)(customMode))
            .map(ArduCopterCustomMode::new)
            .orElse(undefined);
    }
}
