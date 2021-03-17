/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.airplane;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.plane.IAirplane;

/** check B-10 Flight plan HW selection differs from the target drone. */
public class HardwareCompatibilityValidator extends AirplaneValidatorBase {

    public interface Factory {
        HardwareCompatibilityValidator create(FlightplanAirplanePair flightplanAirplanePair);
    }

    private final String className = HardwareCompatibilityValidator.class.getName();

    @Inject
    public HardwareCompatibilityValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightplanAirplanePair flightplanAirplanePair) {
        super(languageHelper, quantityStyleProvider, flightplanAirplanePair);
        addPlaneInfoListener();
        addFlightplanChangeListener();
        // setOkMessage(languageHelper.getString(className+".okMessage"));//dont show it by intention
    }

    @Override
    protected boolean onInvalidated(FlightplanAirplanePair flightplanAirplanePair) {
        final Flightplan fp = flightplanAirplanePair.getFlightplan();
        if (fp == null) {
            return false;
        }

        final IAirplane plane = flightplanAirplanePair.getPlane();
        IPlatformDescription planePlatformDescription = plane.getNativeHardwareConfiguration().getPlatformDescription();
        IPlatformDescription flightPlanPlatformDescription = fp.getHardwareConfiguration().getPlatformDescription();

        if (planePlatformDescription.isInCopterMode()) {
            // TODO FIXME... there has to be a better way ;-)
            if (!flightPlanPlatformDescription.isInCopterMode()) {
                addWarning(languageHelper.getString(className + ".wrongPlane"), ValidationMessageCategory.NORMAL);
            }
        } else {
            if (flightPlanPlatformDescription.isInCopterMode()) {
                addWarning(languageHelper.getString(className + ".wrongPlane"), ValidationMessageCategory.NORMAL);
            }
        }

        return true;
    }

}
