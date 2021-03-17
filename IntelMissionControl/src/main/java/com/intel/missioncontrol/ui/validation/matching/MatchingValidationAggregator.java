/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.matching;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.ui.validation.ValidationAggregator;

public class MatchingValidationAggregator extends ValidationAggregator<Matching> {

    public interface Factory {
        MatchingValidationAggregator create(Matching flightPlan);
    }

    @Inject
    public MatchingValidationAggregator(
            ExifValidator.Factory exifValidatorFactory,
            ImageResolutionValidator.Factory imageResolutionValidatorFactory,
            LogsAndImagesMatchesValidator.Factory logsAndImagesMatchesValidatorFactory,
            MatchingCoverageValidator.Factory matchingCoverageValidatorFactory,
            RtkLocationConfirmedValidator.Factory rtkLocationConfirmedValidatorFactory,
            AvailableSpaceToCopyValidator.Factory avaliableSpaceToCopyValidatorFactory,
            @Assisted Matching matching) {
        super(
            exifValidatorFactory.create(matching),
            imageResolutionValidatorFactory.create(matching),
            logsAndImagesMatchesValidatorFactory.create(matching),
            matchingCoverageValidatorFactory.create(matching),
            rtkLocationConfirmedValidatorFactory.create(matching),
            avaliableSpaceToCopyValidatorFactory.create(matching));
    }

}
