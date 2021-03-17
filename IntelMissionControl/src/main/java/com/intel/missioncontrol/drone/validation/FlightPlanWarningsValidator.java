/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.google.inject.Inject;
import com.intel.missioncontrol.StaticInjector;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import com.intel.missioncontrol.ui.validation.ResolvableValidationMessage;
import com.intel.missioncontrol.ui.validation.ValidationService;
import java.util.Objects;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.concurrent.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Check if mission service reports errors */
public class FlightPlanWarningsValidator implements IFlightValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightPlanWarningsValidator.class);

    public interface Factory {
        FlightPlanWarningsValidator create();
    }

    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<FlightPlan> flightPlan = new UIAsyncObjectProperty<>(this);

    private ValidationService validationService;

    @Inject
    FlightPlanWarningsValidator(IFlightValidationService flightValidationService, ILanguageHelper languageHelper) {
        flightPlan.bind(flightValidationService.flightPlanProperty());

        Dispatcher dispatcher = Dispatcher.platform();
        dispatcher.run(
            () -> {
                validationService = StaticInjector.getInstance(ValidationService.class);

                validationStatus.bind(
                    Bindings.createObjectBinding(
                        () -> {
                            if (!Platform.isFxApplicationThread()) {
                                LOGGER.error("validationStatus binding not on JavaFX application thread");
                                throw new IllegalStateException("Not on JavaFX application thread");
                            }

                            FlightPlan fp = flightPlan.get();
                            ValidationService validationService = this.validationService;
                            FlightPlan validatorServiceFp =
                                validationService != null ? validationService.observedFlightPlanProperty().get() : null;
                            if (fp == null || !Objects.equals(fp, validatorServiceFp)) {
                                return new FlightValidationStatus(
                                    AlertType.LOADING,
                                    languageHelper.getString(FlightPlanWarningsValidator.class, "loadingMessage"));
                            }

                            ObservableList<ResolvableValidationMessage> planningValidationMessages =
                                validationService.planningValidationMessagesProperty().get();
                            int msgCount = planningValidationMessages == null ? 0 : planningValidationMessages.size();

                            if (msgCount == 0) {
                                return new FlightValidationStatus(
                                    AlertType.COMPLETED,
                                    languageHelper.getString(FlightPlanWarningsValidator.class, "okMessage"));
                            } else {
                                return new FlightValidationStatus(
                                    AlertType.WARNING,
                                    languageHelper.getString(
                                        FlightPlanWarningsValidator.class, "flightPlanHasMessages"));
                            }
                        },
                        validationService.planningValidationMessagesProperty(),
                        validationService.observedFlightPlanProperty(),
                        flightPlan));
            });
    }

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.FLIGHTPLAN_WARNINGS;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<IResolveAction> getFirstResolveAction() {
        return null;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<IResolveAction> getSecondResolveAction() {
        return null;
    }
}
