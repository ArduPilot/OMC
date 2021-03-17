/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.flightplan;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.validation.ValidationMessageCategory;
import eu.mavinci.flightplan.computation.FPsim;

/** check A-16: actual estimated data size exceeds SD card capacity */
public class SdCardCapacityValidator extends OnFlightplanRecomputedValidator {

    public interface Factory {
        SdCardCapacityValidator create(FlightPlan flightPlan);
    }

    private final String className = SdCardCapacityValidator.class.getName();
    private final ILanguageHelper languageHelper;

    @Inject
    public SdCardCapacityValidator(
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider,
            @Assisted FlightPlan flightplan) {
        super(flightplan, quantityStyleProvider);
        this.languageHelper = languageHelper;
        setOkMessage(languageHelper.getString(className + ".okMessage"));
    }

    @Override
    protected boolean onInvalidated(FlightPlan flightplan) {
        if (flightplan.isSimulatedTimeValidProperty().get()) {
            FPsim.SimResultData simResult = flightplan.getLegacyFlightplan().getFPsim().getSimResult();
            if (simResult == null) {
                return false;
            }

            int picCount = simResult.pic_count;
            IGenericCameraDescription cameraDesc =
                flightplan
                    .getLegacyFlightplan()
                    .getHardwareConfiguration()
                    .getPrimaryPayload(IGenericCameraConfiguration.class)
                    .getDescription();
            double estSize = picCount * cameraDesc.getPictureSizeInMB();

            if (estSize >= cameraDesc.getSdCapacityInGB() * 1024) {
                addWarning(
                    languageHelper.getString(
                        className + ".tooSmall",
                        "" + picCount,
                        formatBytes((long)(estSize * 1024 * 1024)),
                        formatBytes((long)(cameraDesc.getSdCapacityInGB() * 1024 * 1024 * 1024))),
                    ValidationMessageCategory.NORMAL);
            }
        }

        return true;
    }

}
