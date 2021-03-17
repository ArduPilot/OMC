/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public abstract class AWaypoint extends ReentryPoint implements IFlightplanStatement, IFlightplanPositionReferenced {

    protected double lon;
    protected double lat;
    protected int alt; // altitude in cm over starting point

    public static final int ALTITUDE_MAX_WITHIN_CM = 20000000;
    public static final int ALTITUDE_MIN_WITHIN_CM = -3000000;

    public static final double ALTITUDE_MAX_WITHIN_M = ALTITUDE_MAX_WITHIN_CM / 100d;
    public static final double ALTITUDE_MIN_WITHIN_M = ALTITUDE_MIN_WITHIN_CM / 100d;

    public static final int DEFAULT_ALT_WITHIN_CM = 10000;
    public static final double DEFAULT_ALT_WITHIN_M = DEFAULT_ALT_WITHIN_CM / 100.;

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        if (this.lon != lon) {
            this.lon = lon;
            informChangeListener();
        }
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        if (this.lat != lat) {
            this.lat = lat;
            informChangeListener();
        }
    }

    public double getAltInMAboveFPRefPoint() {
        return (double)(alt) / 100.d;
    }

    public int getAltWithinCM() {
        return alt;
    }

    public void setAltInMAboveFPRefPoint(double alt) {
        setAltWithinCM((int)Math.round(alt * 100));
    }

    public void setAltWithinM(float alt) {
        setAltWithinCM(Math.round(alt * 100));
    }

    public void setAltWithinCM(int alt) {
        if (alt > ALTITUDE_MAX_WITHIN_CM) {
            alt = ALTITUDE_MAX_WITHIN_CM;
        }

        if (alt < ALTITUDE_MIN_WITHIN_CM) {
            alt = ALTITUDE_MIN_WITHIN_CM;
        }

        if (this.alt != alt) {
            this.alt = alt;
            // CDebug.getLog().log(Level.FINE,"set alt. of waypoint " + this + " neaAlt:"+alt,new Exception());
            informChangeListener();
        }
    }

    protected AWaypoint(double lon, double lat, double altWithinM, int id, IFlightplanContainer parent) {
        super(parent, id);
        setLatLon(lat, lon);
        setAltInMAboveFPRefPoint(altWithinM);
    }

    protected AWaypoint(double lon, double lat, int altWithinCM, int id, IFlightplanContainer parent) {
        super(parent, id);
        setLatLon(lat, lon);
        setAltWithinCM(altWithinCM);
    }

    protected AWaypoint(double lon, double lat, double altWithinM, IFlightplanContainer parent) {
        super(parent);
        setLatLon(lat, lon);
        setAltInMAboveFPRefPoint(altWithinM);
    }

    protected AWaypoint(double lon, double lat, int altWithinCM, IFlightplanContainer parent) {
        super(parent);
        setLatLon(lat, lon);
        setAltWithinCM(altWithinCM);
    }

    protected AWaypoint(double lon, double lat, int altWithinCM, int id) {
        super(id);
        setLatLon(lat, lon);
        setAltWithinCM(altWithinCM);
    }

    protected AWaypoint(double lon, double lat, IFlightplanContainer parent) {
        super(parent);
        setLatLon(lat, lon);
        setAltInMAboveFPRefPoint(DEFAULT_ALT_WITHIN_M);
    }

    public String toString() {
        return "AWaypoint"; // TODO more useful name
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof AWaypoint) {
            AWaypoint wp = (AWaypoint)o;
            return id == wp.id && lon == wp.lon && lat == wp.lat && alt == wp.alt;
        }

        return false;
    }

    public boolean isStickingToGround() {
        return false;
    }

    public void setLatLon(double lat, double lon) {
        if (this.lat != lat || this.lon != lon) {
            this.lat = lat;
            this.lon = lon;
            informChangeListener();
        }
    }

    public int getAlt() {
        return alt;
    }

    public void setAlt(int alt) {
        this.alt = alt;
    }

}
