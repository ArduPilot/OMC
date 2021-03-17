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
import com.intel.missioncontrol.ui.validation.SimpleResolveAction;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.desktop.helper.FileHelper;
import eu.mavinci.desktop.main.debug.Debug;
import eu.mavinci.flightplan.PicArea;
import java.io.IOException;
import java.util.logging.Level;

/**
 * check A-14: licence of one of the UAVs of selected type does not allow the execution of the flight plan with specific
 * AOIs
 */
public class AoiLicenseValidator extends OnPicAreaChangedValidator {

    public interface Factory {
        AoiLicenseValidator create(PicArea picArea);
    }

    private final String className = AoiLicenseValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public AoiLicenseValidator(
            ILanguageHelper languageHelper, IQuantityStyleProvider quantityStyleProvider, @Assisted PicArea picArea) {
        super(picArea, quantityStyleProvider);
        this.languageHelper = languageHelper;
        // setOkMessage(languageHelper.getString(className+".okMessage"));//dont show it by intention
    }

    @Override
    protected boolean onInvalidated(PicArea picArea) {
        // TODO FIXME ... we need an actual implementation here!!
        // if (picArea.getPlanType() != PlanType.COPTER3D) {
        if (false) {
            addWarning(
                languageHelper.getString(className + ".wrongLicence", picArea.getPlanType()),
                ValidationMessageCategory.NORMAL,
                new SimpleResolveAction(
                    languageHelper.getString(className + ".buy"),
                    () -> {
                        try {
                            FileHelper.openFileOrURL("http://drones.intel.com"); // TODO fixme.. correct URL
                        } catch (IOException e) {
                            Debug.getLog().log(Level.WARNING, "can open URL", e);
                        }
                    }));
        }

        return true;
    }

}
