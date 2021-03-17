/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.Earth;

public class CircleAirspace extends Airspace {

    public CircleAirspace(String name, AirspaceTypes type, LatLon center, double radius_meters) {
        super(name, type);
        this.center = center;
        this.radius_meters = radius_meters;
        interpolate(DEFAULT_CRICLE_SAMPLING);
    }

    @Override
    public boolean withinAirspace(LatLon pos, double altitudeAbsoluteMeters) {
        if (insideCircle(pos))
            if ((floorMeters(pos) < altitudeAbsoluteMeters) && (altitudeAbsoluteMeters < ceilingMeters(pos)))
                return true;
        return false;
    }

    @Override
    public boolean insidePolygon(LatLon ref) {
        return insideCircle(ref);
    }

    private boolean insideCircle(LatLon pos) {
        double distance = LatLon.greatCircleDistance(pos, center).radians * Earth.WGS84_EQUATORIAL_RADIUS;
        return (distance < radius_meters);
    }

    private void interpolate(int num_of_sampling_points) {
        double center_lat_radians = center.getLatitude().getRadians();
        double center_lon_radians = center.getLongitude().getRadians();
        double radius_radians = (Math.PI / (180 * 60)) * radius_meters / NM_TO_METER;
        for (int i = 0; i < num_of_sampling_points; i++) {
            double turn_radians = i * (Math.PI * 2) / num_of_sampling_points;
            double lat =
                Math.asin(
                    Math.sin(center_lat_radians) * Math.cos(radius_radians)
                        + Math.cos(center_lat_radians) * Math.sin(radius_radians) * Math.cos(turn_radians));
            double lon;
            if (Math.cos(lat) == 0) lon = center_lon_radians; // endpoint a pole
            else
                lon =
                    ((center_lon_radians
                                - Math.asin(Math.sin(turn_radians) * Math.sin(radius_radians) / Math.cos(lat))
                                + Math.PI)
                            % (2 * Math.PI))
                        - Math.PI;
            vertices.add(LatLon.fromRadians(lat, lon));
        }
    }

    private static final int DEFAULT_CRICLE_SAMPLING = 20;
    private double radius_meters;
    private LatLon center;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CircleAirspace that = (CircleAirspace)o;

        if (Double.compare(that.radius_meters, radius_meters) != 0) return false;
        return center.equals(that.center);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(radius_meters);
        result = 31 * result + (int)(temp ^ (temp >>> 32));
        result = 31 * result + center.hashCode();
        return result;
    }
}
