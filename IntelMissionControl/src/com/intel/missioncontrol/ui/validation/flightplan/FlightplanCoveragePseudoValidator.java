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
import eu.mavinci.desktop.helper.MathHelper;

/** check A-07b: estimated AOI coverage is below tolerance level */
public class FlightplanCoveragePseudoValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        FlightplanCoveragePseudoValidator create(FlightPlan flightPlan);
    }

    private final String className = FlightplanCoveragePseudoValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public FlightplanCoveragePseudoValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightPlan flightplan) {
        super(flightplan, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        if (flightplan.isSimulatedTimeValidProperty().get() && flightplan.allAoisSizeValidProperty().get()) {
            double pseudoOrtho = flightplan.getLegacyFlightplan().getFPcoverage().getCoverageRatioPseudoOrtho();
            if (pseudoOrtho < 0) {
                return false;
            }

            if (pseudoOrtho < 0.995) {
                addWarning(
                    languageHelper.getString(
                        className + ".pseudoOrthoCoverageSmall", MathHelper.round(100 * pseudoOrtho, 1)),
                    ValidationMessageCategory.NOTICE);
            }
        }

        return true;
    }

}
