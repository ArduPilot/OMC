/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

public class IndexByWaypointVisitor extends AFlightplanVisitor {

    private CWaypoint requiredWaipoint;
    private int globalIndex;
    private Integer resultIndex;

    private IndexByWaypointVisitor(CWaypoint requiredWaipoint) {
        this.requiredWaipoint = requiredWaipoint;
    }

    @Override
    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj instanceof CWaypoint) {
            globalIndex++;
            CWaypoint currentWp = (CWaypoint)fpObj;
            if (currentWp.equals(requiredWaipoint)) {
                resultIndex = globalIndex;
                return true;
            }
        }

        return false;
    }

    public static IndexByWaypointVisitor findIndexOf(CWaypoint waypoint) {
        return new IndexByWaypointVisitor(waypoint);
    }

    public Integer inFlightPlan(CFlightplan flightplan) {
        globalIndex = 0;
        resultIndex = null;
        startVisit(flightplan);
        return resultIndex;
    }
}
