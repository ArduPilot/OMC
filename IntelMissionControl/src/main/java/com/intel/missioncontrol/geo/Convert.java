/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geo;

import com.intel.missioncontrol.geospatial.LatLon;
import com.intel.missioncontrol.geospatial.Position;
import com.intel.missioncontrol.project.Sector;
import gov.nasa.worldwind.geom.Angle;
import java.util.List;

public class Convert {
    public static Position toCorePosition(gov.nasa.worldwind.geom.Position position) {
        return Position.fromRadians(position.latitude.radians, position.longitude.radians, position.elevation);
    }

    public static gov.nasa.worldwind.geom.Position toWWPosition(Position position) {
        return new gov.nasa.worldwind.geom.Position(
            Angle.fromDegrees(position.getLatitude()),
            Angle.fromDegrees(position.getLongitude()),
            position.getElevation());
    }

    public static Sector toCoreSector(gov.nasa.worldwind.geom.Sector sector) {
        return new Sector(
            sector.getMinLatitude().radians,
            sector.getMinLatitude().radians,
            sector.getMinLongitude().radians,
            sector.getMaxLongitude().radians);
    }

    public static gov.nasa.worldwind.geom.Sector toWWSector(Sector sector) {
        List<Position> corners = sector.getCorners();
        return new gov.nasa.worldwind.geom.Sector(
            Angle.fromRadians(corners.get(0).getLatitude()),
            Angle.fromRadians(corners.get(0).getLongitude()),
            Angle.fromRadians(corners.get(3).getLatitude()),
            Angle.fromRadians(corners.get(3).getLongitude()));
    }

    public static LatLon toCoreLatLon(gov.nasa.worldwind.geom.LatLon latlon) {
        return LatLon.fromDegrees(latlon.latitude.degrees, latlon.longitude.degrees);
    }

    public static gov.nasa.worldwind.geom.LatLon toWWLatLon(LatLon latlon) {
        return new gov.nasa.worldwind.geom.LatLon(
            Angle.fromRadians(latlon.getLatitude()), Angle.fromRadians(latlon.getLongitude()));
    }

}
