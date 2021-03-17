/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.flightplan.LandingPoint;

public abstract class CStartProcedure extends ReentryPoint
        implements IFlightplanPositionReferenced, IFlightplanDeactivateable {

    public static int PHOTO_DistanceInCm = Integer.MAX_VALUE;

    // in cm
    protected int altitude = LandingPoint.LandingCircleAltDef;
    protected boolean hasOwnAltitude;

    protected boolean isActive = true;

    public CStartProcedure(IFlightplanContainer parent, int id) {
        super(parent, id);
    }

    public CStartProcedure(IFlightplanContainer parent) {
        super(parent);
    }

    public CStartProcedure(int id) {
        super(id);
    }

    public boolean hasOwnAltitude() {
        return hasOwnAltitude;
    }

    public void setHasOwnAltitude(boolean hasOwnAltitude) {
        if (this.hasOwnAltitude == hasOwnAltitude) {
            return;
        }

        this.hasOwnAltitude = hasOwnAltitude;
        informChangeListener();
    }

    public int getAltWithinCM() {
        if (hasOwnAltitude) {
            return altitude;
        } else {
            return getLandingpoint().getAltWithinCM();
        }
    }

    public void setActive(boolean active) {
        if (this.isActive == active) {
            return;
        }

        isActive = active;
        informChangeListener();
    }

    public boolean isActive() {
        /*CFlightplan fp = getFlightplan();
        if(fp == null) {*/
        return isActive && getLandingpoint().isActive();
        /*} else {
            return isActive && !fp.isMeta(); //this caused a stack overflow exception
        }*/
    }

    public double getAltInMAboveFPRefPoint() {
        return getAltWithinCM() / 100.;
    }

    public void setAltWithinMforwarding(double altitude) {
        if (hasOwnAltitude) {
            setAltInMAboveFPRefPoint(altitude);
        } else {
            getLandingpoint().setAltInMAboveFPRefPoint(altitude);
        }
    }

    public void setAltInMAboveFPRefPoint(double altitude) {
        setAltitude((int)Math.round(altitude * 100));
    }

    public void setAltitude(int altitude) {
        if (altitude > CWaypoint.ALTITUDE_MAX_WITHIN_CM) {
            altitude = CWaypoint.ALTITUDE_MAX_WITHIN_CM;
        }

        if (altitude < CWaypoint.ALTITUDE_MIN_WITHIN_CM) {
            altitude = CWaypoint.ALTITUDE_MIN_WITHIN_CM;
        }

        if (this.altitude != altitude) {
            this.altitude = altitude;
            informChangeListener();
        }
    }

    @Override
    public CStartProcedure clone() {
        CStartProcedure start = FlightplanFactory.getFactory().newCStartProcedure(id);
        start.altitude = altitude;
        start.hasOwnAltitude = hasOwnAltitude;
        start.isActive = isActive;
        return start;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof CStartProcedure) {
            CStartProcedure start = (CStartProcedure)o;
            return id == start.id
                && hasOwnAltitude == start.hasOwnAltitude
                && (!hasOwnAltitude || altitude == start.altitude)
                && isActive == start.isActive;
        } else {
            return false;
        }
    }

    public LandingPoint getLandingpoint() {
        CFlightplan cflightplan = getFlightplan();
        Ensure.notNull(cflightplan, "cflightplan");
        return cflightplan.getLandingpoint();
    }

    public double getLon() {
        return getLandingpoint().getLon();
    }

    public void setLon(double lon) {
        getLandingpoint().setLon(lon);
    }

    public double getLat() {
        return getLandingpoint().getLat();
    }

    public void setLat(double lat) {
        getLandingpoint().setLat(lat);
    }

    public void setLatLon(double lat, double lon) {
        getLandingpoint().setLatLon(lat, lon);
    }

    public boolean isStickingToGround() {
        return false;
    }

    @Override
    public void reassignIDs() {
        IFlightplanContainer parent = getParent();
        if (parent != null) {
            id = -1;
            setId(ReentryPointID.createNextSideLineID(parent.getFlightplan().getMaxUsedId()));
        }
    }
}
