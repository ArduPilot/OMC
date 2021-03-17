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

/** check if all waypoints can be stored on drone */
public class NumberOfWaypointsValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        NumberOfWaypointsValidator create(FlightPlan flightPlan);
    }

    private ILanguageHelper languageHelper;

    @Inject
    public NumberOfWaypointsValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightPlan flightplan) {
        super(flightplan, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(NumberOfWaypointsValidator.class, "okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        boolean checkOk = FlightPlanValidation.validateNumberOfWaypoints(flightplan);
        if (!checkOk) {
            addWarning(languageHelper.getString(NumberOfWaypointsValidator.class, "tooManyWaypoints"), ValidationMessageCategory.NORMAL);
        }

        return true;
    }

}
