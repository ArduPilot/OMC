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
import eu.mavinci.core.flightplan.CStartProcedure;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.plane.IAirplane;

/**
 * check B-06: Start procedure is below Xm over takeoff // for whatever reason sometimes the starting procedure (resp.
 * the landing point comes with a negative altitude.. this would lead to // a crash after takeoff // detect such
 * situations, throw some severe validation and fix them
 */
public class StartAltValidator extends AirplaneValidatorBase {

    public interface Factory {
        StartAltValidator create(FlightplanAirplanePair flightplanAirplanePair);
    }

    private static final double MIN_HEIGHT_FIXEDWING = 30; // in meters
    private static final double MIN_HEIGHT_COPTER = 2; // in meters

    private final String className = StartAltValidator.class.getName();

    @Inject
    public StartAltValidator(
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
        double minHeight = MIN_HEIGHT_FIXEDWING;

        try {
            fp.getTakeoff().updateFromUAV(plane);
            // TODO FIXME... there has to be a better way ;-)
            if (plane.getAirplaneCache().getApType().name().equals("falcon")) {
                minHeight = MIN_HEIGHT_COPTER;
            }
        } catch (AirplaneCacheEmptyException e) {
            addWarning(languageHelper.getString(className + ".startAltUnknown"), ValidationMessageCategory.BLOCKING);
            return false;
        }

        if (fp.sizeOfFlightplanContainer() > 0 && fp.getFromFlightplanContainer(0) instanceof CStartProcedure) {
            CStartProcedure start = (CStartProcedure)fp.getFromFlightplanContainer(0);
            if (start.getAltInMAboveFPRefPoint() < minHeight) {
                addWarning(
                    languageHelper.getString(
                        className + ".minStartHeightTooLow",
                        formatLength(minHeight),
                        formatLength(start.getAltInMAboveFPRefPoint())),
                    ValidationMessageCategory.BLOCKING);
            }
        }

        return true;
    }

}
