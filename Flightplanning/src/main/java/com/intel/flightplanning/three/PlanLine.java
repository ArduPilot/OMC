/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.three;

import com.jme3.math.Vector3f;
import java.util.LinkedList;

public class PlanLine {
    public int idx;
    public LinkedList<LinePoint> points = new LinkedList<>();
    public Vector3f scanDir;
    public double scanLevel;
    public boolean closedLoop;
}
