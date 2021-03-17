/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation;

import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.core.helper.MinMaxPair;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.core.helper.MinMaxPair;

import java.util.ArrayList;

public class RefineResult {
    public ArrayList<Position> positions = new ArrayList<Position>(); // camera positions
    public ArrayList<Orientation> orientations = new ArrayList<>(); // camera orientations
    public ArrayList<Vec4> surfaceNormals = new ArrayList<>(); // camera center normals
    public MinMaxPair groundDistance =
        new MinMaxPair(); // distance minMax from each flight point to related ground point
    public MinMaxPair sollDistance =
        new MinMaxPair(); // minMax for distance from target altitude to individual altitudes above ground
    public double realToleranceGSD; // 0 means perfect, 1 means GSD between ZERO and twice the defined value
}
