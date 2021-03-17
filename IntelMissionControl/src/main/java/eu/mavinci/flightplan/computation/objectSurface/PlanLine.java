/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import gov.nasa.worldwind.geom.Vec4;
import java.util.LinkedList;

public class PlanLine {
    public int idx;
    public LinkedList<LinePoint> points = new LinkedList<>();
    public Vec4 scanDir;
    public double scanLevel;
    public boolean closedLoop;
}
