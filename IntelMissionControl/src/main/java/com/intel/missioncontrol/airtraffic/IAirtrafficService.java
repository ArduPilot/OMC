/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic;

import com.intel.missioncontrol.airtraffic.dto.AirtrafficObject;
import java.util.List;

public interface IAirtrafficService {
    /**
     * Returns all airtraffic objects around a "drone" at latitude,longitude in the radius of  radius meters in the timeframe of
     * time
     *
     * @param latitude
     * @param longitude
     * @param radius    0 < value < 20000, Radius in meters around specified coordinate 'pos'.
     * @param time      Maximum age of airborne targets in seconds. If not specified, defaults to 15.
     * @return
     */
    public abstract List<AirtrafficObject> getTraffic(double latitude, double longitude, double radius, double time);

    /**
     * @param lon
     * @param lat
     * @param baroAltitude     Barometric altitude in meters above 1013.25hPa
     * @param courseOverGround True course over ground in degree between 0 and 360
     * @param idType           0, 1, 2, 3, 4 for unknown, icao_addr, non_icao, flarm, infrastructure
     * @param identifier       Unique identifier dependent on 'idType', e.g. six-digit hex ICAO address if 'idType' is 1
     * @param onGround         Whether target is on ground or not. Should be used if target is on ground.
     * @param speedOverGround  Speed over ground in m/s
     * @param timestamp        Timestamp as ISO8610 strings in UTC or empty string for now()
     * @param type             The aircraft's type or category, 0 if not set.
     * @param verticalSpeed    Vertical speed in m/s.
     * @param wgs84Altitude    GNSS altitude in meters above WGS84.
     * @return
     */
    public abstract String publishUavPosition(double lon,
                                              double lat,
                                              int baroAltitude,
                                              int courseOverGround,
                                              int idType,
                                              String identifier,
                                              boolean onGround,
                                              int speedOverGround,
                                              String timestamp,
                                              int type,
                                              double verticalSpeed,
                                              int wgs84Altitude);
}
