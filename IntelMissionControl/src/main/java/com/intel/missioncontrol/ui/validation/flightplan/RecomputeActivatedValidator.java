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
import com.intel.missioncontrol.ui.validation.SimpleResolveAction;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.flightplan.Flightplan;

/** check A-18: check if this mission is in automatic recompute mode */
public class RecomputeActivatedValidator extends OnFlightplanChangedValidator {

    public interface Factory {
        RecomputeActivatedValidator create(FlightPlan flightPlan);
    }

    private final String className = RecomputeActivatedValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public RecomputeActivatedValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightPlan flightplan) {
        super(flightplan, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        Flightplan legacyFlightplan = flightplan.getLegacyFlightplan();

        if (!legacyFlightplan.getRecalculateOnEveryChange()) {
            addWarning(
                languageHelper.getString(className + ".noInAutoRecomputeMode"),
                ValidationMessageCategory.NOTICE,
                new SimpleResolveAction(
                    languageHelper.getString(className + ".activate"),
                    () -> {
                        legacyFlightplan.setRecalculateOnEveryChange(true);
                    }));
        }

        return true;
    }

}
