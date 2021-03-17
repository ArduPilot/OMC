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
import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.drone.GnssState;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.IGnssInfo;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import javafx.beans.binding.Bindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Check if drone has GNSS fix */
public class GnssFixValidator implements IFlightValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(GnssFixValidator.class);

    public interface Factory {
        GnssFixValidator create();
    }

    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);

    @Inject
    GnssFixValidator(IFlightValidationService flightValidationService, ILanguageHelper languageHelper) {
        ReadOnlyAsyncObjectProperty<GnssState> gnssState =
            PropertyPath.from(flightValidationService.droneProperty())
                .select(IDrone::gnssInfoProperty)
                .selectReadOnlyAsyncObject(IGnssInfo::gnssStateProperty);

        ReadOnlyAsyncBooleanProperty telemetryOld =
            PropertyPath.from(flightValidationService.droneProperty())
                .select(IDrone::gnssInfoProperty)
                .selectReadOnlyAsyncBoolean(IGnssInfo::telemetryOldProperty);

        // TODO warning if low gps quality

        validationStatus.bind(
            Bindings.createObjectBinding(
                () -> {
                    GnssState s = gnssState.get();
                    if (s == null || telemetryOld.get()) {
                        s = GnssState.UNKNOWN;
                    }

                    switch (s) {
                    case NO_FIX:
                        return new FlightValidationStatus(
                            AlertType.ERROR, languageHelper.getString(GnssFixValidator.class, "noFixMessage"));
                    case GPS:
                    case RTK_FLOAT:
                    case RTK_FIXED:
                        return new FlightValidationStatus(
                            AlertType.COMPLETED, languageHelper.getString(GnssFixValidator.class, "okMessage"));
                    case UNKNOWN:
                    default:
                        return new FlightValidationStatus(
                            AlertType.LOADING, languageHelper.getString(GnssFixValidator.class, "loadingMessage"));
                    }
                },
                gnssState,
                telemetryOld));
    }

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.GNSS_FIX;
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
