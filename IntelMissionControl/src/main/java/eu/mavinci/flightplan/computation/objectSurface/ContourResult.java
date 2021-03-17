/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import java.util.Vector;

public class ContourResult {
    public Vector<Vector<Vec4>> lines;
    public Vector<Vector<Position>> polygons;
    public Vector<Position> elevations;
}
