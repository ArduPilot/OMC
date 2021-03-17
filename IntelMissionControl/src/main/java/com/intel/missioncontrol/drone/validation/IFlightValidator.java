/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.intel.missioncontrol.ui.validation.IResolveAction;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;

public interface IFlightValidator {
    ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty();

    default FlightValidationStatus getValidationStatus() {
        return validationStatusProperty().get();
    }

    FlightValidatorType getFlightValidatorType();

    ReadOnlyAsyncObjectProperty<IResolveAction> getFirstResolveAction();

    ReadOnlyAsyncObjectProperty<IResolveAction> getSecondResolveAction();

}
