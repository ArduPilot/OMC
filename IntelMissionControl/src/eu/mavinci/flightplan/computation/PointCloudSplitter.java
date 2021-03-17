/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

public class PointCloudSplitter {
    public static Vector<Vector<FlightplanVertex>> splitPointCloud(Vector<FlightplanVertex> points, double maxDist) {
        double maxDistSq = maxDist * maxDist;
        Vector<Vector<FlightplanVertex>> res = new Vector<>();

        LinkedList<FlightplanVertex> pointsUnfinished = new LinkedList<>();
        pointsUnfinished.addAll(points);

        // outer loop for starting node curUnderTest
        while (!pointsUnfinished.isEmpty()) {
            Vector<FlightplanVertex> currentCluster = new Vector<>();
            LinkedList<FlightplanVertex> pointsUnderProbe = new LinkedList<>();
            pointsUnderProbe.add(pointsUnfinished.removeFirst());

            while (!pointsUnderProbe.isEmpty()) {
                FlightplanVertex curPoint = pointsUnderProbe.removeFirst();
                currentCluster.add(curPoint);

                // Find all direct neighbors:
                Iterator<FlightplanVertex> it = pointsUnfinished.iterator();
                while (it.hasNext()) {
                    FlightplanVertex next = it.next();
                    if (next.distanceAtWaypointSquared(curPoint) <= maxDistSq) {
                        pointsUnderProbe.add(next);
                        it.remove();
                    }
                }
            }

            res.add(currentCluster);
        }

        return res;
    }

}
