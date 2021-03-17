/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.google.inject.Inject;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncDoubleProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.drone.BatteryAlertLevel;
import com.intel.missioncontrol.drone.IBattery;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import eu.mavinci.core.helper.StringHelper;
import javafx.beans.binding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatteryValidator implements IFlightValidator {
    public interface Factory {
        BatteryValidator create();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BatteryValidator.class);
    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);

    private final AsyncDoubleProperty remainingChargePercentage = new SimpleAsyncDoubleProperty(this);
    private final AsyncObjectProperty<BatteryAlertLevel> alertLevel = new SimpleAsyncObjectProperty<>(this);
    private final AsyncBooleanProperty telemetryOld = new SimpleAsyncBooleanProperty(this);

    @Inject
    BatteryValidator(IFlightValidationService flightValidationService, ILanguageHelper languageHelper) {
        alertLevel.bind(
            PropertyPath.from(flightValidationService.droneProperty())
                .select(IDrone::batteryProperty)
                .selectReadOnlyAsyncObject(IBattery::alertLevelProperty));

        remainingChargePercentage.bind(
            PropertyPath.from(flightValidationService.droneProperty())
                .select(IDrone::batteryProperty)
                .selectReadOnlyAsyncDouble(IBattery::remainingChargePercentageProperty));

        telemetryOld.bind(
            PropertyPath.from(flightValidationService.droneProperty())
                .select(IDrone::batteryProperty)
                .selectReadOnlyAsyncBoolean(IBattery::telemetryOldProperty));

        validationStatus.bind(
            Bindings.createObjectBinding(
                () -> {
                    if (telemetryOld.get()) {
                        return new FlightValidationStatus(
                            AlertType.LOADING, languageHelper.getString(BatteryValidator.class, "loadingMessage"));
                    }

                    double percentage = remainingChargePercentage.get();
                    BatteryAlertLevel level = alertLevel.get();
                    if (level == null) {
                        level = BatteryAlertLevel.UNKNOWN;
                    }

                    switch (level) {
                    case UNKNOWN:
                        return new FlightValidationStatus(
                            AlertType.LOADING, languageHelper.getString(BatteryValidator.class, "loadingMessage"));
                    case GREEN:
                        return new FlightValidationStatus(
                            AlertType.COMPLETED, languageHelper.getString(BatteryValidator.class, "okMessage"));
                    case YELLOW:
                        return new FlightValidationStatus(
                            AlertType.WARNING,
                            languageHelper.getString(
                                BatteryValidator.class,
                                "yellowLevel",
                                StringHelper.ratioToPercent(percentage / 100, 0, false)));
                    default:
                    case RED:
                        return new FlightValidationStatus(
                            AlertType.ERROR,
                            languageHelper.getString(
                                BatteryValidator.class,
                                "redLevel",
                                StringHelper.ratioToPercent(percentage / 100, 0, false)));
                    }
                },
                remainingChargePercentage,
                alertLevel,
                telemetryOld));
    }

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.BATTERY;
    }
}
