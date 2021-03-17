/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.rasterizer;

import com.intel.flightplanning.core.FlightPlan;
import com.intel.flightplanning.core.Goal;
import com.intel.flightplanning.core.terrain.IElevationModel;
import com.intel.flightplanning.core.vehicle.UAV;
import com.intel.flightplanning.geometry2d.Geometry2D;
import com.intel.flightplanning.sensor.Camera;
/**
 * A Rasterizer takes an AreaOfInterest, a Camera, a UAV, an elevation model and a goal description to return a
 * FlightPlan which contains a list of waypoints and flightlines that the uav shall follow to reach its goal.
 */
public class RasterizerBuilding {
    public FlightPlan rasterize(Geometry2D aoi, Camera camera, UAV uav, IElevationModel elev, Goal goal) {
        return null;
    }

    public FlightPlan rasterize(FlightPlan startWithThis, Geometry2D aoi, Camera camera, UAV uav, IElevationModel elev, Goal goal) {
        return null;
    }
}
