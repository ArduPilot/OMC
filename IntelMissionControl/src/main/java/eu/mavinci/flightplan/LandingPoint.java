/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanDeactivateable;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IRecalculateable;
import eu.mavinci.core.flightplan.IReentryPoint;
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.core.flightplan.ReentryPoint;
import eu.mavinci.core.flightplan.ReentryPointID;
import eu.mavinci.core.flightplan.visitors.LastWaypointVisitor;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.desktop.helper.MathHelper;
import eu.mavinci.geo.IPositionReferenced;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

public final class LandingPoint extends Point
        implements IPositionReferenced,
            IFlightplanPositionReferenced,
            IReentryPoint,
            IRecalculateable,
            IFlightplanDeactivateable {

    public static final LandingModes DEFAULT_LANDING_MODES = LandingModes.LAND_AT_TAKEOFF;

    private int id = ReentryPoint.INVALID_REENTRYPOINT;
    private LandingModes mode = DEFAULT_LANDING_MODES;
    private double altitudeInMeter = 50;

    private boolean landAutomatically;

    public LandingPoint(Flightplan fp, double lat, double lon, LandingModes mode, int id) {
        super(fp, lat, lon);
        this.mode = mode;
        this.id = id;
        reassignIDs();
    }

    public LandingPoint(double lat, double lon, LandingModes mode) {
        super(lat, lon);
        this.mode = mode;
        reassignIDs();
    }

    public LandingPoint(Flightplan fp, double lat, double lon) {
        super(fp, lat, lon);
        reassignIDs();
    }

    public LandingPoint(CFlightplan fp) {
        super(fp);
        reassignIDs();
    }

    public LandingPoint(double lat, double lon) {
        super(lat, lon);
        reassignIDs();
    }

    public LandingPoint(LandingPoint source) {
        super(source);
        this.id = source.id;
        this.mode = source.mode;
        this.landAutomatically = source.landAutomatically;
        this.altitudeInMeter = source.altitudeInMeter;
        reassignIDs();
    }

    @Override
    public void reassignIDs() {
        IFlightplanContainer parent = getParent();
        if (parent != null) {
            id = -1;
            CFlightplan pFlightP = parent.getFlightplan();
            Ensure.notNull(pFlightP, "pFlightP");
            setId(ReentryPointID.createNextSideLineID(pFlightP.getMaxUsedId()));
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        if (this.id != id) {
            this.id = id;
            notifyStatementChanged();
        }
    }

    @Override
    public boolean isStickingToGround() {
        return false;
    }

    public LandingModes getMode() {
        return mode;
    }

    public void setLatLon(LatLon latLon) {
        setLatLon(latLon.getLatitude().degrees, latLon.getLongitude().degrees);
    }

    @Override
    public String toString() {
        return "LandingPoint:" + lat + "  " + lon + " " + mode + " " + altitudeInMeter + " " + landAutomatically;
    }

    @Override
    public LatLon getLatLon() {
        return new LatLon(Angle.fromDegreesLatitude(lat), Angle.fromDegreesLongitude(lon));
    }

    public void setFromCurrentAirplane(IAirplane plane) throws AirplaneCacheEmptyException {
        // shifting the position sidewards by circle radius
        CFlightplan flightP = getFlightplan();
        Ensure.notNull(flightP, "flightP");
        LatLon latLon = plane.getAirplaneCache().getCurLatLon();
        if (mode == LandingModes.CUSTOM_LOCATION) {
            setLatLon(latLon);
        }
    }

    @Override
    public boolean doSubRecalculationStage1() {
        return true;
    }

    @Override
    public boolean doSubRecalculationStage2() {
        switch (mode) {
        case LAST_WAYPOINT:
            LastWaypointVisitor vis = new LastWaypointVisitor();
            vis.startVisit(getFlightplan());
            if (vis.lastWaypoint != null) {
                setLatLon(vis.lastWaypoint.getLat(), vis.lastWaypoint.getLon());
            }

            break;
        case CUSTOM_LOCATION:
            // nothing to do
            break;

        case LAND_AT_TAKEOFF:
            setLatLon(getFlightplan().getTakeoff().getLatLon());
            break;
        }

        return true;
    }

    public void updateFromUAV(IAirplane plane) throws AirplaneCacheEmptyException {
        /** TODO implement for IDL */
    }

    @Override
    public Position getPosition() {
        return new Position(getLatLon(), altitudeInMeter);
    }

    public void setMode(LandingModes mode) {
        if (this.mode != mode) {
            this.mode = mode;
            notifyStatementChanged();
        }
    }

    public boolean isEmpty() {
        return lat == 0.0d && lon == 0.0d;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof LandingPoint) {
            LandingPoint landingpoint = (LandingPoint)o;
            return lat == landingpoint.lat
                && lon == landingpoint.lon
                && mode == landingpoint.mode
                && id == landingpoint.id
                && landAutomatically == landingpoint.landAutomatically
                && altitudeInMeter == landingpoint.altitudeInMeter;
        } else {
            return false;
        }
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new LandingPoint(this);
    }

    @Override
    public void setActive(boolean active) {}

    public boolean isActive() {
        CFlightplan fp = getFlightplan();
        Ensure.notNull(fp);
        if (MathHelper.isDifferenceTiny(lat, 0) && MathHelper.isDifferenceTiny(lon, 0)) {
            return false;
        }

        return true;
    }

    public boolean isLandAutomatically() {
        return landAutomatically;
    }

    public boolean setLandAutomatically(boolean landAutomatically) {
        if (this.landAutomatically == landAutomatically) {
            return false;
        }

        this.landAutomatically = landAutomatically;
        notifyStatementChanged();
        return true;
    }

    @Override
    public void setAltInMAboveFPRefPoint(double altInM) {
        altInM = MathHelper.intoRange(altInM, CWaypoint.ALTITUDE_MIN_WITHIN_M, CWaypoint.ALTITUDE_MAX_WITHIN_M);

        if (this.altitudeInMeter != altInM) {
            this.altitudeInMeter = altInM;
            notifyStatementChanged();
        }
    }

    @Override
    public double getAltInMAboveFPRefPoint() {
        return altitudeInMeter;
    }
}
