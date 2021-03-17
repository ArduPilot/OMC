/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanLatLonReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IMuteable;
import eu.mavinci.geo.ILatLonReferenced;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;

public class Point extends FlightplanStatement implements ILatLonReferenced, IFlightplanLatLonReferenced, IMuteable {

    public static final String KEY = "eu.mavinci.flightplan.Point";
    public static final String KEY_TO_STRING = KEY + ".toString";

    // public static final int DIGITS_LAT_LON_SHOWN = 5; //means a precision of at least 1.1m
    // public static final int DIGITS_LAT_LON_SHOWN = 9; //means a precision of at least 0.11mm
    public static final int DIGITS_LAT_LON_SHOWN = 8; // means a precision of at least 1.1mm

    protected double lat;
    protected double lon;

    public Point(Point source) {
        this.lat = source.lat;
        this.lon = source.lon;
    }

    public Point(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public Point(IFlightplanContainer container, double lat, double lon) {
        super(container);
        this.lat = lat;
        this.lon = lon;
    }

    public Point(IFlightplanContainer container) {
        super(container);
    }

    @Override
    public String toString() {
        return "Point:" + this.lat + " " + this.lon;
    }

    @Override
    public void setLat(double lat) {
        if (this.lat != lat) {
            this.lat = lat;

            notifyStatementChanged();
        }
    }

    @Override
    public void setLon(double lon) {
        if (this.lon != lon) {
            this.lon = lon;

            notifyStatementChanged();
        }
    }

    @Override
    public void setLatLon(double lat, double lon) {
        if (this.lat != lat || this.lon != lon) {
            this.lat = lat;
            this.lon = lon;

            notifyStatementChanged();
        }
    }

    @Override
    protected void notifyStatementChanged() {
        if (mute) {
            return;
        }

        super.notifyStatementChanged();
    }

    @Override
    public boolean isStickingToGround() {
        return true;
    }

    @Override
    public double getLon() {
        return this.lon;
    }

    @Override
    public double getLat() {
        return this.lat;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof Point) {
            Point landingPoint = (Point)o;
            return this.lat == landingPoint.lat && this.lon == landingPoint.lon;
        } else {
            return false;
        }
    }

    @Override
    public LatLon getLatLon() {
        return new LatLon(Angle.fromDegreesLatitude(this.lat), Angle.fromDegreesLongitude(this.lon));
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new Point(this);
    }

    boolean mute;

    @Override
    public void setMute(boolean mute) {
        if (mute == this.mute) {
            return;
        }

        this.mute = mute;
        if (!mute) {
            notifyStatementChanged();
        }
    }

    @Override
    public void setSilentUnmute() {
        this.mute = false;
    }

    @Override
    public boolean isMute() {
        return mute;
    }
}
