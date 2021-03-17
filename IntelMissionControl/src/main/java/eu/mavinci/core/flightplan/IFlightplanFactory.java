/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan;

public interface IFlightplanFactory {

    public abstract CFlightplan newFlightplan();

    /*public abstract CLandingPoint newLandingPoint(CFlightplan fp);

    public abstract CLandingPoint newLandingPoint(double lat, double lon);

    public abstract CLandingPoint newLandingPoint(CFlightplan fp, double lat, double lon);

    public abstract CLandingPoint newLandingPoint(double lat, double lon, LandingModes mode);

    public abstract CLandingPoint newLandingPoint(CFlightplan fp, double lat, double lon, LandingModes mode, double yaw, int id);

    public abstract COrigin newOrigin(CFlightplan cFlightplan);

    public abstract COrigin newOrigin(CFlightplan fp, double lat, double lon, double alt, double yaw, boolean isDefined);*/

    public abstract CPhoto newCPhoto(boolean powerOn, double distance, double distanceMax, int id);

    public abstract CPhoto newCPhoto(
            boolean powerOn, double distance, double distanceMax, int id, IFlightplanContainer parent);

    public abstract CPhoto newCPhoto(boolean powerOn, double distance, double distanceMax, IFlightplanContainer parent);

    public abstract CPhoto newCPhoto(IFlightplanContainer parent);

    public abstract CDump newCDump(String body);

    public abstract CWaypoint newCWaypoint(
            double lon,
            double lat,
            double altWithinM,
            AltAssertModes assertAltitude,
            float radiusWithinM,
            String body,
            int id,
            IFlightplanContainer parent);

    public abstract CWaypoint newCWaypoint(
            double lon,
            double lat,
            int altWithinCM,
            AltAssertModes assertAltitude,
            int radiusWithinCM,
            String body,
            int id,
            IFlightplanContainer parent);

    public abstract CWaypoint newCWaypoint(
            double lon,
            double lat,
            double altWithinM,
            AltAssertModes assertAltitude,
            float radiusWithinM,
            String body,
            IFlightplanContainer parent);

    public abstract CWaypoint newCWaypoint(
            double lon,
            double lat,
            int altWithinCM,
            AltAssertModes assertAltitude,
            int radiusWithinCM,
            String body,
            IFlightplanContainer parent);

    public abstract CWaypoint newCWaypoint(
            double lon,
            double lat,
            int altWithinCM,
            AltAssertModes assertAltitude,
            int radiusWithinCM,
            String body,
            int id);

    public abstract CWaypoint newCWaypoint(double lon, double lat, IFlightplanContainer parent);

    public abstract CWaypointLoop newCWaypointLoop(IFlightplanContainer parent);

    public abstract CWaypointLoop newCWaypointLoop(int count, int time, IFlightplanContainer parent);

    public abstract CWaypointLoop newCWaypointLoop(int count, int time, int id, IFlightplanContainer parent);

    public abstract CPicArea newPicArea(
            int id,
            IFlightplanContainer parent,
            double gsd,
            double overlapInFlight,
            double overlapInFlightMin,
            double overlapParallel);

    public abstract CPicArea newPicArea(
            IFlightplanContainer parent,
            double gsd,
            double overlapInFlight,
            double overlapInFlightMin,
            double overlapParallel);

    // public abstract CPoint newCPoint(IFlightplanContainer cont, double lat, double lon);

    public abstract CPicAreaCorners newPicAreaCorners(CPicArea area);

    public abstract CPicArea newPicArea(IFlightplanContainer parent);

    // public abstract CPoint newCPoint(IFlightplanContainer parent);

    public abstract CPhotoSettings newCPhotoSettings(
            double maxRoll, double maxNick, double mintimeinterval, IFlightplanContainer cont);

    public abstract CPhotoSettings newCPhotoSettings(IFlightplanContainer cont);

    public abstract CEventList newCEventList(CFlightplan fp);

    public abstract CEvent newCEvent(CEventList cont, String name);

}
