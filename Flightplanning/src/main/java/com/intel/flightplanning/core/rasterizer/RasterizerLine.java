/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.rasterizer;

import com.intel.flightplanning.core.Goal;
import com.intel.flightplanning.core.Waypoint;
import com.intel.flightplanning.core.annotations.NeedsRework;
import com.intel.flightplanning.core.terrain.IElevationModel;
import com.intel.flightplanning.core.vehicle.UAV;
import com.intel.flightplanning.geometry2d.Geometry2D;
import com.intel.flightplanning.sensor.Camera;
import com.intel.flightplanning.sensor.PhotoProperties;
import java.util.List;

public class RasterizerLine {
    @NeedsRework
    public static List<Waypoint> rasterize(
            List<Geometry2D> aoiList,
            Camera camera,
            UAV uav,
            IElevationModel elev,
            Goal goal,
            PhotoProperties photoProps) {
    return null;
    }
}
