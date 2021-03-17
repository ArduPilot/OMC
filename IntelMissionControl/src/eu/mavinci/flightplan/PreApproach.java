/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import eu.mavinci.core.flightplan.CPreApproach;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.geo.IPositionReferenced;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

public class PreApproach extends CPreApproach implements IPositionReferenced {

    public static final String KEY = "eu.mavinci.flightplan.PreApproach";
    public static final String KEY_LAT = KEY + ".lat";
    public static final String KEY_LON = KEY + ".lon";
    public static final String KEY_ALT = KEY + ".alt";
    public static final String KEY_TO_STRING = KEY + ".toString";

    public PreApproach(double lon, double lat, double altWithinM, IFlightplanContainer parent) {
        super(lon, lat, altWithinM, parent);
    }

    public PreApproach(double lon, double lat, double altWithinM, int id, IFlightplanContainer parent) {
        super(lon, lat, altWithinM, id, parent);
    }

    public PreApproach(double lon, double lat, IFlightplanContainer parent) {
        super(lon, lat, parent);
    }

    public PreApproach(double lon, double lat, int altWithinCM, IFlightplanContainer parent) {
        super(lon, lat, altWithinCM, parent);
    }

    public PreApproach(double lon, double lat, int altWithinCM, int id, IFlightplanContainer parent) {
        super(lon, lat, altWithinCM, id, parent);
    }

    public PreApproach(double lon, double lat, int altWithinCM, int id) {
        super(lon, lat, altWithinCM, id);
    }

    public PreApproach(PreApproach source) {
        super(source.lon, source.lat, source.alt, source.id);
    }

    @Override
    public LatLon getLatLon() {
        return new LatLon(Angle.fromDegreesLatitude(lat), Angle.fromDegreesLongitude(lon));
    }

    /** returning the position of this waypoint, assuming the starting elevation is 0 */
    @Override
    public Position getPosition() {
        return new Position(getLatLon(), getAltInMAboveFPRefPoint());
    }

    @Override
    public String toString() {
        return "PreApproach:" + (alt / 100.) + "m " + lat + " " + lon;
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new PreApproach(this);
    }

}
