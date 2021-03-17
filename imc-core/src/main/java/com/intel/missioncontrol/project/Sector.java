/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.geospatial.Position;
import java.util.List;

public class Sector {
    private static final double DEG_TO_RAD = Math.PI / 180.0;

    public Sector(double radians, double radians1, double radians2, double radians3) {}

    public static Sector fromDegrees(double i, double i1, double i2, double i3) {
        return new Sector(i * DEG_TO_RAD, i1 * DEG_TO_RAD, i2 * DEG_TO_RAD, i3 * DEG_TO_RAD);
    }

    public List<Position> getCorners() {
        return null;
    }
}
