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
import com.intel.missioncontrol.mission.FlightPlanValidation;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.core.helper.StringHelper;
import eu.mavinci.flightplan.computation.FPsim;

/** check A-11: actual max estimated flight time longer than max battery life (assuming its full) */
public class FlightTimeValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        FlightTimeValidator create(FlightPlan flightPlan);
    }

    private final String className = FlightTimeValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public FlightTimeValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightPlan flightplan) {
        super(flightplan, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        FPsim fpsim = flightplan.getLegacyFlightplan().getFPsim();
        if (fpsim == null) {
            return false;
        }

        var simResult = fpsim.getSimResult();
        if (simResult == null) {
            return false;
        }

        double flightTime = simResult.flightTime;
        double batteryTime =
            flightplan
                .getLegacyFlightplan()
                .getHardwareConfiguration()
                .getPlatformDescription()
                .getMaxFlightTime()
                .convertTo(Unit.SECOND)
                .getValue()
                .doubleValue();

        double alternateFlighttime = FlightPlanValidation.estimateFlightTime(flightplan);

        if (!simResult.simulatedTimeValid) {
            if (fpsim.getSimulatedTimeMessage() != null
                    && fpsim.getSimulatedTimeMessage().equals("tooLongOverSimTimeMax")) {
                addWarning(
                    languageHelper.getString(
                        className + ".tooLongOverSimTimeMax",
                        StringHelper.secToShortDHMS(flightTime),
                        StringHelper.secToShortDHMS(batteryTime)),
                    ValidationMessageCategory.BLOCKING);
            }

            return true;
        } else {
            if (fpsim.getSimulatedTimeMessage() != null && fpsim.getSimulatedTimeMessage().equals("tooLong")) {
                addWarning(
                    languageHelper.getString(
                        className + ".tooLong",
                        StringHelper.secToShortDHMS(flightTime),
                        StringHelper.secToShortDHMS(batteryTime)),
                    ValidationMessageCategory.NORMAL);
            }

            return true;
        }
    }
}
