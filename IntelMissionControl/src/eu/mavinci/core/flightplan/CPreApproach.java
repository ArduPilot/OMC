/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public abstract class CPreApproach extends AWaypoint {

    public static final int DEFAULT_ALT_WITHIN_CM = 5000;
    public static final double DEFAULT_ALT_WITHIN_M = DEFAULT_ALT_WITHIN_CM / 100.;

    protected CPreApproach(double lon, double lat, double altWithinM, IFlightplanContainer parent) {
        super(lon, lat, altWithinM, parent);
    }

    protected CPreApproach(double lon, double lat, double altWithinM, int id, IFlightplanContainer parent) {
        super(lon, lat, altWithinM, id, parent);
    }

    protected CPreApproach(double lon, double lat, IFlightplanContainer parent) {
        super(lon, lat, parent);
    }

    protected CPreApproach(double lon, double lat, int altWithinCM, IFlightplanContainer parent) {
        super(lon, lat, altWithinCM, parent);
    }

    protected CPreApproach(double lon, double lat, int altWithinCM, int id, IFlightplanContainer parent) {
        super(lon, lat, altWithinCM, id, parent);
    }

    protected CPreApproach(double lon, double lat, int altWithinCM, int id) {
        super(lon, lat, altWithinCM, id);
    }

    public String toString() {
        return "PreApproach"; // TODO more useful name
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof CPreApproach) {
            CPreApproach pa = (CPreApproach)o;
            return super.equals(pa);
        }

        return false;
    }

}
