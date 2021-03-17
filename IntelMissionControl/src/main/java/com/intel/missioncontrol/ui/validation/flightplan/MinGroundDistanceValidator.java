/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.flightplan;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.flightplan.computation.FPsim;

/** check A-06: actual minimal distance to ground smaller than tolerance */
public class MinGroundDistanceValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        MinGroundDistanceValidator create(FlightPlan flightPlan);
    }

    private final String className = MinGroundDistanceValidator.class.getName();
    private final ILanguageHelper languageHelper;

    @Inject
    public MinGroundDistanceValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightPlan flightplan) {
        super(flightplan, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        FPsim.SimResultData simResult = flightplan.getLegacyFlightplan().getFPsim().getSimResult();
        if (simResult == null) {
            return false;
        }

        double camMinDist =
            flightplan
                .getLegacyFlightplan()
                .getHardwareConfiguration()
                .getPlatformDescription()
                .getMinGroundDistance()
                .convertTo(Unit.METER)
                .getValue()
                .doubleValue();

        if (simResult.minMaxDistanceToGround.min < camMinDist) {
            addWarning(
                languageHelper.getString(
                    className + ".tooCloseToGround",
                    formatLength(simResult.minMaxDistanceToGround.min),
                    formatLength(camMinDist))
                // ,new SimpleResolveAction(languageHelper.getString(className + ".increaseAltitude"))  //reinclude
                // later when show concept works
                ,
                ValidationMessageCategory.BLOCKING);
        }

        return true;
    }

}
