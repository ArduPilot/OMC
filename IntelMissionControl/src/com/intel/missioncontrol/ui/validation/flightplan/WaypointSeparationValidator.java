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
import com.intel.missioncontrol.mission.FlightPlanValidation;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;

/** check spacial separation between waypoints */
public class WaypointSeparationValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        WaypointSeparationValidator create(FlightPlan flightPlan);
    }

    private final String className = WaypointSeparationValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public WaypointSeparationValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightPlan flightplan) {
        super(flightplan, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        boolean checkOk = FlightPlanValidation.validateWaypointSeparation(flightplan);
        if (!checkOk) {
            addWarning(languageHelper.getString(className + ".outOfRange"), ValidationMessageCategory.NORMAL);
        }

        return true;
    }

}
