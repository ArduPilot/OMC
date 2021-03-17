/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.mission.FlightPlan;
import javafx.collections.ObservableList;

public interface IFlightValidationService extends IFlightValidator {
    ReadOnlyAsyncListProperty<IFlightValidator> validatorsProperty();

    default ObservableList<IFlightValidator> getValidators() {
        return validatorsProperty().get();
    }

    ReadOnlyAsyncObjectProperty<FlightValidationStatus> combinedStatusProperty();

    default FlightValidationStatus getCombinedStatus() {
        return combinedStatusProperty().get();
    }

    AsyncObjectProperty<IDrone> droneProperty();

    AsyncObjectProperty<FlightPlan> flightPlanProperty();
}
