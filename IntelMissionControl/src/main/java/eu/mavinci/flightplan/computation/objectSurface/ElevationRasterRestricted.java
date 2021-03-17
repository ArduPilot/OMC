/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.flightplan.ITransformationProvider;
import gov.nasa.worldwind.geom.Sector;

public class ElevationRasterRestricted {
    public double[][] zValues; // first index is for x, undefined values are -inf
    public MinMaxPair minMaxX; // in local coordinates
    public MinMaxPair minMaxY; // in local coordinates
    public MinMaxPair minMaxZ; // in wgs84 elevations
    public int xSteps;
    public int ySteps;
    public ITransformationProvider trafo;
    public Sector sector;
}
