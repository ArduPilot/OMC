/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import java.time.Duration;
import java.util.Arrays;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;

public class AnnoyingTestValidator implements IFlightValidator {

    public interface Factory {
        AnnoyingTestValidator create(CancellationSource cancellationSource);
    }

    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);

    private enum State {
        UPDATING,
        OK,
        WARNING,
        ERROR;

        private static State get(int i) {
            return Arrays.stream(State.values())
                .filter(s -> s.ordinal() == i)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("i out of range"));
        }

        State getNext() {
            return ordinal() < State.values().length - 1 ? get(ordinal() + 1) : get(0);
        }
    }

    private State state = State.UPDATING;

    @Inject
    AnnoyingTestValidator(
            IFlightValidationService flightValidationService, @Assisted CancellationSource cancellationSource) {
        onInvalidated();
        Dispatcher.background()
            .runLaterAsync(this::onInvalidated, Duration.ofSeconds(2), Duration.ofSeconds(2), cancellationSource);
    }

    private void onInvalidated() {
        State currentState = state;
        state = currentState.getNext();

        switch (currentState) {
        case UPDATING:
            validationStatus.setValue(new FlightValidationStatus(AlertType.LOADING, "Test updating"));
            return;
        case OK:
            validationStatus.setValue(new FlightValidationStatus(AlertType.COMPLETED, "Test ok"));
            return;
        case WARNING:
            validationStatus.setValue(new FlightValidationStatus(AlertType.WARNING, "Test Warning"));
            return;
        case ERROR:
        default:
            validationStatus.setValue(new FlightValidationStatus(AlertType.ERROR, "Test Error"));
        }
    }

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.ANNOYING_TEST;
    }

}
