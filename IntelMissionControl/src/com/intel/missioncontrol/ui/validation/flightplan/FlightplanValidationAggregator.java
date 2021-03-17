/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.flightplan;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.validation.ValidationAggregator;

public class FlightplanValidationAggregator extends ValidationAggregator<FlightPlan> {

    public interface Factory {
        FlightplanValidationAggregator create(FlightPlan flightPlan);
    }

    @Inject
    public FlightplanValidationAggregator(
            AirspaceDistanceValidator.Factory airspaceDistanceValidatorFactory,
            FlightplanCoveragePseudoValidator.Factory flightplanCoveragePseudoValidatorFactory,
            FlightplanCoverageTrueValidator.Factory flightplanCoverageTrueValidatorFactory,
            FlightTimeValidator.Factory flightTimeValidatorFactory,
            LandingModeValidator.Factory landingModeValidatorFactory,
            LineOfSightValidator.Factory lineOfSightValidatorFactory,
            MaxHeightValidator.Factory maxHeightValidatorFactory,
            MinGroundDistanceValidator.Factory minGroundDistanceValidatorFactory,
            RestrictedCountryValidator.Factory restrictedCountryValidatorFactory,
            SdCardCapacityValidator.Factory sdCardCapacityValidatorFactory,
            GsdVariationToleranceValidator.Factory gsdVariationToleranceValidatorFactory,
            AoiCollisionValidator.Factory aoiCollisionValidatorFactory,
            RecomputeActivatedValidator.Factory recomputeActivatedValidatorFactory,
            ElevationModelConsistencyValidator.Factory elevationModelConsistencyValidatorFactory,
            GimbalPitchValidator.Factory gimbalPitchValidatorFactory,
            WaypointSeparationValidator.Factory waypointSeparationValidatorFactory,
            @Assisted FlightPlan flightPlan) {
        super(
            airspaceDistanceValidatorFactory.create(flightPlan),
            flightplanCoveragePseudoValidatorFactory.create(flightPlan),
            flightplanCoverageTrueValidatorFactory.create(flightPlan),
            flightTimeValidatorFactory.create(flightPlan),
            landingModeValidatorFactory.create(flightPlan),
            lineOfSightValidatorFactory.create(flightPlan),
            maxHeightValidatorFactory.create(flightPlan),
            minGroundDistanceValidatorFactory.create(flightPlan),
            restrictedCountryValidatorFactory.create(flightPlan),
            sdCardCapacityValidatorFactory.create(flightPlan),
            gsdVariationToleranceValidatorFactory.create(flightPlan),
            aoiCollisionValidatorFactory.create(flightPlan),
            recomputeActivatedValidatorFactory.create(flightPlan),
            elevationModelConsistencyValidatorFactory.create(flightPlan),
            gimbalPitchValidatorFactory.create(flightPlan),
            waypointSeparationValidatorFactory.create(flightPlan));
    }

}
