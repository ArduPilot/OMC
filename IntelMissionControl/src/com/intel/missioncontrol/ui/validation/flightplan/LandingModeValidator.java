/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.flightplan;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.LandingPoint;

/** check A-02: landing mode is not supported in this user level */
public class LandingModeValidator extends OnFlightplanChangedValidator {

    public interface Factory {
        LandingModeValidator create(FlightPlan flightPlan);
    }

    private final String className = LandingModeValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public LandingModeValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightPlan flightplan) {
        super(flightplan, quantityStyleProvider);
        this.languageHelper = languageHelper;
        // setOkMessage(languageHelper.getString(className+".okMessage"));//dont show it by intention
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        Flightplan legacyFlightplan = flightplan.getLegacyFlightplan();
        if (!LandingPoint.isModeUserLevelOK(
                legacyFlightplan.getLandingpoint().getMode(),
                legacyFlightplan.getHardwareConfiguration().getPlatformDescription())) {
            addWarning(
                languageHelper.getString(
                    className + ".landingModeNotAvaliable",
                    LandingPoint.getLandingModeI18N(flightplan.getLegacyFlightplan().getLandingpoint().getMode())),
                ValidationMessageCategory.BLOCKING);
        }

        if (legacyFlightplan.getLandingpoint().getLat() == 0 && legacyFlightplan.getLandingpoint().getLon() == 0) {
            if (legacyFlightplan.getHardwareConfiguration().getPlatformDescription().isInCopterMode()
                    && legacyFlightplan.getLandingpoint().getMode() != LandingModes.DESC_CIRCLE) {
                return true;
            }

            addWarning(
                languageHelper.getString(className + ".autoLandingPointUndefined"), ValidationMessageCategory.NORMAL);
            return false;
        }

        return true;
    }

}
