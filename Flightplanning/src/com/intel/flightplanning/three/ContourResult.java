/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.three;

import com.intel.flightplanning.core.Waypoint;
import com.jme3.math.Vector3f;
import java.util.Vector;

public class ContourResult {
    public Vector<Vector<Vector3f>> lines;
    public Vector<Vector<Waypoint>> polygons;
    public Vector<Waypoint> elevations;
}
