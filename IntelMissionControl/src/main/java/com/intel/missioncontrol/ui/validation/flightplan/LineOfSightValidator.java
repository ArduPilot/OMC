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

/** check A-TODO: flying within max. line of sight for estimated takeoff */
public class LineOfSightValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        LineOfSightValidator create(FlightPlan flightPlan);
    }

    private final String className = LineOfSightValidator.class.getName();
    private final ILanguageHelper languageHelper;

    @Inject
    public LineOfSightValidator(
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

        if (simResult.minMaxDistanceToTakeoff.max > lineOfSight) {
            addWarning(
                languageHelper.getString(
                    className + ".losTooLarge",
                    formatLength(simResult.minMaxDistanceToTakeoff.max),
                    formatLength(lineOfSight)),
                ValidationMessageCategory.NORMAL);
        }

        return true;
    }

}
