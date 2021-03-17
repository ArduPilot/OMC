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

/** check A-07a: estimated AOI coverage is below tolerance level */
public class FlightplanCoverageTrueValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        FlightplanCoverageTrueValidator create(FlightPlan flightPlan);
    }

    private final String className = FlightplanCoverageTrueValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public FlightplanCoverageTrueValidator(
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
            double ortho = flightplan.getLegacyFlightplan().getFPcoverage().getCoverageRatioOrtho();
            if (ortho < 0) {
                return false;
            }

            if (ortho < 0.995) {
                addWarning(
                    languageHelper.getString(className + ".orthoCoverageSmall", MathHelper.round(100 * ortho, 1)),
                    ValidationMessageCategory.NOTICE);
            }
        }

        return true;
    }

}
