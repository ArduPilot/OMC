/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import gov.nasa.worldwind.geom.LatLon;

public class ElevationModelRequestException extends Exception {

    public static final String KEY = "com.intel.missioncontrol.map.elevation.ElevationModelRequestException";

    private static final long serialVersionUID = -4364931387231008560L;

    /** as bigger than one the value is, as worse the result is! */
    public final double resolutionMissmatch;

    /** best possible guess for altitude */
    public final double achievedAltitude;

    /** is true if the worst possible resolution (none) is archived */
    public final boolean isWorst;

    public final double achivedResMeter;
    public final LatLon location;

    ElevationModelRequestException(
            double resolutionMissmatch, double archivedAltitude, double archivedResMeter, LatLon location) {
        if (resolutionMissmatch == Double.POSITIVE_INFINITY) {
            resolutionMissmatch = Double.MAX_VALUE;
        }

        this.resolutionMissmatch = resolutionMissmatch;
        this.achivedResMeter = archivedResMeter;
        this.location = location;
        isWorst = (resolutionMissmatch == Double.MAX_VALUE);
        if (isWorst) {
            this.achievedAltitude = 0;
        } else {
            this.achievedAltitude = archivedAltitude;
        }
    }

    ElevationModelRequestException(LatLon location) {
        this.location = location;
        isWorst = true;
        resolutionMissmatch = Double.MAX_VALUE;
        achievedAltitude = 0;
        achivedResMeter = Double.MAX_VALUE;
    }

    @Override
    public String toString() {
        return "achievedAltitude:"
            + achievedAltitude
            + " resolutionMissmatch:"
            + resolutionMissmatch
            + " isWorst:"
            + isWorst
            + " achivedResMeter:"
            + achivedResMeter
            + " latLon:"
            + location
            + "  "
            + super.toString();
    }

}
