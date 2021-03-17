/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

public class LastWaypointVisitor extends AFlightplanVisitor {

    public CWaypoint lastWaypoint = null;

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj instanceof CWaypoint) {
            CWaypoint wp = (CWaypoint)fpObj;
            lastWaypoint = wp;
        }

        return false;
    }

}
