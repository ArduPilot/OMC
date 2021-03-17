/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;

public class NextWaypointVisitor extends AFlightplanVisitor {

    IFlightplanRelatedObject fpStatement;
    public CWaypoint nextWaypoint = null;

    public NextWaypointVisitor(IFlightplanRelatedObject fpStatement) {
        this.fpStatement = fpStatement;
    }

    boolean found = false;

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (found) {
            if (fpObj instanceof CWaypoint) {
                CWaypoint wp = (CWaypoint)fpObj;
                nextWaypoint = wp;
                return true;
            }
        }

        if (fpObj == fpStatement) {
            found = true;
        }

        return false;
    }

}
