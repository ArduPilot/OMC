/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.utils;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import java.util.List;

public class GeoUtils {

    public static LatLon computeCenter(List<? extends LatLon> corners) {
        LatLon result = null;
        if (!corners.isEmpty()) {
            if (corners.size() > 1) {
                Sector sector = Sector.boundingSector(corners);
                result = sector.getCentroid();
            } else {
                result = corners.get(0);
            }
        }

        return result;
    }
}
