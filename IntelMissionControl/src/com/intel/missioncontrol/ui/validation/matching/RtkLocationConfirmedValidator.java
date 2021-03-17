/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.matching;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.MatchingStatus;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.LocationType;

/** check D-04: If image resolution fits to selected hardware */
public class RtkLocationConfirmedValidator extends MatchingValidatorBase {

    public interface Factory {
        RtkLocationConfirmedValidator create(Matching flightPlan);
    }

    private final String className = RtkLocationConfirmedValidator.class.getName();
    private final ILanguageHelper languageHelper;

    @Inject
    public RtkLocationConfirmedValidator(
            ILanguageHelper languageHelper, IQuantityStyleProvider quantityStyleProvider, @Assisted Matching matching) {
        super(matching, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
        addDependencies(
            matching.rtkAvailableProperty(),
            matching.rtkLocationConfirmedProperty(),
            matching.rtkBaseLocationTypeProperty(),
            matching.statusProperty());
    }

    @Override
    protected boolean onInvalidated(Matching matching) {
        if (matching.getStatus() != MatchingStatus.IMPORTED) return false;
        if (!matching.rtkAvailableProperty().get()) {
            return false;
        }

        if (matching.rtkBaseLocationTypeProperty().get() == LocationType.ASSUMED
                && !matching.rtkLocationConfirmedProperty().get()) {
            addWarning(
                languageHelper.getString(className + ".imgRtkLocationWarning"), ValidationMessageCategory.NORMAL);
        }

        return true;
    }

}
