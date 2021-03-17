/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext.sun;

import gov.nasa.worldwind.geom.Angle;

public class AzEl {
    public final Angle azimuth;
    public final Angle elevation;

    /**
     * Factory method for obtaining a new <code>AzEl</code> from two angles expressed in degrees.
     *
     * @param azimuth in degrees
     * @param elevation in degrees
     * @return a new <code>AzEl</code> from the given angles, which are expressed as degrees
     */
    public static AzEl fromDegrees(double azimuth, double elevation) {
        return new AzEl(azimuth, elevation);
    }

    private AzEl(double azimuth, double elevation) {
        this.azimuth = Angle.fromDegrees(azimuth);
        this.elevation = Angle.fromDegrees(elevation);
    }

    /**
     * Factor method for obtaining a new <code>AzEl</code> from two angles expressed in radians.
     *
     * @param azimuth in radians
     * @param elevation in radians
     * @return a new <code>AzEl</code> from the given angles, which are expressed as radians
     */
    public static AzEl fromRadians(double azimuth, double elevation) {
        return new AzEl(Math.toDegrees(azimuth), Math.toDegrees(elevation));
    }

    @Override
    public String toString() {
        String las = String.format("Az %7.4f\u00B0", this.azimuth.getDegrees());
        String los = String.format("El %7.4f\u00B0", this.elevation.getDegrees());
        return "(" + las + ", " + los + ")";
    }
}
