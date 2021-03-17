/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.helper.optimizer.tsp;

import eu.mavinci.flightplan.computation.FlightplanVertex;
import eu.mavinci.flightplan.computation.FlightplanVertex;

/**
 * Repräsentation eines Eckpunkts des TSP Graphen
 *
 * @author Marco Moeller
 */
public class TspPoint {
    private FlightplanVertex flightplanVertex;

    public TspPoint(FlightplanVertex source) {
        flightplanVertex = source;
    }

    public FlightplanVertex getSource() {
        return flightplanVertex;
    }

    public double getX() {
        return flightplanVertex.getWayPoint().x;
    }

    public double getY() {
        return flightplanVertex.getWayPoint().y;
    }

    public double getZ() {
        return flightplanVertex.getWayPoint().z;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TspPoint) {
            TspPoint other = (TspPoint)obj;
            return (other.flightplanVertex == flightplanVertex);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return flightplanVertex.hashCode();
    }
}
