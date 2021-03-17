/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic.dto;

import java.util.Date;

/*
Example:
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


        Regarding the type, we obtained a table from air avionics

        # type:
# -----
#       |                                                   | defined   |
# id    | name                                              | in DO-260 | FLARM mapping
# =====================================================================================
# 0     | NO_CATEGORY_INFO                                  | y         | all unknown types
# 1     | LIGHT_ACFT (< 15500 lbs)                          | y         | MotorPlane, DropPlane, TowPlane
# 2     | SMALL_ACFT (15500 to 75000 lbs)                   | y         | JetPlane
# 3     | MEDIUM_ACFT (75000 to 300000 lbs) (DO-260)        | y         |
# 4     | HIGH_VORTEX_ACFT (aircraft such as B-757)         | y         |
# 5     | HEAVY_ACFT (> 300000 lbs)                         | y         |
# 6     | HIGH_PERFORMANCE_ACFT (>5g accel, > 400 kn cruise)| y         |
# 10    | ROTORCRAFT                                        | y         |
# 11    | GLIDER                                            | y         | Glider
# 12    | LIGHTER_THAN_AIR                                  | y         |
#       | BALLOON                                           | n         | Balloon
#       | AIRSHIP                                           | n         | Airship
# 13    | UNMANNED_AERIAL_VEHICLE                           | y         | UAV
#       | LIGHT_COPTER_UAV                                  | n         |
#       | MEDIUM_COPTER_UAV                                 | n         |
#       | HEAVY_COPTER_UAV                                  | n         |
#       | LIGHT_HELI_UAV                                    | n         |
#       | MEDIUM_HELI_UAV                                   | n         |
#       | HEAVY_HELI_UAV                                    | n         |
#       | LIGHT_FIXEDWING_UAV                               | n         |
#       | MEDIUM_FIXEDWING_UAV                              | n         |
#       | HEAVY_FIXEDWING_UAV                               | n         |
# 14    | SPACE_VEHICLE                                     | y         |
# 15    | ULTRALIGHT_PARAGLIDER                             | y         |
#       | ULTRALIGHT_ACFT                                   | n         |
#       | PARAGLIDER                                        | n         | Paraglider
#       | HANGGLIDER                                        | n         | HangGlider
# 16    | SKYDIVER                                          | y         | Skydiver
#       | SFC_VEHICLE                                       | n         |
# 20    | SFC_EMERGENCY_VEHICLE                             | y         |
# 21    | SFC_SERVICE_VEHICLE                               | y         |
# 22    | POINT_OR_TETHERED_OBST (incl tethered balloons)   | y         |
# 23    | CLUSTER_OBSTACLE                                  | y         | StaticObject (tbd: map to POINT_OR_TETHERED_OBST?)
# 24    | LINE_OBSTACLE                                     | y         |
#

 */
public class AittrafficObjectProperties {

    /**
     * Barometric altitude in meters above 1013.25hPa.
     */
    int baroAltitude;
    int wgs84Altitude;
    /**
     * The distance in meters to this airborne target relative to the requested coordinates defined in the request via the 'pos' query parameter.
     */
    Double distanceTo;
    /**
     * The flight id or callsign.
     */
    String flightId;
    /**
     * 0: unknown, 1: ICAO, 2: NON_ICAO, 3: FLARM, 4: infrastructure
     */
    int idType;
    /**
     * Unique identifier dependent on 'idType', e.g. six-digit hex ICAO address if 'idType' is 1.
     */
    String identifier;
    Date time;
    /**
     * Whether target is on ground or not. Should be used if target is on ground. Assumes target is airborne if not set.
     */
    boolean onGround;
    /**
     * The aircraft's type or category, 0 if not set.
     */
    int type;
    /**
     * True course over ground in degree.
     */
    double courseOverGround;
    /**
     * in m/s
     */
    double speedOverGround;

    public boolean isOnGround() {
        return onGround;
    }

    public double getVerticalSpeed() {
        return verticalSpeed;
    }

    /**
     * in m/s
     */
    double verticalSpeed;

    public AittrafficObjectProperties(AirtrafficObjectAltitude altitude, Double distanceTo, String flightId, int idType, String identifier, Date time, int type, double courseOverGround, double speedOverGround) {
        this.distanceTo = distanceTo;
        this.flightId = flightId;
        this.idType = idType;
        this.identifier = identifier;
        this.time = time;
        this.type = type;
        this.courseOverGround = courseOverGround;
        this.speedOverGround = speedOverGround;
    }

    public AittrafficObjectProperties(int baroAltitude, int wgs84Altitude, Double distanceTo, String flightId, int idType, String identifier, Date time, boolean onGround, int type, double courseOverGround, double speedOverGround, double verticalSpeed) {
        this.baroAltitude = baroAltitude;
        this.wgs84Altitude = wgs84Altitude;
        this.distanceTo = distanceTo;
        this.flightId = flightId;
        this.idType = idType;
        this.identifier = identifier;
        this.time = time;
        this.onGround = onGround;
        this.type = type;
        this.courseOverGround = courseOverGround;
        this.speedOverGround = speedOverGround;
        this.verticalSpeed = verticalSpeed;
    }

    @Override
    public String toString() {
        return "AittrafficObjectProperties{" +
                "barometricAltitude=" + baroAltitude +
                ", wgs84Altitude=" + wgs84Altitude +
                ", distanceTo=" + distanceTo +
                ", flightId='" + flightId + '\'' +
                ", idType='" + idType + '\'' +
                ", identifier='" + identifier + '\'' +
                ", time=" + time +
                ", onGround=" + onGround +
                ", type=" + type +
                ", courseOverGround=" + courseOverGround +
                ", speedOverGround=" + speedOverGround +
                ", verticalSpeed=" + verticalSpeed +
                '}';
    }

    public int getBaroAltitude() {
        return baroAltitude;
    }

    public int getWgs84Altitude() {
        return wgs84Altitude;
    }

    public double getCourseOverGround() {
        return courseOverGround;
    }

    public double getSpeedOverGround() {
        return speedOverGround;
    }

    public Double getDistanceTo() {
        return distanceTo;
    }

    public String getFlightId() {
        return flightId;
    }

    public int getIdType() {
        return idType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Date getTime() {
        return time;
    }

    public int getType() {
        return type;
    }
}
