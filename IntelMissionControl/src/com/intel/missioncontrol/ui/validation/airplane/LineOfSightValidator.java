/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.airplane;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.visitors.LineOfSightVisitor;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

/** check B-19 Line of Sight checks. */
public class LineOfSightValidator extends AirplaneValidatorBase {

    public interface Factory {
        LineOfSightValidator create(FlightplanAirplanePair flightplanAirplanePair);
    }

    private final String className = LineOfSightValidator.class.getName();

    @Inject
    public LineOfSightValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightplanAirplanePair flightplanAirplanePair) {
        super(languageHelper, quantityStyleProvider, flightplanAirplanePair);
        addPlaneInfoListener();
        addStartPosListener();
        addFlightplanChangeListener();
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightplanAirplanePair flightplanAirplanePair) {
        final Flightplan fp = flightplanAirplanePair.getFlightplan();
        if (fp == null) {
            return false;
        }

        final IAirplane plane = flightplanAirplanePair.getPlane();
        LatLon pilotS = null;

        try {
            if (pilotS == null) {
                // this center will be used by the autpilot to determine the VLOS limit, so this one is the most
                // informative one
                pilotS = ((IAirplane)plane).getAirplaneCache().getStartPos();
            }
        } catch (AirplaneCacheEmptyException e) {
            // expected
        }

        try {
            if (pilotS == null && plane.getAirplaneCache().getBackend().hasFix) {
                pilotS =
                    LatLon.fromDegrees(
                        plane.getAirplaneCache().getBackend().lat, plane.getAirplaneCache().getBackend().lon);
            }
        } catch (AirplaneCacheEmptyException e) {
            // expected
        }

        if (pilotS == null) {
            pilotS = LatLon.fromDegrees(fp.getLandingpoint().getLat(), fp.getLandingpoint().getLon());
        }

        IElevationModel elevationModel = DependencyInjector.getInstance().getInstanceOf(IElevationModel.class);
        Position pilotPoint = elevationModel.getPositionOverGround(pilotS);
        LineOfSightVisitor visLoS = new LineOfSightVisitor(fp, pilotPoint);
        visLoS.startVisit(fp);
        double maxDistance2d = visLoS.getMaxDistance2d();

        double vlosLimit = 0;
        try {
            vlosLimit = plane.getAirplaneCache().getPlaneInfo().vloslimit;
        } catch (AirplaneCacheEmptyException e) {
            // expected
        }

        if (vlosLimit > 0) { // otherwise their is no limit!
            if (vlosLimit < maxDistance2d) { // also inside EU
                // check for vlosLimit > 0 if not 0
                addWarning(
                    languageHelper.getString(
                        className + ".outOfLineOfSightForbidden", formatLength(maxDistance2d), formatLength(vlosLimit)),
                    ValidationMessageCategory.BLOCKING);
            }

            double camMaxDist =
                fp.getHardwareConfiguration()
                    .getPlatformDescription()
                    .getMaxLineOfSight()
                    .convertTo(Unit.METER)
                    .getValue()
                    .doubleValue();
            if (camMaxDist < maxDistance2d) {
                addWarning(
                    languageHelper.getString(
                        className + ".outOfLineOfSightWarning", formatLength(maxDistance2d), formatLength(camMaxDist)),
                    ValidationMessageCategory.NORMAL);
            }
        }

        return true;
    }

}
