/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geospatial;

import com.intel.missioncontrol.serialization.BinaryDeserializationContext;
import com.intel.missioncontrol.serialization.BinarySerializable;
import com.intel.missioncontrol.serialization.BinarySerializationContext;
import com.intel.missioncontrol.serialization.PrimitiveDeserializationContext;
import com.intel.missioncontrol.serialization.PrimitiveSerializable;
import com.intel.missioncontrol.serialization.PrimitiveSerializationContext;

/** Describes a WGS84 position on the globe. */
public class LatLon implements PrimitiveSerializable, BinarySerializable {

    private final double latitude;
    private final double longitude;

    public static LatLon fromRadians(double latitude, double longitude) {
        return new LatLon(Convert.radiansToDegrees(latitude), Convert.radiansToDegrees(longitude));
    }

    public static LatLon fromDegrees(double latitude, double longitude) {
        return new LatLon(latitude, longitude);
    }

    private LatLon(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLon(PrimitiveDeserializationContext context) {
        String[] coords = context.read().split(",");
        latitude = Double.parseDouble(coords[0]);
        longitude = Double.parseDouble(coords[1]);
    }

    public LatLon(BinaryDeserializationContext context) {
        latitude = context.readDouble();
        longitude = context.readDouble();
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

    @Override
    public void serialize(PrimitiveSerializationContext context) {
        context.write(latitude + "," + longitude);
    }

    @Override
    public void serialize(BinarySerializationContext context) {
        context.writeDouble(latitude);
        context.writeDouble(longitude);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(latitude) + 31 * Double.hashCode(longitude);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LatLon)) {
            return false;
        }

        LatLon other = (LatLon)obj;
        return latitude == other.latitude && longitude == other.longitude;
    }

    @Override
    public String toString() {
        return latitude + "; " + longitude;
    }

}
