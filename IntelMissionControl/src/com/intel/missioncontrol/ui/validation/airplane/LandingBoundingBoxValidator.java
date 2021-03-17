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
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.core.plane.CAirplaneCache;
import eu.mavinci.core.plane.PlaneConstants;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.FlightplanManager;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.geom.LatLon;
import java.util.logging.Level;

/**
 * check B-05: Landing point is outside of bounding box (sirius only!) / too far away of current drone location
 * (falcon).
 */
public class LandingBoundingBoxValidator extends AirplaneValidatorBase {

    public interface Factory {
        LandingBoundingBoxValidator create(FlightplanAirplanePair flightplanAirplanePair);
    }

    private final String className = LandingBoundingBoxValidator.class.getName();

    @Inject
    public LandingBoundingBoxValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightplanAirplanePair flightplanAirplanePair) {
        super(languageHelper, quantityStyleProvider, flightplanAirplanePair);
        addPlaneInfoListener();
        addStartPosListener();
        addFlightplanChangeListener();
    }

    @Override
    protected boolean onInvalidated(FlightplanAirplanePair flightplanAirplanePair) {
        final Flightplan fp = flightplanAirplanePair.getFlightplan();
        if (fp == null) {
            return false;
        }

        final IAirplane plane = flightplanAirplanePair.getPlane();

        try {
            LatLon bbox = ((IAirplane)plane).getAirplaneCache().getStartPos();
            double radius;
            double circ;

            try {
                if (plane.getNativeHardwareConfiguration().getPlatformDescription().isInCopterMode()) {
                    radius = 300;
                    circ = 0;
                    setOkMessage("");
                } else {
                    setOkMessage(languageHelper.getString(className + ".okMessage"));
                    // sirius
                    if (plane.getAirplaneCache().getConf().USER_BBOXSIZE > 0) {
                        radius = plane.getAirplaneCache().getConf().USER_BBOXSIZE / 100.;
                    } else if (plane.getAirplaneCache().getConf().MISC_BBOXSIZE > 0) {
                        radius = plane.getAirplaneCache().getConf().MISC_BBOXSIZE / 100.;
                    } else {
                        radius = PlaneConstants.DEF_MISC_BBOXSIZE / 100.;
                    }

                    circ = plane.getAirplaneCache().getConf().CONT_NAV_CIRCR / 100.;
                }
            } catch (AirplaneCacheEmptyException e) {
                radius = PlaneConstants.DEF_MISC_BBOXSIZE / 100.;
                circ = PlaneConstants.DEF_CONT_NAV_CIRCR / 100.;
            }

            if (plane.getNativeHardwareConfiguration().getPlatformDescription().isInCopterMode()
                    && fp.getLandingpoint().getMode() == LandingModes.DESC_STAYAIRBORNE) {
                // landing point is ignored
                return true;
            }

            LatLon landing = fp.getLandingpoint().getLatLon();
            double dist =
                CAirplaneCache.distanceMeters(
                    bbox.latitude.degrees, bbox.longitude.degrees, landing.latitude.degrees, landing.longitude.degrees);
            // double dist = LatLon.ellipsoidalDistance(bbox, landing, Earth.WGS84_EQUATORIAL_RADIUS,
            // Earth.WGS84_POLAR_RADIUS); //this was returning NAN instead of 0
            Debug.getLog()
                .log(
                    Level.INFO,
                    "distance between BBox center and landingPoint: "
                        + dist
                        + "   BBox Radius: "
                        + radius
                        + "   bbox: "
                        + bbox
                        + "  landing: "
                        + landing
                        + " isOnAir:"
                        + fp.isOnAirFlightplan()
                        + " isOnAirRelatedLocal:"
                        + fp.isOnAirRelatedLocalFlightplan(plane));
            if (dist >= radius - circ - FlightplanManager.BOUNDING_BOX_SAFETY_MARGIN_M) {
                addWarning(
                    languageHelper.getString(className + ".outOfBBox", formatLength(dist), formatLength(radius)),
                    ValidationMessageCategory.BLOCKING);
            }
        } catch (AirplaneCacheEmptyException e) {
            // expected
            return false;
        }

        return true;
    }

}
