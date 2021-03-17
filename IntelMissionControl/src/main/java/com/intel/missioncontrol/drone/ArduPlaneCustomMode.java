/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import io.dronefleet.mavlink.ardupilotmega.PlaneMode;
import io.dronefleet.mavlink.util.reflection.MavlinkReflection;

class ArduPlaneCustomMode {
    private final PlaneMode planeMode;

    public static final ArduPlaneCustomMode undefined = new ArduPlaneCustomMode(PlaneMode.PLANE_MODE_STABILIZE);

    private ArduPlaneCustomMode(PlaneMode planeMode) {
        this.planeMode = planeMode;
    }

    PlaneMode getPlaneMode() {
        return planeMode;
    }

    static ArduPlaneCustomMode fromCustomMode(long customMode) {
        return MavlinkReflection.getEntryByValue(PlaneMode.class, (int)(customMode))
            .map(ArduPlaneCustomMode::new)
            .orElse(undefined);
    }
}
