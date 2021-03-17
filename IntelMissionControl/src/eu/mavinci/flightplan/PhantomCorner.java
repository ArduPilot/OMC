/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.desktop.main.debug.Debug;
import gov.nasa.worldwind.geom.LatLon;
import eu.mavinci.core.flightplan.FlightplanContainerFullException;
import eu.mavinci.core.flightplan.FlightplanContainerWrongAddingException;
import eu.mavinci.core.flightplan.IFlightplanContainer;

import java.util.logging.Level;

public class PhantomCorner {
    Point nodeToAddBehind;
    LatLon location;
    Point relaizedPoint;

    public PhantomCorner(Point nodeToAddBehind, LatLon location) {
        this.nodeToAddBehind = nodeToAddBehind;
        this.location = location;
    }

    public Point makeReal() throws FlightplanContainerFullException {
        if (relaizedPoint != null) return relaizedPoint;
        IFlightplanContainer container = nodeToAddBehind.getParent();
        int idx = 0;
        while (idx < container.getMaxSize() && container.getFromFlightplanContainer(idx) != nodeToAddBehind) {
            idx++;
        }

        idx++;
        Point point = new Point(container, location.getLatitude().degrees, location.getLongitude().degrees);
        try {
            container.addToFlightplanContainer(idx, point);
        } catch (FlightplanContainerWrongAddingException e) {
            Debug.getLog().log(Level.WARNING, "cant add new corner to corner list out of virtual point", e);
        }

        relaizedPoint = point;
        return point;
    }
}
