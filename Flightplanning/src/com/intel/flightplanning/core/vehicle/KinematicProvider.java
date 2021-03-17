/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.vehicle;

import com.intel.flightplanning.core.GimbalStateVector;
import com.jme3.math.Vector3f;

public abstract class KinematicProvider {

    GimbalStateVector toGimbalCoordinates(double pitch) {
        return new GimbalStateVector();
    }
    // versus horizon
    GimbalStateVector toGimbalCoordinates(double roll, double pitch, double yaw) {
        // TODO: ask markus for implementation :=)
        return new GimbalStateVector();
    }

    public abstract Vector3f toRPY(GimbalStateVector gimbalStateVector);
}
