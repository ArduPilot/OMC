/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.aoi;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.flightplan.PicArea;

/** show messages if something was wrong with AOI computation */
public class ComputeFailValidator extends OnPicAreaChangedValidator {

    public interface Factory {
        ComputeFailValidator create(PicArea picArea);
    }

    private final String className = ComputeFailValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public ComputeFailValidator(
            ILanguageHelper languageHelper, IQuantityStyleProvider quantityStyleProvider, @Assisted PicArea picArea) {
        super(picArea, quantityStyleProvider);
        this.languageHelper = languageHelper;
        // setOkMessage(languageHelper.getString(className+".okMessage"));//dont show it by intention
    }

    @Override
    protected boolean onInvalidated(PicArea picArea) {
        PicArea.RecomputeErrors lastError = picArea.getLastError();
        if (lastError != null) {
            addWarning(
                languageHelper.getString(className + "." + lastError.name()), ValidationMessageCategory.BLOCKING);
            return false;
        }

        return true;
    }

}
