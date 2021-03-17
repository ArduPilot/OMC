/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.modules;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.intel.missioncontrol.drone.validation.AnnoyingTestValidator;
import com.intel.missioncontrol.drone.validation.AutomaticModeValidator;
import com.intel.missioncontrol.drone.validation.BatteryValidator;
import com.intel.missioncontrol.drone.validation.DaylightValidator;
import com.intel.missioncontrol.drone.validation.FlightPlanWarningsValidator;
import com.intel.missioncontrol.drone.validation.GnssFixValidator;
import com.intel.missioncontrol.drone.validation.HardwareCompatibilityValidator;
import com.intel.missioncontrol.drone.validation.RemoteControlValidator;
import com.intel.missioncontrol.drone.validation.RestrictedCountryValidator;
import com.intel.missioncontrol.drone.validation.SensorCalibrationValidator;
import com.intel.missioncontrol.drone.validation.StorageValidator;
import com.intel.missioncontrol.drone.validation.TakeoffPositionValidator;

public class FlightValidationModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(AnnoyingTestValidator.Factory.class));
        install(new FactoryModuleBuilder().build(AutomaticModeValidator.Factory.class));
        install(new FactoryModuleBuilder().build(BatteryValidator.Factory.class));
        install(new FactoryModuleBuilder().build(DaylightValidator.Factory.class));
        install(new FactoryModuleBuilder().build(FlightPlanWarningsValidator.Factory.class));
        install(new FactoryModuleBuilder().build(GnssFixValidator.Factory.class));
        install(new FactoryModuleBuilder().build(HardwareCompatibilityValidator.Factory.class));
        install(new FactoryModuleBuilder().build(RemoteControlValidator.Factory.class));
        install(new FactoryModuleBuilder().build(RestrictedCountryValidator.Factory.class));
        install(new FactoryModuleBuilder().build(SensorCalibrationValidator.Factory.class));
        install(new FactoryModuleBuilder().build(StorageValidator.Factory.class));
        install(new FactoryModuleBuilder().build(TakeoffPositionValidator.Factory.class));
    }

}
