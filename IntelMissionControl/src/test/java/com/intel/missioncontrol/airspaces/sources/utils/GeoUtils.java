/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airspaces.sources.utils;

import gov.nasa.worldwind.geom.LatLon;

public class GeoUtils {
    // todo works for N/E directions only (but it's fine for the current tests as I don't match any S or W coordinates
    // yet)
    public static LatLon fromDegrees(String lat, String lon) {
        double latDegrees = Double.parseDouble(lat.split(":")[0]);
        double latMinutes = Double.parseDouble(lat.split(":")[1]);
        double latSeconds = Double.parseDouble(lat.split(":")[2]);

        double lonDegrees = Double.parseDouble(lon.split(":")[0]);
        double lonMinutes = Double.parseDouble(lon.split(":")[1]);
        double lonSeconds = Double.parseDouble(lon.split(":")[2]);

        return LatLon.fromDegrees(
            latDegrees + latMinutes / 60 + latSeconds / 3600, lonDegrees + lonMinutes / 60 + lonSeconds / 3600);
    }
}
