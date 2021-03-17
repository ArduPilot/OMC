/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.three;

import com.intel.flightplanning.core.Waypoint;
import com.intel.flightplanning.routing.tsp.TspPath;
import java.util.List;

public class EuclDist implements TspPath.DistanceFunction<Waypoint> {

    @Override
    public double distance(List<Waypoint> list) {
        Waypoint last = null;
        double distance = 0;
        double HEIGHT_FACTOR = 0.1;
        double HEIGHT_FACTOR2 = HEIGHT_FACTOR * HEIGHT_FACTOR;
        double YAW_FACTOR = 0.1;
        for (Waypoint next : list) {
            if (last != null) {
                var dx = next.getCameraPosition().x - last.getCameraPosition().x;
                var dy = next.getCameraPosition().y - last.getCameraPosition().y;
                var dz = next.getCameraPosition().z - last.getCameraPosition().z;
                var dyaw = next.getYaw() - last.getYaw();
                while (dyaw > 180) dyaw -= 360;
                while (dyaw < -180) dyaw += 360;

                distance +=
                        Math.sqrt(dx * dx + dy * dy + dz * dz * HEIGHT_FACTOR2) + YAW_FACTOR * Math.abs(dyaw);
            }

            last = next;
        }

        return distance;
    }

    @Override
    public boolean isAcceptable(List<Waypoint> list) {
        return true;
    }

}