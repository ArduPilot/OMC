/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airtraffic.dto;

/*
Example:
            "altitude": {
                "barometric": 983,
                "wgs84": 1044
            }
 */
public class AirtrafficObjectAltitude {
    double barometric;
    double wgs84;

    public AirtrafficObjectAltitude(double barometric, double wgs84) {
        this.barometric = barometric;
        this.wgs84 = wgs84;
    }

    @Override
    public String toString() {
        return "AirtrafficObjectAltitude{" +
                "barometric=" + barometric +
                ", wgs84=" + wgs84 +
                '}';
    }

    public double getBarometric() {
        return barometric;
    }

    public double getWgs84() {
        return wgs84;
    }
}
