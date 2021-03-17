/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.terrain;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

public class MockElevationHelper extends IElevationHelper{

    @Override
    public Vector3f intersectionWithTerrain(IElevationModel elev, Vector3f origin, Vector3f direction) {
        return origin.add(direction).mult(100f);
    }
}
