/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.routing;

import com.intel.flightplanning.core.FlightPlan;
import com.intel.flightplanning.core.Waypoint;
import com.intel.flightplanning.routing.tsp.SimulatedAnnealing;
import com.intel.flightplanning.routing.tsp.TspPath;
import com.intel.flightplanning.routing.tsp.TspSolver;
import com.intel.flightplanning.three.EuclDist;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class SimpleRouteOptimizer extends IRouteOptimizer {
    @Override
    public List<Waypoint> optimize(FlightPlan fp) {
        List<Waypoint> waypoints = fp.getWaypoints();
        TspPath<Waypoint> path = new TspPath<Waypoint>();
        path.distanceFunction = new EuclDist();
        path.nodes = new ArrayList<Waypoint>();

        int k = 0;
        for (var wp : waypoints) {
            path.nodes.add(
                new Waypoint(
                    new Vector3f(wp.getCameraPosition().x, wp.getCameraPosition().y, wp.getCameraPosition().z),
                    new Vector3f(wp.getTarget().x, wp.getTarget().y, wp.getTarget().z)));
            k++;
        }

        TspSolver testSolver = (TspSolver)new SimulatedAnnealing(10, 100000, 0.9999, path);

        testSolver.solve();

        int l = 0;
        waypoints = new ArrayList<>(waypoints.size());
        for (var newWp : testSolver.getPath().nodes) {
            waypoints.add((Waypoint)newWp);
        }

        return waypoints;
    }
}
