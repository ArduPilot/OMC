/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.gui.wwext;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.terrain.ZeroElevationModel;

public class MFlatEarth extends FlatGlobe {

    @SuppressWarnings("deprecation")
    public MFlatEarth() {
        super(
            EarthFlat.WGS84_EQUATORIAL_RADIUS,
            EarthFlat.WGS84_POLAR_RADIUS,
            EarthFlat.WGS84_ES,
            new ZeroElevationModel());
        setProjection(FlatGlobe.PROJECTION_MERCATOR);
    }

    public static final double FLAT_GLOBE_OBJECT_ELEVATION = 0.5;

    @Override
    protected Vec4 geodeticToCartesian(Angle latitude, Angle longitude, double metersElevation) {
        return super.geodeticToCartesian(
            latitude,
            longitude,
            metersElevation > FLAT_GLOBE_OBJECT_ELEVATION
                ? FLAT_GLOBE_OBJECT_ELEVATION
                : (metersElevation < 0 ? 0 : metersElevation));
    }

    @Override
    public String toString() {
        return "Flat Eath mapping including flying Objects to Ground";
    }

    @Override
    public boolean intersects(Frustum frustum) {
        // this dirty hack disables 2D continuus rendering if map is pushed close to or over the dateline.
        // with this enabled the entire map becomes black if zoom level isnt closer than globe levela AND things from
        // both sides the dateline are visible
        return super.intersects(frustum) && getOffset() == 0;
    }
}
