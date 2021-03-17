/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.aoi;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.ui.validation.ValidationAggregator;
import eu.mavinci.flightplan.PicArea;

public class AoiValidationAggregator extends ValidationAggregator<PicArea> {

    public interface Factory {
        AoiValidationAggregator create(PicArea picArea);
    }

    private PicArea picArea;

    @Inject
    public AoiValidationAggregator(
            AoiCompatibilityValidator.Factory aoiCompatibilityValidatorFactory,
            AoiLicenseValidator.Factory aoiLicenseValidatorFactory,
            MotionBlurValidator.Factory motionBlurValidatorFactory,
            OverlapValidator.Factory overlapValidatorFactory,
            ComputeFailValidator.Factory computeFailValidatorFactory,
            Restrictions3DValidator.Factory restrictions3DValidatorFactory,
            AoiSizeValidator.Factory aoiSizeValidatorFactory,
            @Assisted PicArea picArea) {
        super(
            aoiCompatibilityValidatorFactory.create(picArea),
            aoiLicenseValidatorFactory.create(picArea),
            motionBlurValidatorFactory.create(picArea),
            overlapValidatorFactory.create(picArea),
            computeFailValidatorFactory.create(picArea),
            restrictions3DValidatorFactory.create(picArea),
            aoiSizeValidatorFactory.create(picArea));
        this.picArea = picArea;
    }

    public void changePicArea(PicArea picArea) {
        this.picArea = picArea;
        setValidationValue(picArea);
    }

    public PicArea getPicArea() {
        return picArea;
    }

}
