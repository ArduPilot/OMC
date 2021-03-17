/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan;

import eu.mavinci.core.flightplan.IFlightplanContainer;
import eu.mavinci.core.flightplan.IFlightplanRelatedObject;
import eu.mavinci.core.flightplan.IRecalculateable;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.flightplan.visitors.FirstPicAreaVisitor;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.LatLon;

public final class Takeoff extends ReferencePoint implements IRecalculateable {

    public Takeoff(IFlightplanContainer parent) {
        super(parent);
    }

    public Takeoff(Takeoff source) {
        super(source);
    }

    public boolean isDefined() {
        return isDefined;
    }

    private static final String NAME = "Takeoff: ";

    @Override
    public String toString() {
        return NAME + lat + SEP + lon + SEP + altAboveR + SEP + yaw + SEP + elevation;
    }

    @Override
    public IFlightplanRelatedObject getCopy() {
        return new Takeoff(this);
    }

    public void updateFromUAV(IAirplane plane) throws AirplaneCacheEmptyException {
        // TODO IMPLEMENT ME FOR PROPER IDL SUPPORT in IMC 1.1
        /*if (plane.isWriteable()) {
            try {
                double newStartAlt = plane.getAirplaneCache().getStartElevOverWGS84();
                double newEgmOffset = plane.getAirplaneCache().getStartElevEGMoffset();
                // use it somehow...
            } catch (AirplaneCacheEmptyException e) {
            }
        }*/
    }

    // TODO remove this, put update logic into flightplan recalculate
    @Override
    public boolean doSubRecalculationStage1() {
        Flightplan fp = getFlightplan();

        if (!isDefined || isAuto) {
            if (fp == null) {
                isDefined = false;
                return true;
            }

            FirstPicAreaVisitor picAreaCollectsTypeVisitor = new FirstPicAreaVisitor();
            picAreaCollectsTypeVisitor.startVisit(fp);
            LatLon newCenter = null;
            PicArea picArea = picAreaCollectsTypeVisitor.getPicArea();
            if (picArea != null) {
                LatLon tmp = picArea.getCenterShiftedInOtherDirection();
                if (tmp != null) {
                    newCenter = tmp;
                }
            }

            if (newCenter != null) {
                isDefined = true;
                setLatLon(newCenter.latitude.degrees, newCenter.longitude.degrees);
            } else {
                isDefined = false;
            }
        }

        if (isDefined) {
            updateAltitudeWgs84();
            setAltInMAboveFPRefPoint(fp.getTakeofftAltWgs84WithElevation() - fp.getRefPointAltWgs84WithElevation());
        }

        return true;
    }

    @Override
    public boolean doSubRecalculationStage2() {
        return true;
    }

}
