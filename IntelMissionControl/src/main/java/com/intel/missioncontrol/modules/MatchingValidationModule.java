/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.modules;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.intel.missioncontrol.ui.validation.matching.AvailableSpaceToCopyValidator;
import com.intel.missioncontrol.ui.validation.matching.ExifValidator;
import com.intel.missioncontrol.ui.validation.matching.ImageResolutionValidator;
import com.intel.missioncontrol.ui.validation.matching.LogsAndImagesMatchesValidator;
import com.intel.missioncontrol.ui.validation.matching.MatchingCoverageValidator;
import com.intel.missioncontrol.ui.validation.matching.MatchingValidationAggregator;
import com.intel.missioncontrol.ui.validation.matching.RtkLocationConfirmedValidator;

public class MatchingValidationModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(AvailableSpaceToCopyValidator.Factory.class));
        install(new FactoryModuleBuilder().build(ExifValidator.Factory.class));
        install(new FactoryModuleBuilder().build(ImageResolutionValidator.Factory.class));
        install(new FactoryModuleBuilder().build(LogsAndImagesMatchesValidator.Factory.class));
        install(new FactoryModuleBuilder().build(MatchingCoverageValidator.Factory.class));
        install(new FactoryModuleBuilder().build(MatchingValidationAggregator.Factory.class));
        install(new FactoryModuleBuilder().build(RtkLocationConfirmedValidator.Factory.class));
    }

}
