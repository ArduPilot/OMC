/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.geospatial;

import com.intel.missioncontrol.geometry.Mat4;
import com.intel.missioncontrol.geometry.Vec3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GeometryTest {
    @Test
    public void basic_geom_understanding_works() {
        // origin
        Position pos1 = Position.fromRadians(Convert.degreesToRadians(40.00), Convert.degreesToRadians(10.00), 0);

        Position pos2 = Position.fromRadians(Convert.degreesToRadians(40.01), Convert.degreesToRadians(10.01), 0);
        Position pos3 = Position.fromRadians(Convert.degreesToRadians(40.01), Convert.degreesToRadians(10.00), 0);
        Position pos4 = Position.fromRadians(Convert.degreesToRadians(40.00), Convert.degreesToRadians(10.01), 0);

        Vec3 vec1 = Convert.geodeticToEcef(pos1.getLatitude(), pos1.getLongitude(), 0);
        Vec3 vec2 = Convert.geodeticToEcef(pos2.getLatitude(), pos2.getLongitude(), 0);
        Vec3 vec3 = Convert.geodeticToEcef(pos3.getLatitude(), pos3.getLongitude(), 0);
        Vec3 vec4 = Convert.geodeticToEcef(pos4.getLatitude(), pos4.getLongitude(), 0);

        Mat4 toGlobe = pos1.computeEllipsoidalOrientation();
        Mat4 toPos1CoordSystem = toGlobe.invert();

        Vec3 pos1_Local = vec1.transform(toPos1CoordSystem);
        Vec3 pos2_Local = vec2.transform(toPos1CoordSystem);
        Vec3 pos3_Local = vec3.transform(toPos1CoordSystem);
        Vec3 pos4_Local = vec4.transform(toPos1CoordSystem);

        Vec3 reverseVec1 = pos1_Local.transform(toGlobe);
        Vec3 reverseVec2 = pos2_Local.transform(toGlobe);
        Vec3 reverseVec3 = pos3_Local.transform(toGlobe);
        Vec3 reverseVec4 = pos4_Local.transform(toGlobe);

        Position pos1Restored = Convert.ecefToGeodetic(reverseVec1);
        Position pos2Restored = Convert.ecefToGeodetic(reverseVec2);
        Position pos3Restored = Convert.ecefToGeodetic(reverseVec3);
        Position pos4Restored = Convert.ecefToGeodetic(reverseVec4);

        Vec3 reverseVec1_ = (new Vec3(pos1_Local.x, pos1_Local.y, 0)).transform(toGlobe);
        Vec3 reverseVec2_ = (new Vec3(pos2_Local.x, pos2_Local.y, 0)).transform(toGlobe);
        Vec3 reverseVec3_ = (new Vec3(pos3_Local.x, pos3_Local.y, 0)).transform(toGlobe);
        Vec3 reverseVec4_ = (new Vec3(pos4_Local.x, pos4_Local.y, 0)).transform(toGlobe);

        Position pos1Restored_ = Convert.ecefToGeodetic(reverseVec1_);
        Position pos2Restored_ = Convert.ecefToGeodetic(reverseVec2_);
        Position pos3Restored_ = Convert.ecefToGeodetic(reverseVec3_);
        Position pos4Restored_ = Convert.ecefToGeodetic(reverseVec4_);

        Assertions.assertTrue(pos1Restored.getLatitude() - pos1Restored_.getLatitude() < 1e-8);
        Assertions.assertTrue(pos1Restored.getLongitude() - pos1Restored_.getLongitude() < 1e-8);

        Assertions.assertTrue(pos2Restored.getLatitude() - pos2Restored_.getLatitude() < 1e-8);
        Assertions.assertTrue(pos2Restored.getLongitude() - pos2Restored_.getLongitude() < 1e-8);

        Assertions.assertTrue(pos3Restored.getLatitude() - pos3Restored_.getLatitude() < 1e-8);
        Assertions.assertTrue(pos3Restored.getLongitude() - pos3Restored_.getLongitude() < 1e-8);

        Assertions.assertTrue(pos4Restored.getLatitude() - pos4Restored_.getLatitude() < 1e-8);
        Assertions.assertTrue(pos4Restored.getLongitude() - pos4Restored_.getLongitude() < 1e-8);
    }

    public double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        var R = GeoMath.WGS84_EQUATORIAL_RADIUS; // Radius of the earth in m
        var dLat = deg2rad(lat2 - lat1); // deg2rad below
        var dLon = deg2rad(lon2 - lon1);
        var a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        var d = R * c;
        return d;
    }

    public double deg2rad(double deg) {
        return deg * (Math.PI / 180.0);
    }

    /*public LatLon shiftCoordinate(double lat, double lon, double dn, double de) {
        var R = GeoMath.WGS84_EQUATORIAL_RADIUS;
        var dLat = dn / R;
        var dLon = de / (R * Math.cos(deg2rad(lat)));

        // OffsetPosition, decimal degrees
        var latO = lat + dLat * 180.0 / Math.PI;
        var lonO = lon + dLon * 180.0 / Math.PI;

        return new LatLon(Transform.degreesToRadians(latO), Transform.degreesToRadians(lonO));
    }*/

}
