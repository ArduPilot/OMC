/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.aoi;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.Ensure;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;

/** check A-15 check if this UAV is capable of flying the AOI type */
public class AoiCompatibilityValidator extends OnPicAreaChangedValidator {

    public interface Factory {
        AoiCompatibilityValidator create(PicArea picArea);
    }

    private final String className = AoiCompatibilityValidator.class.getName();
    private ILanguageHelper languageHelper;

    @Inject
    public AoiCompatibilityValidator(
            ILanguageHelper languageHelper, IQuantityStyleProvider quantityStyleProvider, @Assisted PicArea picArea) {
        super(picArea, quantityStyleProvider);
        this.languageHelper = languageHelper;
        // setOkMessage(languageHelper.getString(className+".okMessage"));//dont show it by intention
    }

    @Override
    protected boolean onInvalidated(PicArea picArea) {
        Flightplan flightplan = picArea.getFlightplan();
        Ensure.notNull(flightplan, "flightplan");
        IPlatformDescription platformDescription = flightplan.getHardwareConfiguration().getPlatformDescription();

        if (platformDescription.isInCopterMode()
                    && !picArea.getPlanType()
                        .isSelectable(
                            true, platformDescription.getMinWaypointSeparation().getValue().doubleValue() == 0)
                || platformDescription.isInFixedWingEditionMode()
                    && !picArea.getPlanType()
                        .isSelectable(
                            false, platformDescription.getMinWaypointSeparation().getValue().doubleValue() == 0)) {
            addWarning(
                languageHelper.getString(className + ".cantFlyThis", picArea.getPlanType())
                // ,new SimpleResolveAction(languageHelper.getString(className + ".show")) //reinclude later when show
                // concept works
                // ,new SimpleResolveAction(languageHelper.getString(className + ".changePlane")) //reinclude later when
                // show concept works
                ,
                ValidationMessageCategory.BLOCKING);
        }

        return true;
    }

}
