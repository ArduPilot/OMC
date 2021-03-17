/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import com.intel.missioncontrol.map.elevation.IElevationModel;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IMuteable;
import gov.nasa.worldwind.geom.LatLon;

// TODO ReferencePoint, Takeoff, LandingPoint and AOICorner should extend this
public class PointWithAltitudes implements IFlightplanPositionReferenced, IMuteable {

    protected double altInMAboveTakeoff;
    protected double altInMAboveReference;
    protected final Point point;

    public PointWithAltitudes(Point source) {
        this.point = source;
        updateAltitudes();
    }

    public PointWithAltitudes(double lat, double lon) {
        this.point = new Point(lat, lon);
    }

    public PointWithAltitudes(IFlightplanContainer container, double lat, double lon) {
        this.point = new Point(container, lat, lon);
    }

    public PointWithAltitudes(IFlightplanContainer container) {
        this.point = new Point(container);
    }

    @Override
    public void setAltInMAboveFPRefPoint(double altInM) {}

    @Override
    public double getAltInMAboveFPRefPoint() {
        updateAltitudes();
        return altInMAboveReference;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null) {
            return false;
        }

        if (o instanceof PointWithAltitudes) {
            PointWithAltitudes point = (PointWithAltitudes)o;
            return this.point.lat == point.point.lat && this.point.lon == point.point.lon;
        } else {
            return false;
        }
    }

    public boolean updateAltitudes() {
        double tmpAltRef = 0;
        double tmpAltTakeoff = 0;
        if (getFlightplan() != null) {
            double altWgs48 =
                DependencyInjector.getInstance()
                    .getInstanceOf(IElevationModel.class)
                    .getElevationAsGoodAsPossible(getLatLon());
            tmpAltTakeoff = altWgs48 - getFlightplan().getTakeofftAltWgs84WithElevation();
            tmpAltRef = altWgs48 - getFlightplan().getRefPointAltWgs84WithElevation();
        }

        boolean hasChanged = tmpAltTakeoff != altInMAboveTakeoff || tmpAltRef != altInMAboveReference;
        altInMAboveTakeoff = tmpAltTakeoff;
        altInMAboveReference = tmpAltRef;

        return hasChanged;
    }

    public double getAltInMAboveTakeoff() {
        updateAltitudes();
        return altInMAboveTakeoff;
    }

    @Override
    public double getLon() {
        return point.getLon();
    }

    @Override
    public void setLon(double lon) {
        point.setLon(lon);
    }

    @Override
    public double getLat() {
        return point.getLat();
    }

    @Override
    public void setLat(double lat) {
        point.setLat(lat);
    }

    @Override
    public void setLatLon(double lat, double lon) {
        point.setLatLon(lat, lon);
    }

    @Override
    public boolean isStickingToGround() {
        return point.isStickingToGround();
    }

    @Override
    public CFlightplan getFlightplan() {
        return point.getFlightplan();
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new PointWithAltitudes((Point)point.getCopy());
    }

    @Override
    public void setParent(IFlightplanContainer container) {
        point.setParent(container);
    }

    @Override
    public IFlightplanContainer getParent() {
        return point.getParent();
    }

    public Point getPoint() {
        return point;
    }

    @Override
    public void setMute(boolean mute) {
        point.setMute(mute);
    }

    @Override
    public void setSilentUnmute() {
        point.setSilentUnmute();
    }

    @Override
    public boolean isMute() {
        return point.isMute();
    }

    public LatLon getLatLon() {
        return point.getLatLon();
    }
}
