/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.vehicle;

import com.intel.flightplanning.core.GimbalStateVector;
import com.jme3.math.Vector3f;

public class HingeKinematicProvider extends KinematicProvider {
    @Override
    public Vector3f toRPY(GimbalStateVector gimbalStateVector) {
        return null;
    }
}
