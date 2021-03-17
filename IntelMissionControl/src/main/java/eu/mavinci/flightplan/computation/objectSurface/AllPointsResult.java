/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import eu.mavinci.flightplan.computation.FlightplanVertex;
import java.util.Vector;

public class AllPointsResult {
    public Vector<Vector<FlightplanVertex>> allSubClouds = new Vector<>();
    public Vector<FlightplanVertex> flightTour = new Vector<>();
}
