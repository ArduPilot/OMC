/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.three;

import com.intel.flightplanning.core.Waypoint;
import java.util.Vector;

public class AllPointsResult {
    public Vector<Vector<Waypoint>> allSubClouds = new Vector<>();
    public Vector<Waypoint> flightTour = new Vector<>();
}
