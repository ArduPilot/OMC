/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/*
Example response of an AirtrafficObject  @ 2019/12/4
 {
        "geometry": {
            "coordinates": [
                10,
                49
            ],
            "type": "Point"
        },
        "id": 1521834006,
        "properties": {
            "altitude": {
                "barometric": 983,
                "wgs84": 1044
            },
            "distanceTo": 1331.3319797323798,
            "flightId": "DLH223",
            "idType": 1,
            "identifier": "3A82B0",
            "time": "2019-11-26T21:34:45.000Z",
            "type": 5
        }
    }


 */
public class AirtrafficObject {
    @SerializedName("geometry")
    @Expose
    public Geometry geometry;
    Integer id;
    String type;

    public AirtrafficObject(Geometry geometry, Integer id, String type, AittrafficObjectProperties properties) {
        this.geometry = geometry;
        this.id = id;
        this.type = type;
        this.properties = properties;
    }

    AittrafficObjectProperties properties;
    public AirtrafficObject(Geometry geometry, Integer id, AittrafficObjectProperties properties) {
        this.geometry = geometry;
        this.id = id;
        this.properties = properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public String toString() {
        return "AirtrafficObject{" +
                "geometry=" + geometry +
                ", id=" + id +
                ", properties=" + properties +
                '}';
    }

    public String getType() {
        return type;
    }

    public Integer getId() {
        return id;
    }

    public AittrafficObjectProperties getProperties() {
        return properties;
    }
}
