/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;

public interface IFlightValidator {
    ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty();

    default FlightValidationStatus getValidationStatus() {
        return validationStatusProperty().get();
    }

    FlightValidatorType getFlightValidatorType();
}
