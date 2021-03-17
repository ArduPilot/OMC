/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.validation.airplane;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.ui.validation.ValidationAggregator;
import com.intel.missioncontrol.ui.validation.ValidatorBase;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.plane.IAirplane;

public class AirplaneValidationAggregator extends ValidationAggregator<FlightplanAirplanePair> {

    public interface Factory {
        AirplaneValidationAggregator create(Flightplan flightplan, IAirplane airplane);
    }

    @Inject
    public AirplaneValidationAggregator(
            AutoLandingSetupValidator.Factory autoLandingSetupValidatorFactory,
            BatteryValidator.Factory batteryValidatorFactory,
            DaylightValidator.Factory daylightValidatorFactory,
            DistanceFirstPointValidator.Factory distanceFirstPointValidatorFactory,
            HardwareCompatibilityValidator.Factory hardwareCompatibilityFactory,
            LandAltValidator.Factory landAltValidatorFactory,
            LandingBoundingBoxValidator.Factory landingBoundingBoxValidatorFactory,
            LineOfSightValidator.Factory lineOfSightValidatorFactory,
            PlaneLicenseValidator.Factory planeLicenseValidatorFactory,
            RestrictedCountryPlaneValidator.Factory restrictedCountryPlaneValidatorFactory,
            StartAltValidator.Factory startAltValidatorFactory,
            UavConnectedValidator.Factory uavConnectedValidatorFactory,
            @Assisted Flightplan flightplan,
            @Assisted IAirplane airplane) {
        super(
            autoLandingSetupValidatorFactory.create(new FlightplanAirplanePair(flightplan, airplane)),
            batteryValidatorFactory.create(new FlightplanAirplanePair(flightplan, airplane)),
            daylightValidatorFactory.create(new FlightplanAirplanePair(flightplan, airplane)),
            distanceFirstPointValidatorFactory.create(new FlightplanAirplanePair(flightplan, airplane)),
            hardwareCompatibilityFactory.create(new FlightplanAirplanePair(flightplan, airplane)),
            landAltValidatorFactory.create(new FlightplanAirplanePair(flightplan, airplane)),
            landingBoundingBoxValidatorFactory.create(new FlightplanAirplanePair(flightplan, airplane)),
            lineOfSightValidatorFactory.create(new FlightplanAirplanePair(flightplan, airplane)),
            planeLicenseValidatorFactory.create(new FlightplanAirplanePair(flightplan, airplane)),
            restrictedCountryPlaneValidatorFactory.create(new FlightplanAirplanePair(flightplan, airplane)),
            startAltValidatorFactory.create(new FlightplanAirplanePair(flightplan, airplane)),
            uavConnectedValidatorFactory.create(new FlightplanAirplanePair(flightplan, airplane)));
    }

    public void changeFlightplan(Flightplan flightplan) {
        for (ValidatorBase<FlightplanAirplanePair> validator : getValidators()) {
            FlightplanAirplanePair flightplanAirplanePair = validator.getValidationValue();
            flightplanAirplanePair.setFlightplan(flightplan);
        }
    }

}
