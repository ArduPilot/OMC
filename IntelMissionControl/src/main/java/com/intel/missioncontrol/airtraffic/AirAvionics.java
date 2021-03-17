/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intel.missioncontrol.airtraffic.dto.AirAvionicsResponse;
import com.intel.missioncontrol.airtraffic.dto.AirtrafficObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Airavionics implemention of the airtrafficservice. example traffic data returned from air avionics api [ {
 * "geometry": { "coordinates": [ 10, 49 ], "type": "Point" }, "id": 1521834006, "properties": { "altitude": {
 * "barometric": 983, "wgs84": 1044 }, "distanceTo": 1331.3319797323798, "flightId": "DLH223", "idType": 1,
 * "identifier": "3A82B0", "time": "2019-11-26T21:34:45.000Z", "type": 5 } } ]
 */
public class AirAvionics implements IAirtrafficService {

    private AirAvionicsClient client = new AirAvionicsClient();

    public AirAvionics() {}

    private static Gson gsonInstance() {
        return GsonHolder.INSTANCE;
    }

    /**
     * Given a @param jsonResponse from the AirAvionics around endpoint, parse it and return a list of air traffic
     * objects
     *
     * @param jsonResponse
     * @return
     */
    public static List<AirtrafficObject> parseJson(String jsonResponse) {
        var parsedAtos = gsonInstance().fromJson(jsonResponse, AirAvionicsResponse.class);
        var parsedAtosList = new ArrayList<AirtrafficObject>();
        for (AirtrafficObject parsedAto : parsedAtos.getFeatures()) {
            parsedAtosList.add(parsedAto);
        }

        return parsedAtosList;
    }

    /**
     * Returns all airtraffic objects around a "drone" at latitude,longitude in the radius of radius meters in the
     * timeframe of time
     *
     * @param latitude
     * @param longitude
     * @param radius 0 < value < 20000, Radius in meters around specified coordinate 'pos'.
     * @param time Maximum age of airborne targets in seconds. If not specified, defaults to 15.
     * @return
     */
    public List<AirtrafficObject> getTraffic(double latitude, double longitude, double radius, double time) {
        var resp = client.getTraffic(latitude, longitude, radius, time);
        var atos = parseJson(resp);
        return atos;
    }

    @Override
    public String publishUavPosition(double lon, double lat, int baroAltitude, int courseOverGround, int idType, String identifier, boolean onGround, int speedOverGround, String timestamp, int type, double verticalSpeed, int wgs84Altitude) {
        return client.publishUavPosition( lon, lat, baroAltitude, courseOverGround, idType, identifier, onGround, speedOverGround, timestamp, type, verticalSpeed, wgs84Altitude);
    }

    private static class GsonHolder {
        private static Gson INSTANCE = new GsonBuilder().create();
    }

}
