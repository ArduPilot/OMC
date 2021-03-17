/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.google.inject.Inject;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.IHealth;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import javafx.beans.binding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Check if sensors are calibrated */
public class SensorCalibrationValidator implements IFlightValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorCalibrationValidator.class);

    public interface Factory {
        SensorCalibrationValidator create();
    }

    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);

    @Inject
    SensorCalibrationValidator(IFlightValidationService flightValidationService, ILanguageHelper languageHelper) {
        ReadOnlyAsyncObjectProperty<IHealth.CalibrationStatus> calibrationStatus =
            PropertyPath.from(flightValidationService.droneProperty())
                .select(IDrone::healthProperty)
                .selectReadOnlyAsyncObject(IHealth::calibrationStatusProperty);

        validationStatus.bind(
            Bindings.createObjectBinding(
                () -> {
                    IHealth.CalibrationStatus cs = calibrationStatus.get();

                    if (cs == IHealth.CalibrationStatus.CALIBRATION_NEEDED) {
                        return new FlightValidationStatus(
                            AlertType.COMPLETED,
                            languageHelper.getString(SensorCalibrationValidator.class, "calibrationNeeded"));
                    }

                    if (cs == IHealth.CalibrationStatus.OK) {
                        return new FlightValidationStatus(
                            AlertType.COMPLETED,
                            languageHelper.getString(SensorCalibrationValidator.class, "okMessage"));
                    }

                    return new FlightValidationStatus(
                        AlertType.LOADING,
                        languageHelper.getString(SensorCalibrationValidator.class, "loadingMessage"));
                },
                calibrationStatus));
    }

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.SENSOR_CALIBRATION;
    }
}
