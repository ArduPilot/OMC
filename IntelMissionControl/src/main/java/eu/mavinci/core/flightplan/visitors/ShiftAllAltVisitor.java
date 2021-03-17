/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.flightplan.visitors;

import eu.mavinci.core.flightplan.CEventList;
import eu.mavinci.core.flightplan.CPicArea;
import eu.mavinci.core.flightplan.CWaypoint;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.flightplan.LandingPoint;
import eu.mavinci.flightplan.ReferencePoint;

public class ShiftAllAltVisitor extends AFlightplanVisitor {

    public ShiftAllAltVisitor(double dxInM) {
        this.dxInM = dxInM;
    }

    public double dxInM;

    IFlightplanRelatedObject visitRoot;

    public void startVisit(IFlightplanRelatedObject fpObj) {
        visitRoot = fpObj;
        super.startVisit(fpObj);
    }

    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj instanceof ReferencePoint) {
            // ignore
        } else if (fpObj instanceof LandingPoint) {
            LandingPoint landingP = (LandingPoint)fpObj;
            if (landingP.isLandAutomatically()) {
                return false;
            }

            landingP.setAltInMAboveFPRefPoint(landingP.getAltInMAboveFPRefPoint() + dxInM);
        } else if (fpObj instanceof IFlightplanPositionReferenced) {
            IFlightplanPositionReferenced wp = (IFlightplanPositionReferenced)fpObj;
            if (wp instanceof CWaypoint) {
                CWaypoint waypoint = (CWaypoint)wp;
                if (waypoint.getBody().startsWith(CWaypoint.SpecialPurposeBodyPrefix)) {
                    return false;
                }
            }

            wp.setAltInMAboveFPRefPoint(wp.getAltInMAboveFPRefPoint() + dxInM);
        } else if (fpObj instanceof CEventList) {
            CEventList eventList = (CEventList)fpObj;
            eventList.setAltWithinM(eventList.getAltWithinM() + dxInM);
        } else if (fpObj instanceof CPicArea) {
            CPicArea picArea = (CPicArea)fpObj;
            //in case of 3D objects here wrong "alt" was shifted
            if(picArea.getPlanType().needsHeights()){
                //picArea.setObjectHeight(picArea.getObjectHeight() + dxInM);
            }else {
                picArea.setAlt(picArea.getAlt() + dxInM);
            }
        }

        return false;
    }
}
