/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import gov.nasa.worldwind.geom.Vec4;

public class LinePoint {
    public Vec4 p;
    public Vec4 normal;
    public double curving; // 1 means not curved, mess them one inner corner, larger 1 outer corner
}
