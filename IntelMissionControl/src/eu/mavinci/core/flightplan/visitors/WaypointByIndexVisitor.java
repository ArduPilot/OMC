/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

public class WaypointByIndexVisitor extends AFlightplanVisitor {

    public WaypointByIndexVisitor(Integer requredIndex) {
        this.requredIndex = requredIndex;
        skipIgnoredPaths = true;
    }

    private Integer countOfWaypoints = 0;
    private Integer requredIndex;
    private CWaypoint waypointByIndex = null;

    @Override
    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj instanceof CWaypoint) {
            countOfWaypoints++;
            CWaypoint waypoint = (CWaypoint)fpObj;
            if (requredIndex.equals(countOfWaypoints)) {
                waypointByIndex = waypoint;
                waypointByIndex.setReentryPoint(true);
            } else {
                waypoint.setReentryPoint(false);
            }
        }

        return false;
    }

    public Integer getCountOfWaypoints() {
        return countOfWaypoints;
    }

    public Integer getRequredIndex() {
        return requredIndex;
    }

    public CWaypoint getWaypointByIndex() {
        return waypointByIndex;
    }
}
