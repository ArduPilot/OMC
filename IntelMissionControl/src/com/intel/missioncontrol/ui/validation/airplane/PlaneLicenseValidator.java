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
import com.intel.missioncontrol.ui.validation.SimpleResolveAction;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.core.plane.AirplaneCacheEmptyException;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.plane.IAirplane;
import java.io.IOException;
import java.util.logging.Level;

/** check B-01 licence of connected UAV does not allow the execution of the flight plan. */
public class PlaneLicenseValidator extends AirplaneValidatorBase {

    public interface Factory {
        PlaneLicenseValidator create(FlightplanAirplanePair flightplanAirplanePair);
    }

    private final String className = PlaneLicenseValidator.class.getName();

    @Inject
    public PlaneLicenseValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightplanAirplanePair flightplanAirplanePair) {
        super(languageHelper, quantityStyleProvider, flightplanAirplanePair);
        addPlaneInfoListener();
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

        try {
            // TODO FIXME... this needs to be implemented...
            if (plane.getAirplaneCache().getApType().name().equals("falcon")) {
                // TODO: cross check if the current connected plane is capable of flying this missions
                addWarning(
                    languageHelper.getString(className + ".missingLicence"),
                    ValidationMessageCategory.BLOCKING,
                    new SimpleResolveAction(
                        languageHelper.getString(className + ".buy"),
                        () -> {
                            try {
                                // TODO fixme.. correct URL
                                FileHelper.openFileOrURL("http://drones.intel.com");
                            } catch (IOException e) {
                                Debug.getLog().log(Level.WARNING, "can open URL", e);
                            }
                        }));
            }
        } catch (AirplaneCacheEmptyException e) {
            // not defined yet...
            return false;
        }

        return true;
    }

}
