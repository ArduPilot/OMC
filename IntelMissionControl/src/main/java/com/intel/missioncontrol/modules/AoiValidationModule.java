/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.modules;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.intel.missioncontrol.ui.validation.aoi.AoiCompatibilityValidator;
import com.intel.missioncontrol.ui.validation.aoi.AoiLicenseValidator;
import com.intel.missioncontrol.ui.validation.aoi.AoiSizeValidator;
import com.intel.missioncontrol.ui.validation.aoi.AoiValidationAggregator;
import com.intel.missioncontrol.ui.validation.aoi.ComputeFailValidator;
import com.intel.missioncontrol.ui.validation.aoi.MotionBlurValidator;
import com.intel.missioncontrol.ui.validation.aoi.OverlapValidator;
import com.intel.missioncontrol.ui.validation.aoi.Restrictions3DValidator;

public class AoiValidationModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(AoiValidationAggregator.Factory.class));
        install(new FactoryModuleBuilder().build(AoiCompatibilityValidator.Factory.class));
        install(new FactoryModuleBuilder().build(AoiLicenseValidator.Factory.class));
        install(new FactoryModuleBuilder().build(AoiSizeValidator.Factory.class));
        install(new FactoryModuleBuilder().build(ComputeFailValidator.Factory.class));
        install(new FactoryModuleBuilder().build(MotionBlurValidator.Factory.class));
        install(new FactoryModuleBuilder().build(OverlapValidator.Factory.class));
        install(new FactoryModuleBuilder().build(Restrictions3DValidator.Factory.class));
    }

}
