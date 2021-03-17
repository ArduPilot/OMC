/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geospatial;

public class ProjectedPosition {

    public double x;
    public double y;
    public double z;

    public ProjectedPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(x) + 31 * Double.hashCode(y) + 31 * Double.hashCode(z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProjectedPosition)) {
            return false;
        }

        ProjectedPosition other = (ProjectedPosition)obj;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public String toString() {
        return x + "; " + y + "; " + z;
    }

}
