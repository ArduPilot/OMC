/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap.data;

import com.airmap.airmapsdk.networking.services.MappingService.AirMapAirspaceType;
import gov.nasa.worldwind.geom.LatLon;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class AirSpaceObject {
    public UUID id;
    public String name;
    public String type;

    public String country;
    public String state;
    public String city;

    public double latitude;
    public double longitude;

    public GeoJson.GeometryExtended geometry;

    private LatLon latLon;

    public LatLon getPosition() {
        if (latLon == null && latitude != 0 && longitude != 0) {
            latLon = LatLon.fromDegrees(latitude, longitude);
        }

        return latLon;
    }

    public double min_circle_radius;

    public Date last_updated;

    private AirMapAirspaceType typeCache;

    public AirMapAirspaceType getType() {
        if (typeCache == null && type != null) {
            typeCache = AirMapAirspaceType.fromString(type);
        }

        return typeCache;
    }

    private int hash;

    @Override
    public int hashCode() {
        if (hash == 0 && id != null) {
            hash = Objects.hashCode(id);
        }

        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AirSpaceObject that = (AirSpaceObject)o;
        return Objects.equals(id, that.id);
    }

    @Override
    public String toString() {
        return "Airspace[type=" + type + " name=" + name + " id=" + id + "]";
    }
}
