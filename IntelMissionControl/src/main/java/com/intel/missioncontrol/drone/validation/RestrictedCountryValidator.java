/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import eu.mavinci.geo.ICountryDetector;
import gov.nasa.worldwind.geom.Position;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyHelper;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;

public class RestrictedCountryValidator implements IFlightValidator {
    public interface Factory {
        RestrictedCountryValidator create(CancellationSource cancellationSource);
    }

    private final Duration updateInterval = Duration.ofSeconds(1);

    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);

    private final AsyncObjectProperty<Position> position = new SimpleAsyncObjectProperty<>(this);
    private final AsyncBooleanProperty telemetryOld = new SimpleAsyncBooleanProperty(this);

    @Inject
    RestrictedCountryValidator(
            IFlightValidationService flightValidationService,
            ILanguageHelper languageHelper,
            ICountryDetector countryDetector,
            @Assisted CancellationSource cancellationSource) {
        telemetryOld.set(true);

        // update position periodically:
        Dispatcher dispatcher = Dispatcher.background();
        dispatcher.runLaterAsync(
            () -> {
                IDrone drone = flightValidationService.droneProperty().get();
                PropertyHelper.setValueSafe(position, drone != null ? drone.positionProperty().get() : null);
                PropertyHelper.setValueSafe(telemetryOld, drone == null || drone.positionTelemetryOldProperty().get());
            },
            Duration.ZERO,
            updateInterval,
            cancellationSource);

        validationStatus.bind(
            Bindings.createObjectBinding(
                () -> {
                    Position pos = position.get();
                    if (pos == null || telemetryOld.get()) {
                        return new FlightValidationStatus(
                            AlertType.LOADING,
                            languageHelper.getString(RestrictedCountryValidator.class, "loadingMessage"));
                    }

                    if (!countryDetector.allowProceed(pos)) {
                        return new FlightValidationStatus(
                            AlertType.WARNING,
                            languageHelper.getString(
                                RestrictedCountryValidator.class, "restricted", countryDetector.getFirstCountry(pos)));
                    }

                    return new FlightValidationStatus(
                        AlertType.COMPLETED, languageHelper.getString(RestrictedCountryValidator.class, "okMessage"));
                },
                position,
                telemetryOld));
    }

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.RESTRICTED_COUNTRY;
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
