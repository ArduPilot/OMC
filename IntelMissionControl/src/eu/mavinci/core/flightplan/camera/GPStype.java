/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.camera;

import eu.mavinci.core.flightplan.GPSFixType;

/** which curve is the easiest one.. */
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public enum GPStype {
    GPS,
    DGPS,
    DGPS_RTK,
    ILT;

    public double getAccuracy() {
        switch (this) {
        case DGPS:
            return 0.5;
        case DGPS_RTK:
            return 0.03;
        default:
            return 2.0;
        }
    }

    public GPSFixType getBestFixType() {
        switch (this) {
        case DGPS:
            return GPSFixType.dgps;
        case DGPS_RTK:
            return GPSFixType.rtkFixedBL;
        default:
            return GPSFixType.gpsFix;
        }
    }
}
