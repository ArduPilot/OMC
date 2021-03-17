/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geospatial;

public class GeocentricPosition {

    public double x;
    public double y;
    public double z;

    public GeocentricPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

}
