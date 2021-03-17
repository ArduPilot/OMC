/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.coordinatetransform;

import org.gdal.osr.SpatialReference;

public class SpatialReferenceOptimizerResult {
    public MapProjection mapProjection;
    public double err;
    public SpatialReference optimizedSrs;
    public Ellipsoid ellipsoid;
    public double[] bwp;

    public SpatialReferenceOptimizerResult(
            SpatialReference optimizedSrs, MapProjection mapProjection, Ellipsoid ellipsoid, double[] bwp, double err) {
        this.optimizedSrs = optimizedSrs.Clone();
        if (mapProjection != null) {
            this.mapProjection = mapProjection.clone();
        }

        this.ellipsoid = ellipsoid;
        this.bwp = bwp;
        this.err = err;
    }

    // If this method is really needed, rename it to something other than "ToString"!
    //
    /*
     * public String ToString() { String res = mapProjection.projectionType.toString() + ", Parameters: "; for (int i = 0; i <
     * mapProjection.params().length; i++) { res += mapProjection.params()[i] + ", "; } res += "Residual error: " + err; return res; }
     */
}
