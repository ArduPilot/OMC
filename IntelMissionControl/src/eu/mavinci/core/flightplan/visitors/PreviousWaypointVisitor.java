/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

public class PreviousWaypointVisitor extends AFlightplanVisitor {

    IFlightplanRelatedObject fpStatement;
    public CWaypoint prevWaypoint = null;

    public PreviousWaypointVisitor(IFlightplanRelatedObject fpStatement) {
        this.fpStatement = fpStatement;
    }

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj == fpStatement) {
            return true;
        }

        if (fpObj instanceof CWaypoint) {
            CWaypoint wp = (CWaypoint)fpObj;
            prevWaypoint = wp;
        }

        return false;
    }

}
