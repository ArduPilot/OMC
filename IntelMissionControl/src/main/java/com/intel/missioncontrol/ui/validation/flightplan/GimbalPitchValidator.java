/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.flightplan;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.FlightPlanValidation;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;

/** Check if gimbal pitch values are within limits */
public class GimbalPitchValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        GimbalPitchValidator create(FlightPlan flightPlan);
    }

    private final String className = GimbalPitchValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public GimbalPitchValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightPlan flightplan) {
        super(flightplan, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        boolean checkOk = FlightPlanValidation.validateGimbalPitch(flightplan);
        if (!checkOk) {
            addWarning(languageHelper.getString(className + ".outOfRange"), ValidationMessageCategory.NOTICE);
        }

        return true;
    }

}
