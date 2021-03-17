/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.modules;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.intel.missioncontrol.ui.validation.airplane.AirplaneValidationAggregator;
import com.intel.missioncontrol.ui.validation.airplane.AutoLandingSetupValidator;
import com.intel.missioncontrol.ui.validation.airplane.BatteryValidator;
import com.intel.missioncontrol.ui.validation.airplane.DaylightValidator;
import com.intel.missioncontrol.ui.validation.airplane.DistanceFirstPointValidator;
import com.intel.missioncontrol.ui.validation.airplane.HardwareCompatibilityValidator;
import com.intel.missioncontrol.ui.validation.airplane.LandAltValidator;
import com.intel.missioncontrol.ui.validation.airplane.LandingBoundingBoxValidator;
import com.intel.missioncontrol.ui.validation.airplane.LineOfSightValidator;
import com.intel.missioncontrol.ui.validation.airplane.PlaneLicenseValidator;
import com.intel.missioncontrol.ui.validation.airplane.RestrictedCountryPlaneValidator;
import com.intel.missioncontrol.ui.validation.airplane.StartAltValidator;
import com.intel.missioncontrol.ui.validation.airplane.UavConnectedValidator;

public class AirplaneValidationModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(AirplaneValidationAggregator.Factory.class));
        install(new FactoryModuleBuilder().build(AutoLandingSetupValidator.Factory.class));
        install(new FactoryModuleBuilder().build(BatteryValidator.Factory.class));
        install(new FactoryModuleBuilder().build(DaylightValidator.Factory.class));
        install(new FactoryModuleBuilder().build(DistanceFirstPointValidator.Factory.class));
        install(new FactoryModuleBuilder().build(HardwareCompatibilityValidator.Factory.class));
        install(new FactoryModuleBuilder().build(LandAltValidator.Factory.class));
        install(new FactoryModuleBuilder().build(LandingBoundingBoxValidator.Factory.class));
        install(new FactoryModuleBuilder().build(LineOfSightValidator.Factory.class));
        install(new FactoryModuleBuilder().build(PlaneLicenseValidator.Factory.class));
        install(new FactoryModuleBuilder().build(RestrictedCountryPlaneValidator.Factory.class));
        install(new FactoryModuleBuilder().build(StartAltValidator.Factory.class));
        install(new FactoryModuleBuilder().build(UavConnectedValidator.Factory.class));
    }

}
