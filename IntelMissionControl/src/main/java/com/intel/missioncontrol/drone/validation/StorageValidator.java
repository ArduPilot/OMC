/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.google.inject.Inject;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.IStorage;
import com.intel.missioncontrol.drone.Storage;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.WayPoint;
import com.intel.missioncontrol.ui.controls.AdaptiveQuantityFormat;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncDoubleProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Check if drone is in automatic mode */
public class StorageValidator implements IFlightValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageValidator.class);

    public interface Factory {
        StorageValidator create();
    }

    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);

    @Inject
    StorageValidator(
            IFlightValidationService flightValidationService,
            ILanguageHelper languageHelper,
            IQuantityStyleProvider quantityStyleProvider) {
        AdaptiveQuantityFormat quantityFormat = new AdaptiveQuantityFormat(quantityStyleProvider);
        quantityFormat.setSignificantDigits(3);

        ReadOnlyAsyncObjectProperty<Storage.Status> storageStatus =
            PropertyPath.from(flightValidationService.droneProperty())
                .select(IDrone::storageProperty)
                .selectReadOnlyAsyncObject(IStorage::statusProperty);

        ReadOnlyAsyncDoubleProperty availableSpaceMiB =
            PropertyPath.from(flightValidationService.droneProperty())
                .select(IDrone::storageProperty)
                .selectReadOnlyAsyncDouble(IStorage::availableSpaceMiBProperty);

        ReadOnlyListProperty<WayPoint> wayPoints =
            PropertyPath.from(flightValidationService.flightPlanProperty())
                .selectReadOnlyList(FlightPlan::waypointsProperty);

        // TODO: make sure this updates if camera selection is changed
        DoubleBinding requiredStorageSpacePerImageMiB =
            Bindings.createDoubleBinding(
                () -> {
                    FlightPlan fp = flightValidationService.flightPlanProperty().get();
                    if (fp == null) {
                        return Double.NaN;
                    }

                    IGenericCameraDescription desc =
                        fp.getLegacyFlightplan()
                            .getHardwareConfiguration()
                            .getPrimaryPayload(IGenericCameraConfiguration.class)
                            .getDescription();

                    return desc.getPictureSizeInMB();
                },
                flightValidationService.flightPlanProperty());

        DoubleBinding requiredSpaceMiB =
            Bindings.createDoubleBinding(
                () -> {
                    if (wayPoints.get() == null) {
                        return Double.NaN;
                    }

                    long imageCount =
                        wayPoints.stream().filter(wp -> wp.triggerImageHereCopterModeProperty().get()).count();
                    double requiredStorageSpPerImageMiB = requiredStorageSpacePerImageMiB.get();
                    return (double)imageCount * requiredStorageSpPerImageMiB;
                },
                wayPoints,
                requiredStorageSpacePerImageMiB);

        validationStatus.bind(
            Bindings.createObjectBinding(
                () -> {
                    double availableSpMiB = availableSpaceMiB.get();
                    double requiredSpMiB = requiredSpaceMiB.get();

                    Storage.Status s = storageStatus.get();
                    if (s == null) {
                        s = Storage.Status.UNKNOWN;
                    } else if (Double.isNaN(availableSpMiB) || Double.isNaN(requiredSpMiB)) {
                        s = Storage.Status.STORAGE_DEVICE_ERROR;
                    }

                    switch (s) {
                    case STORAGE_DEVICE_ERROR:
                        return new FlightValidationStatus(
                            AlertType.WARNING,
                            languageHelper.getString(StorageValidator.class, "storageDeviceErrorMessage"));
                    case NO_STORAGE_DEVICE:
                        return new FlightValidationStatus(
                            AlertType.WARNING,
                            languageHelper.getString(StorageValidator.class, "noStorageDeviceMessage"));
                    case OK:
                        String availableSpString = quantityFormat.format(Quantity.of(availableSpMiB, Unit.MEBIBYTE));
                        String requiredSpString = quantityFormat.format(Quantity.of(requiredSpMiB, Unit.MEBIBYTE));

                        if (availableSpMiB < requiredSpMiB) {
                            return new FlightValidationStatus(
                                AlertType.WARNING,
                                languageHelper.getString(
                                    StorageValidator.class,
                                    "insufficientStorageSpaceMessage",
                                    requiredSpString,
                                    availableSpString));
                        }

                        return new FlightValidationStatus(
                            AlertType.COMPLETED,
                            languageHelper.getString(
                                StorageValidator.class, "okMessage", requiredSpString, availableSpString));
                    case UNKNOWN:
                    default:
                        return new FlightValidationStatus(
                            AlertType.LOADING, languageHelper.getString(StorageValidator.class, "loadingMessage"));
                    }
                },
                storageStatus,
                availableSpaceMiB,
                requiredSpaceMiB));
    }

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.STORAGE;
    }
}
