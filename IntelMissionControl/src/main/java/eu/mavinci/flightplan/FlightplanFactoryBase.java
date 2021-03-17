/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import eu.mavinci.core.flightplan.AltAssertModes;
import eu.mavinci.core.flightplan.CDump;
import eu.mavinci.core.flightplan.CEvent;
import eu.mavinci.core.flightplan.CEventList;
import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CPhoto;
import eu.mavinci.core.flightplan.CPhotoSettings;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.CPicAreaCorners;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.CWaypointLoop;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanFactory;
import eu.mavinci.core.flightplan.IFlightplanStatement;

public class FlightplanFactoryBase implements IFlightplanFactory {

    public CFlightplan newFlightplan() {
        return new Flightplan();
    }

    /*public CLandingPoint newLandingPoint(CFlightplan fp) {
        return new LandingPoint(fp);
    }

    public CLandingPoint newLandingPoint(double lat, double lon) {
        return new LandingPoint(lat, lon);
    }

    public CLandingPoint newLandingPoint(CFlightplan fp, double lat, double lon) {
        return new LandingPoint(fp, lat, lon);
    }

    public CLandingPoint newLandingPoint(double lat, double lon, LandingModes mode) {
        return new LandingPoint(lat, lon, mode);
    }

    public CLandingPoint newLandingPoint(CFlightplan fp, double lat, double lon, LandingModes mode, double yaw, int id) {
        return new LandingPoint(fp, lat, lon, mode, yaw, id);
    }*/

    public CPhoto newCPhoto(boolean powerOn, double distance, double distanceMax, int id) {
        return new Photo(powerOn, distance, distanceMax, id);
    }

    public CPhoto newCPhoto(boolean powerOn, double distance, double distanceMax, int id, IFlightplanContainer parent) {
        return new Photo(powerOn, distance, distanceMax, id, parent);
    }

    public CPhoto newCPhoto(boolean powerOn, double distance, double distanceMax, IFlightplanContainer parent) {
        return new Photo(powerOn, distance, distanceMax, parent);
    }

    public CWaypoint newCWaypoint(
            double lon,
            double lat,
            double altWithinM,
            AltAssertModes assertAltitude,
            float radiusWithinM,
            String body,
            int id,
            IFlightplanContainer parent) {
        return new Waypoint(lon, lat, altWithinM, assertAltitude, radiusWithinM, body, parent);
    }

    public CWaypoint newCWaypoint(
            double lon,
            double lat,
            int altWithinCM,
            AltAssertModes assertAltitude,
            int radiusWithinCM,
            String body,
            int id,
            IFlightplanContainer parent) {
        return new Waypoint(lon, lat, altWithinCM, assertAltitude, radiusWithinCM, body, id, parent);
    }

    public CWaypoint newCWaypoint(
            double lon,
            double lat,
            double altWithinM,
            AltAssertModes assertAltitude,
            float radiusWithinM,
            String body,
            IFlightplanContainer parent) {
        return new Waypoint(lon, lat, altWithinM, assertAltitude, radiusWithinM, body, parent);
    }

    public CWaypoint newCWaypoint(
            double lon,
            double lat,
            int altWithinCM,
            AltAssertModes assertAltitude,
            int radiusWithinCM,
            String body,
            IFlightplanContainer parent) {
        return new Waypoint(lon, lat, altWithinCM, assertAltitude, radiusWithinCM, body, parent);
    }

    public CWaypoint newCWaypoint(
            double lon,
            double lat,
            int altWithinCM,
            AltAssertModes assertAltitude,
            int radiusWithinCM,
            String body,
            int id) {
        return new Waypoint(lon, lat, altWithinCM, assertAltitude, radiusWithinCM, body, id);
    }

    public CWaypointLoop newCWaypointLoop(int count, int time, IFlightplanContainer parent) {
        return new WaypointLoop(count, time, parent);
    }

    public CWaypointLoop newCWaypointLoop(int count, int time, int id, IFlightplanContainer parent) {
        return new WaypointLoop(count, time, id, parent);
    }

    public CPhoto newCPhoto(IFlightplanContainer parent) {
        return new Photo(parent);
    }

    public CWaypoint newCWaypoint(double lon, double lat, IFlightplanContainer parent) {
        return new Waypoint(lon, lat, parent);
    }

    public CWaypointLoop newCWaypointLoop(IFlightplanContainer parent) {
        return new WaypointLoop(parent);
    }

    public Point newCPoint(IFlightplanContainer cont, double lat, double lon) {
        return new Point(cont, lat, lon);
    }

    public CPicAreaCorners newPicAreaCorners(CPicArea area) {
        return new PicAreaCorners(area);
    }

    public CPicArea newPicArea(
            int id,
            IFlightplanContainer parent,
            double gsd,
            double overlapInFlight,
            double overlapInFlightMin,
            double overlapParallel) {
        return new PicArea(id, parent, gsd, overlapInFlight, overlapInFlightMin, overlapParallel);
    }

    public CPicArea newPicArea(IFlightplanContainer parent) {
        return new PicArea(parent);
    }

    /*public CPoint newCPoint(IFlightplanContainer parent) {
        return new Point(parent);
    }*/

    public CPicArea newPicArea(
            IFlightplanContainer parent,
            double gsd,
            double overlapInFlight,
            double overlapInFlightMin,
            double overlapParallel) {
        return new PicArea(parent, gsd, overlapInFlight, overlapInFlightMin, overlapParallel);
    }

    public CPhotoSettings newCPhotoSettings(
            double maxRoll, double maxNick, double mintimeinterval, IFlightplanContainer cont) {
        return new PhotoSettings(maxRoll, maxNick, mintimeinterval, cont);
    }

    public CPhotoSettings newCPhotoSettings(IFlightplanContainer cont) {
        return new PhotoSettings(cont);
    }

    @Override
    public CDump newCDump(String body) {
        return new Dump(body);
    }

    @Override
    public CEventList newCEventList(CFlightplan fp) {
        return new EventList((Flightplan)fp);
    }

    public CEvent newCEvent(CEventList cont, String name) {
        return new Event((EventList)cont, name);
    }


    /*@Override
    public COrigin newOrigin(CFlightplan cFlightplan) {
        return new Origin(cFlightplan);
    }

    @Override
    public COrigin newOrigin(CFlightplan fp, double lat, double lon, double alt, double yaw, boolean isDefined) {
        return new Origin(fp, lat, lon, alt, yaw, isDefined);
    }*/

}
