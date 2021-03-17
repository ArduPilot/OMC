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
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.flightplan.computation.FPsim;

/** check A-04: flying altitude larger than max. line of sight */
public class MaxHeightValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        MaxHeightValidator create(FlightPlan flightPlan);
    }

    private final String className = MaxHeightValidator.class.getName();
    private final ILanguageHelper languageHelper;

    @Inject
    public MaxHeightValidator(
            IQuantityStyleProvider quantityStyleProvider,
            ILanguageHelper languageHelper,
            @Assisted FlightPlan flightPlan) {
        super(flightPlan, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        FPsim.SimResultData simResult = flightplan.getLegacyFlightplan().getFPsim().getSimResult();
        if (simResult == null) {
            return false;
        }

        double lineOfSight =
            flightplan
                .getLegacyFlightplan()
                .getHardwareConfiguration()
                .getPlatformDescription()
                .getMaxLineOfSight()
                .convertTo(Unit.METER)
                .getValue()
                .doubleValue();

        if (simResult.minMaxHeightOverTakeoff.max > lineOfSight) {
            addWarning(
                languageHelper.getString(
                    className + ".altitudeTooHigh",
                    formatLength(simResult.minMaxHeightOverTakeoff.max),
                    formatLength(lineOfSight)),
                ValidationMessageCategory.NORMAL);
        }

        return true;
    }

}
