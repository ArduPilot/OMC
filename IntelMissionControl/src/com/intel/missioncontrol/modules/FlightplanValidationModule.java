/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.modules;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.intel.missioncontrol.ui.validation.flightplan.AirspaceDistanceValidator;
import com.intel.missioncontrol.ui.validation.flightplan.AoiCollisionValidator;
import com.intel.missioncontrol.ui.validation.flightplan.ElevationModelConsistencyValidator;
import com.intel.missioncontrol.ui.validation.flightplan.FlightTimeValidator;
import com.intel.missioncontrol.ui.validation.flightplan.FlightplanCoveragePseudoValidator;
import com.intel.missioncontrol.ui.validation.flightplan.FlightplanCoverageTrueValidator;
import com.intel.missioncontrol.ui.validation.flightplan.FlightplanValidationAggregator;
import com.intel.missioncontrol.ui.validation.flightplan.GimbalPitchValidator;
import com.intel.missioncontrol.ui.validation.flightplan.GsdVariationToleranceValidator;
import com.intel.missioncontrol.ui.validation.flightplan.LandingModeValidator;
import com.intel.missioncontrol.ui.validation.flightplan.LineOfSightValidator;
import com.intel.missioncontrol.ui.validation.flightplan.MaxHeightValidator;
import com.intel.missioncontrol.ui.validation.flightplan.MinGroundDistanceValidator;
import com.intel.missioncontrol.ui.validation.flightplan.RecomputeActivatedValidator;
import com.intel.missioncontrol.ui.validation.flightplan.RestrictedCountryValidator;
import com.intel.missioncontrol.ui.validation.flightplan.SdCardCapacityValidator;
import com.intel.missioncontrol.ui.validation.flightplan.WaypointSeparationValidator;

public class FlightplanValidationModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(FlightplanValidationAggregator.Factory.class));
        install(new FactoryModuleBuilder().build(AirspaceDistanceValidator.Factory.class));
        install(new FactoryModuleBuilder().build(FlightplanCoveragePseudoValidator.Factory.class));
        install(new FactoryModuleBuilder().build(FlightplanCoverageTrueValidator.Factory.class));
        install(new FactoryModuleBuilder().build(FlightTimeValidator.Factory.class));
        install(new FactoryModuleBuilder().build(LandingModeValidator.Factory.class));
        install(new FactoryModuleBuilder().build(LineOfSightValidator.Factory.class));
        install(new FactoryModuleBuilder().build(MaxHeightValidator.Factory.class));
        install(new FactoryModuleBuilder().build(MinGroundDistanceValidator.Factory.class));
        install(new FactoryModuleBuilder().build(RestrictedCountryValidator.Factory.class));
        install(new FactoryModuleBuilder().build(SdCardCapacityValidator.Factory.class));
        install(new FactoryModuleBuilder().build(GsdVariationToleranceValidator.Factory.class));
        install(new FactoryModuleBuilder().build(AoiCollisionValidator.Factory.class));
        install(new FactoryModuleBuilder().build(RecomputeActivatedValidator.Factory.class));
        install(new FactoryModuleBuilder().build(ElevationModelConsistencyValidator.Factory.class));
        install(new FactoryModuleBuilder().build(GimbalPitchValidator.Factory.class));
        install(new FactoryModuleBuilder().build(WaypointSeparationValidator.Factory.class));
    }

}
