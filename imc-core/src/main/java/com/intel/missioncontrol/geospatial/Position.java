/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geospatial;

import com.intel.missioncontrol.geometry.Mat4;
import com.intel.missioncontrol.geometry.Vec3;
import com.intel.missioncontrol.serialization.BinaryDeserializationContext;
import com.intel.missioncontrol.serialization.BinarySerializable;
import com.intel.missioncontrol.serialization.BinarySerializationContext;
import com.intel.missioncontrol.serialization.PrimitiveDeserializationContext;
import com.intel.missioncontrol.serialization.PrimitiveSerializable;
import com.intel.missioncontrol.serialization.PrimitiveSerializationContext;

/** Describes a WGS84 position on the globe, including elevation above the ellipsoid. */
public class Position implements PrimitiveSerializable, BinarySerializable {

    private final double latitude;
    private final double longitude;
    private final double elevation;

    public static Position fromRadians(double latitude, double longitude) {
        return new Position(Convert.radiansToDegrees(latitude), Convert.radiansToDegrees(longitude), 0);
    }

    public static Position fromRadians(double latitude, double longitude, double elevation) {
        return new Position(Convert.radiansToDegrees(latitude), Convert.radiansToDegrees(longitude), elevation);
    }

    public static Position fromDegrees(double latitude, double longitude, double elevation) {
        return new Position(latitude, longitude, elevation);
    }

    public static Position fromDegrees(double latitude, double longitude) {
        return new Position(latitude, longitude, 0);
    }

    private Position(double latitude, double longitude, double elevation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }

    public Position(PrimitiveDeserializationContext context) {
        String[] coords = context.read().split(",");
        latitude = Double.parseDouble(coords[0]);
        longitude = Double.parseDouble(coords[1]);
        elevation = Double.parseDouble(coords[2]);
    }

    public Position(BinaryDeserializationContext context) {
        latitude = context.readDouble();
        longitude = context.readDouble();
        elevation = context.readDouble();
    }

    /** Latitude in degrees. */
    public double getLatitude() {
        return latitude;
    }

    /** Latitude in radians. */
    public double getLatitudeRadians() {
        return Convert.degreesToRadians(latitude);
    }

    /** Longitude in degrees. */
    public double getLongitude() {
        return longitude;
    }

    /** Longitude in radians. */
    public double getLongitudeRadians() {
        return Convert.degreesToRadians(longitude);
    }

    /** Elevation above the WGS84 ellipsoid, in meters. */
    public double getElevation() {
        return elevation;
    }

    public Mat4 computeEllipsoidalOrientation() {
        double radLat = Convert.degreesToRadians(latitude);
        double radLon = Convert.degreesToRadians(longitude);
        Vec3 point = Convert.geodeticToEcef(radLat, radLon, elevation);

        // Transform to the cartesian coordinates of (latitude, longitude, metersElevation).
        Mat4 transform = Mat4.fromTranslation(point);

        // Rotate the coordinate system to match the longitude.
        // Longitude is treated as counter-clockwise rotation about the Y-axis.
        transform.multiplyInplace(Mat4.fromRotationY(radLon));

        // Rotate the coordinate system to match the latitude.
        // Latitude is treated clockwise as rotation about the X-axis. We flip the latitude value so that a positive
        // rotation produces a clockwise rotation (when facing the axis).
        return transform.multiplyInplace(Mat4.fromRotationX(-radLat));
    }

    @Override
    public void serialize(PrimitiveSerializationContext context) {
        context.write(latitude + "," + longitude + "," + elevation);
    }

    @Override
    public void serialize(BinarySerializationContext context) {
        context.writeDouble(latitude);
        context.writeDouble(longitude);
        context.writeDouble(elevation);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(latitude) + 31 * Double.hashCode(longitude) + 31 * Double.hashCode(elevation);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) {
            return false;
        }

        Position other = (Position)obj;
        return latitude == other.latitude && longitude == other.longitude && elevation == other.elevation;
    }

    @Override
    public String toString() {
        return latitude + "; " + longitude + "; " + elevation;
    }

}
