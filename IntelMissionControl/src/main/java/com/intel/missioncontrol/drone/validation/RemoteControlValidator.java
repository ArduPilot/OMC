/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import java.util.ArrayList;
import java.util.List;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.IRemoteControl;
import com.intel.missioncontrol.drone.RemoteControl;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import javafx.beans.binding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Check if remote control is connected and has no errors */
public class RemoteControlValidator implements IFlightValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteControlValidator.class);

    public interface Factory {
        RemoteControlValidator create();
    }

    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);

    @Inject
    RemoteControlValidator(IFlightValidationService flightValidationService, ILanguageHelper languageHelper) {
        ReadOnlyAsyncObjectProperty<RemoteControl.Status> rcStatus =
            PropertyPath.from(flightValidationService.droneProperty())
                .select(IDrone::remoteControlProperty)
                .selectReadOnlyAsyncObject(IRemoteControl::statusProperty);

        validationStatus.bind(
            Bindings.createObjectBinding(
                () -> {
                    RemoteControl.Status s = rcStatus.get();
                    if (s == null) {
                        s = RemoteControl.Status.UNKNOWN;
                    }

                    switch (s) {
                    case NO_REMOTE_CONTROL:
                        return new FlightValidationStatus(
                            AlertType.ERROR, languageHelper.getString(RemoteControlValidator.class, "noRc"));
                    case REMOTE_CONTROL_ERROR:
                        return new FlightValidationStatus(
                            AlertType.ERROR, languageHelper.getString(RemoteControlValidator.class, "rcError"));
                    case OK:
                        return new FlightValidationStatus(
                            AlertType.COMPLETED, languageHelper.getString(RemoteControlValidator.class, "okMessage"));
                    case UNKNOWN:
                    default:
                        return new FlightValidationStatus(
                            AlertType.LOADING,
                            languageHelper.getString(RemoteControlValidator.class, "loadingMessage"));
                    }
                },
                rcStatus));
    }

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.REMOTE_CONTROL;
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
