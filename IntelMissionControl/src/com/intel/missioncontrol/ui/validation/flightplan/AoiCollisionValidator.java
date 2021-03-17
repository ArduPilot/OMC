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
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.computation.FPsim;

/** check A-20: check if this flight plan collides with some 3D PicAreas */
public class AoiCollisionValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        AoiCollisionValidator create(FlightPlan flightPlan);
    }

    private final String className = AoiCollisionValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public AoiCollisionValidator(
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

        for (PicArea picArea : simResult.aoiCollisionsTakeoff) {
            // lower criticallity for takeoff path, since this is often anyway handflown, so user needs option to
            // overwrite warning
            addWarning(
                languageHelper.getString(className + ".collisionTakeoff", picArea.getName()),
                ValidationMessageCategory.NORMAL);
        }

        for (PicArea picArea : simResult.aoiCollisions) {
            addWarning(
                languageHelper.getString(className + ".collision", picArea.getName()),
                ValidationMessageCategory.BLOCKING);
        }

        return true;
    }

}
