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
import eu.mavinci.desktop.helper.MathHelper;

/** check D-07: Estimated AOI coverage is below tolerance level */
public class MatchingCoverageValidator extends MatchingValidatorBase {
    public interface Factory {
        MatchingCoverageValidator create(Matching flightPlan);
    }

    private final String className = MatchingCoverageValidator.class.getName();
    private final ILanguageHelper languageHelper;

    @Inject
    public MatchingCoverageValidator(
            ILanguageHelper languageHelper, IQuantityStyleProvider quantityStyleProvider, @Assisted Matching matching) {
        super(matching, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
        addDependencies(
            matching.trueOrthoRatioProperty(), matching.pseudoOrthoRatioProperty(), matching.statusProperty());
    }

    @Override
    protected boolean onInvalidated(Matching matching) {
        if (matching.getStatus() != MatchingStatus.IMPORTED) {
            return false;
        }

        double ortho = matching.trueOrthoRatioProperty().get();
        double pseudo = matching.pseudoOrthoRatioProperty().get();
        if (ortho >= 0 && ortho < 0.995) {
            addWarning(
                languageHelper.getString(className + ".orthoCoverageSmall", MathHelper.round(100 * ortho, 1)),
                ValidationMessageCategory.NOTICE);
        }

        if (pseudo >= 0 && pseudo < 0.995) {
            addWarning(
                languageHelper.getString(className + ".pseudoOrthoCoverageSmall", MathHelper.round(100 * pseudo, 1)),
                ValidationMessageCategory.NOTICE);
        }

        return true;
    }

}
