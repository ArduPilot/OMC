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
import eu.mavinci.core.flightplan.IFlightplanPositionReferenced;
import eu.mavinci.core.flightplan.visitors.ExtractTypeVisitor;
import eu.mavinci.core.helper.MinMaxPair;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.flightplan.Flightplan;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.globes.Earth;

/** check B-11: Flight plan is too far away (first waypint > 1km, some waypoints more then 10km). */
public class DistanceFirstPointValidator extends AirplaneValidatorBase {

    public interface Factory {
        DistanceFirstPointValidator create(FlightplanAirplanePair flightplanAirplanePair);
    }

    private static final double MAX_DISTANCE_FIRST = 1000; // in meters
    private static final double MAX_DISTANCE_ALL = 10000; // in meters

    private final String className = DistanceFirstPointValidator.class.getName();

    @Inject
    public DistanceFirstPointValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightplanAirplanePair flightplanAirplanePair) {
        super(languageHelper, quantityStyleProvider, flightplanAirplanePair);
        addStartPosListener();
        addFlightplanChangeListener();
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightplanAirplanePair flightplanAirplanePair) {
        Flightplan fp = flightplanAirplanePair.getFlightplan();
        if (fp == null) {
            return false;
        }

        ExtractTypeVisitor<IFlightplanPositionReferenced> visitor =
            new ExtractTypeVisitor<>(IFlightplanPositionReferenced.class);
        visitor.startVisit(fp);
        if (!visitor.filterResults.isEmpty()) {
            IFlightplanPositionReferenced objFirst = visitor.filterResults.firstElement();
            try {
                LatLon posTakeoff = flightplanAirplanePair.getPlane().getAirplaneCache().getStartPos();
                LatLon posFirst = LatLon.fromDegrees(objFirst.getLat(), objFirst.getLon());
                double dist =
                    LatLon.ellipsoidalDistance(
                        posTakeoff, posFirst, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS);
                if (dist >= MAX_DISTANCE_FIRST) {
                    addWarning(
                        languageHelper.getString(
                            className + ".takeoffTooFarAway", formatLength(dist), formatLength(MAX_DISTANCE_FIRST)),
                        ValidationMessageCategory.NORMAL);
                }

                MinMaxPair minMaxPair = new MinMaxPair();
                for (Object obj : visitor.filterResults) {
                    IFlightplanPositionReferenced objPos = (IFlightplanPositionReferenced)obj;
                    LatLon pos = LatLon.fromDegrees(objPos.getLat(), objPos.getLon());
                    dist =
                        LatLon.ellipsoidalDistance(
                            posTakeoff, pos, Earth.WGS84_EQUATORIAL_RADIUS, Earth.WGS84_POLAR_RADIUS);
                    minMaxPair.update(dist);
                }

                if (minMaxPair.max >= MAX_DISTANCE_ALL) {
                    addWarning(
                        languageHelper.getString(
                            className + ".someTooFarAway",
                            formatLength(minMaxPair.max),
                            formatLength(MAX_DISTANCE_ALL)),
                        ValidationMessageCategory.NORMAL);
                }
            } catch (AirplaneCacheEmptyException e) {
                // expected
                return false;
            }
        }

        return true;
    }

}
