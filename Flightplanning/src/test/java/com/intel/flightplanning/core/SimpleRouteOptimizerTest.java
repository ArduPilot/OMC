/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

import com.intel.flightplanning.core.FlightPlan;
import com.intel.flightplanning.core.Waypoint;
import com.intel.flightplanning.routing.SimpleRouteOptimizer;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class SimpleRouteOptimizerTest {

    @Test
    public void optimize() {
        var wps = new ArrayList<Waypoint>();
        wps.add(new Waypoint(new Vector3f(0,0,0), new Vector3f(0,0,0)));
        wps.add(new Waypoint(new Vector3f(10,0,0), new Vector3f(0,0,0)));
        wps.add(new Waypoint(new Vector3f(5,0,0), new Vector3f(0,0,0)));
        wps.add(new Waypoint(new Vector3f(20,0,0), new Vector3f(0,0,0)));
        var fp = new FlightPlan(wps);
        SimpleRouteOptimizer opt = new SimpleRouteOptimizer();
        var optWps = opt.optimize(fp);
        for (var wp : optWps) {
            System.out.println(wp);
        }
    }
}