/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.CEventList;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.CStartProcedure;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.flightplan.LandingPoint;
import eu.mavinci.flightplan.ReferencePoint;

public class SetAllAltVisitor extends AFlightplanVisitor {

    public SetAllAltVisitor(double dxInM) {
        this.altInM = dxInM;
    }

    public double altInM;

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj instanceof ReferencePoint) {
            // ignore
        } else if (fpObj instanceof CStartProcedure) {
            CStartProcedure start = (CStartProcedure)fpObj;
            if (!start.hasOwnAltitude()) {
                return false;
            }

            start.setAltWithinMforwarding(altInM);
        } else if (fpObj instanceof IFlightplanPositionReferenced) {
            IFlightplanPositionReferenced wp = (IFlightplanPositionReferenced)fpObj;
            if (wp instanceof LandingPoint) {
                LandingPoint lp = (LandingPoint)wp;
                if (lp.getMode() == LandingModes.DESC_FULL3d) {
                    return false;
                }
            }

            if (wp instanceof CWaypoint) {
                CWaypoint waypoint = (CWaypoint)wp;
                if (waypoint.getBody().startsWith(CWaypoint.SpecialPurposeBodyPrefix)) {
                    return false;
                }
            }

            wp.setAltInMAboveFPRefPoint(altInM);
        } else if (fpObj instanceof CEventList) {
            CEventList eventList = (CEventList)fpObj;
            eventList.setAltWithinM(altInM);
        } else if (fpObj instanceof CPicArea) {
            CPicArea picArea = (CPicArea)fpObj;
            picArea.setAlt(altInM);
        }

        return false;
    }
}
