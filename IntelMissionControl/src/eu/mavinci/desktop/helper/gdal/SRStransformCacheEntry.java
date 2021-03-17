/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.gdal;

import gov.nasa.worldwind.geom.Vec4;

public final class SRStransformCacheEntry {

    public SRStransformCacheEntry(double x, double y, double z, String id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
    }

    public SRStransformCacheEntry(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = null;
    }

    public final double x;
    public final double y;
    public final double z;
    public final String id;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int)(temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int)(temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = prime * result + (int)(temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        SRStransformCacheEntry other = (SRStransformCacheEntry)obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }

        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
            return false;
        }

        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
            return false;
        }

        if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z)) {
            return false;
        }

        return true;
    }

    public Vec4 getVec4() {
        return new Vec4(x, y, z);
    }

    @Override
    public String toString() {
        return "x:" + x + " y:" + y + " z:" + z + " id:" + id;
    }
}
