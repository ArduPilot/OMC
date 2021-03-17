/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.aoi;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.flightplan.PicArea;

/** check A-13: Selected area is too large for plan generation; MAX_NUM_PICS = 10000 */
public class AoiSizeValidator extends OnPicAreaChangedValidator {

    public interface Factory {
        AoiSizeValidator create(PicArea picArea);
    }

    private final String className = AoiSizeValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public AoiSizeValidator(
            ILanguageHelper languageHelper, IQuantityStyleProvider quantityStyleProvider, @Assisted PicArea picArea) {
        super(picArea, quantityStyleProvider);
        this.languageHelper = languageHelper;
        // setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(PicArea picArea) {
        if (!picArea.isValidSizeAOI()) {
            addWarning(languageHelper.getString(className + ".areaTooBig"), ValidationMessageCategory.BLOCKING);
            return true;
        }

        return false;
    }

}
