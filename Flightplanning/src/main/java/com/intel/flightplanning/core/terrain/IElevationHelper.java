/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.terrain;

import com.jme3.math.Vector3f;

public abstract class IElevationHelper {
    public abstract Vector3f intersectionWithTerrain(IElevationModel elev, Vector3f origin, Vector3f direction);
}
