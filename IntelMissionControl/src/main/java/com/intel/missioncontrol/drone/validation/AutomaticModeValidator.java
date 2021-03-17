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
import com.intel.missioncontrol.drone.AutopilotState;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import de.saxsys.mvvmfx.internal.viewloader.DependencyInjector;
import javafx.beans.binding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Check if drone is in automatic mode */
public class AutomaticModeValidator implements IFlightValidator {
    public interface Factory {
        AutomaticModeValidator create();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticModeValidator.class);

    private final ILanguageHelper languageHelper =
        DependencyInjector.getInstance().getInstanceOf(ILanguageHelper.class);

    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);

    @Inject
    AutomaticModeValidator(IFlightValidationService flightValidationService) {
        ReadOnlyAsyncObjectProperty<AutopilotState> autopilotState =
            PropertyPath.from(flightValidationService.droneProperty())
                .selectReadOnlyAsyncObject(IDrone::autopilotStateProperty);

        validationStatus.bind(
            Bindings.createObjectBinding(
                () -> {
                    AutopilotState s = autopilotState.get();
                    if (s == null) {
                        s = AutopilotState.UNKNOWN;
                    }

                    switch (s) {
                    case MANUAL:
                        return new FlightValidationStatus(
                            AlertType.WARNING,
                            languageHelper.getString(AutomaticModeValidator.class, "manualModeMessage"));
                    case AUTOPILOT:
                        return new FlightValidationStatus(
                            AlertType.COMPLETED, languageHelper.getString(AutomaticModeValidator.class, "okMessage"));
                    case UNKNOWN:
                    default:
                        return new FlightValidationStatus(
                            AlertType.LOADING,
                            languageHelper.getString(AutomaticModeValidator.class, "loadingMessage"));
                    }
                },
                autopilotState));
    }

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.AUTOMATIC_MODE;
    }
}
