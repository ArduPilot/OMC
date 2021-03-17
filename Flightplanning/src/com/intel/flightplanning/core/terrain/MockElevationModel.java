/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.terrain;

import com.jme3.math.Vector3f;

public class MockElevationModel extends IElevationModel{
    public float getElevationAsGoodAsPossible(Vector3f r) {
        return -0f;
    }
}
