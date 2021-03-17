/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import org.apache.commons.math3.util.FastMath;

public class FastPositionTransformationProvider {
    private double centralLongitudeRad = Double.NaN;

    public FastPositionTransformationProvider(double centralLongitudeRad) {
        this.centralLongitudeRad = centralLongitudeRad;
    }

    /**
     * sinodial projection into ENU East-North-Up frame
     *
     * <p>https://en.wikipedia.org/wiki/Sinusoidal_projection
     *
     * @param p
     * @return
     */
    public Vec4 cheapToLocalRelHeights(Position p) {
        return new Vec4(
            (p.getLongitude().radians - centralLongitudeRad)
                * FastMath.cos(p.getLatitude().radians)
                * Earth.WGS84_EQUATORIAL_RADIUS,
            p.getLatitude().radians * Earth.WGS84_POLAR_RADIUS,
            p.elevation);
    }

    public Vec4 cheapToLocalRelHeights(LatLon latLon) {
        return new Vec4(
            (latLon.getLongitude().radians - centralLongitudeRad)
                * FastMath.cos(latLon.getLatitude().radians)
                * Earth.WGS84_EQUATORIAL_RADIUS,
            latLon.getLatitude().radians * Earth.WGS84_POLAR_RADIUS,
            0);
    }

    public Position cheapToGlobalRelHeights(Vec4 v) {
        double lat = v.y / Earth.WGS84_POLAR_RADIUS;
        double lon = centralLongitudeRad + v.x / Earth.WGS84_EQUATORIAL_RADIUS / FastMath.cos(lat);
        return Position.fromRadians(lat, lon, v.z);
    }
}
