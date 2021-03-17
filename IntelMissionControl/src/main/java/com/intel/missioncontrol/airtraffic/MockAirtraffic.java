/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic;

import com.intel.insight.datastructures.Feature;
import com.intel.missioncontrol.airtraffic.dto.AirtrafficObject;
import com.intel.missioncontrol.airtraffic.dto.AirtrafficObjectAltitude;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MockAirtraffic implements IAirtrafficService {
    @Override
    public List<AirtrafficObject> getTraffic(double latitude, double longitude, double radius, double time) {
        var aoList = new ArrayList<AirtrafficObject>();
        Feature pos = new Feature();
        Feature pos2 = new Feature();

        // pos.getGeometry().setCoordinates(); = LatLon.fromDegrees(latitude, longitude);
        // pos2.coordinates = LatLon.fromDegrees(latitude + 0.0001, longitude);

        var date1 = new Date();
        var date2 = new Date();

        AirtrafficObjectAltitude alt = new AirtrafficObjectAltitude(1000, 1000);
        AirtrafficObjectAltitude alt2 = new AirtrafficObjectAltitude(1000, 1000);

        // AittrafficObjectProperties prop = new AittrafficObjectProperties(alt, 10, "1", "1", "test1", date1, 1);
        // AittrafficObjectProperties prop2 = new AittrafficObjectProperties(alt2, 100, "22", "1", "test123", date2, 1);

        // var ao1 = new AirtrafficObject(pos, 1, prop);
        // var ao2 = new AirtrafficObject(pos2, 2, prop2);
        // aoList.add(ao1);
        // aoList.add(ao2);
        return aoList;
    }

    @Override
    public String publishUavPosition(double lon, double lat, int baroAltitude, int courseOverGround, int idType, String identifier, boolean onGround, int speedOverGround, String timestamp, int type, double verticalSpeed, int wgs84Altitude) {
        return "nope";
    }

    // @Test
    public void Mock_Test() {
        var m = new MockAirtraffic();
        var res = m.getTraffic(10, 10, 100, 10);
        System.out.println(res);
    }
}
