/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import eu.mavinci.core.flightplan.AltAssertModes;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.Orientation;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.geo.IPositionReferenced;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

public class Waypoint extends CWaypoint implements IPositionReferenced {
    public Waypoint(Waypoint source) {
        super(source.lon, source.lat, source.alt, source.assertAltitude, source.radius, source.body, source.id);
        this.orientation = new Orientation(source.orientation);
        this.ignore = source.ignore;
        this.assertYawOn = source.assertYawOn;
        this.assertYaw = source.assertYaw;
        this.speedMode = source.speedMode;
        this.speedMpSec = source.speedMpSec;
        this.stopHereTimeCopter = source.stopHereTimeCopter;
        this.triggerImageHereCopterMode = source.triggerImageHereCopterMode;
        this.isBeginFlightline = source.isBeginFlightline;
        this.targetDistance = source.targetDistance;
    }

    public Waypoint(
            double lon,
            double lat,
            double altWithinM,
            AltAssertModes assertAltitude,
            float radiusInM,
            String body,
            int id,
            IFlightplanContainer parent) {
        super(lon, lat, altWithinM, assertAltitude, radiusInM, body, id, parent);
    }

    protected Waypoint(
            double lon,
            double lat,
            int altWithinCM,
            AltAssertModes assertAltitude,
            int radiusInCM,
            String body,
            int id,
            IFlightplanContainer parent) {
        super(lon, lat, altWithinCM, assertAltitude, radiusInCM, body, id, parent);
    }

    public Waypoint(
            double lon,
            double lat,
            double altWithinM,
            AltAssertModes assertAltitude,
            float radiusInM,
            String body,
            IFlightplanContainer parent) {
        super(lon, lat, altWithinM, assertAltitude, radiusInM, body, parent);
    }

    protected Waypoint(
            double lon,
            double lat,
            int altWithinCM,
            AltAssertModes assertAltitude,
            int radiusInCM,
            String body,
            IFlightplanContainer parent) {
        super(lon, lat, altWithinCM, assertAltitude, radiusInCM, body, parent);
    }

    public Waypoint(double lon, double lat, IFlightplanContainer parent) {
        super(lon, lat, parent);
    }

    public Waypoint(
            double lon,
            double lat,
            int altWithinCM,
            AltAssertModes assertAltitude,
            int radiusInCM,
            String body,
            int id) {
        super(lon, lat, altWithinCM, assertAltitude, radiusInCM, body, id);
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
        // TODO: LanguageHelper should only be used in UI classes.
        return "Waypoint:"
            + (alt / 100.)
            + "m "
            + (assertAltitude != AltAssertModes.unasserted ? "Assert" : "")
            + " "
            + lat
            + " "
            + lon
            + " "
            + body
            + " "
            + StringHelper.lengthToIngName(radius / 100., -3, false);
    }

    @Override
    public Waypoint getCopy() {
        return new Waypoint(this);
    }
}
