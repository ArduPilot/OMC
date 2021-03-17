/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.visitors;

import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.measure.Unit;
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.visitors.AFlightplanVisitor;
import eu.mavinci.core.plane.CAirplaneCache;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.LandingPoint;
import eu.mavinci.flightplan.ReferencePoint;
import gov.nasa.worldwind.geom.Position;

public class LineOfSightVisitor extends AFlightplanVisitor {

    double startElevation;

    IFlightplanPositionReferenced last = null;
    IFlightplanPositionReferenced maxObj = null;
    double maxDistance2d = -1;

    Position pilotPoint;

    public LineOfSightVisitor(Flightplan fp, Position pilotPoint) {
        this.startElevation = fp.getRefPointAltWgs84WithElevation();
        this.pilotPoint = pilotPoint;
        // System.out.println("PilotPoint:"+pilotPoint + " startElevation:"+startElevation);
    }

    public boolean isValid(IPlatformDescription plane) {
        if (true) {
            return true; // TODO FIXME reinclude this test for productive version
        }

        double vlosLimit = 0; // TODO plane.getSomeRelatedParameter();
        // Debug.getLog().config("vlosLimit: " + vlosLimit);
        if (vlosLimit < maxDistance2d) {
            // check for vlosLimit > 0 if not 0
            return false; // also inside EU
        }

        // Debug.getLog().config("LineOfSight: " + maxDistance2d + " @ "+maxObj+" maxIs:" +
        // plane.getCamera().getMaxLineOfSightInM() + " for
        // FP:" + (last!=null? last.getFlightplan() : null));
        return maxDistance2d <= plane.getMaxLineOfSight().convertTo(Unit.METER).getValue().doubleValue();
    }

    public double getMaxDistance2d() {
        return maxDistance2d;
    }

    @Override
    public boolean visit(IFlightplanRelatedObject fpObj) {
        if (fpObj instanceof ReferencePoint) {
            return false;
        }

        if (fpObj instanceof IFlightplanPositionReferenced) {
            IFlightplanPositionReferenced next = (IFlightplanPositionReferenced)fpObj;
            double fpAlt = last != null ? last.getAltInMAboveFPRefPoint() : 0;
            if (!(next instanceof LandingPoint)) { // while flying to landingpoint, UAV is not chaning its alt
                fpAlt = Math.min(fpAlt, next.getAltInMAboveFPRefPoint());
            }

            fpAlt += startElevation;

            // double dist = CAirplaneCache.distanceMeters(next.getLat(), next.getLon(),fpAlt,
            // pilotPoint.latitude.degrees,
            // pilotPoint.longitude.degrees,pilotPoint.elevation);

            double dist =
                CAirplaneCache.distanceMeters(
                    next.getLat(), next.getLon(), pilotPoint.latitude.degrees, pilotPoint.longitude.degrees);
            if (dist > maxDistance2d) {
                maxDistance2d = dist;
                maxObj = next;
            }

            last = next;
        }

        return false;
    }

}
