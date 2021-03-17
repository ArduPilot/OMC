/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;

/** Check if mission hardware type is compatible with connected drone */
public class HardwareCompatibilityValidator implements IFlightValidator {
    public interface Factory {
        HardwareCompatibilityValidator create(CancellationSource cancellationSource);
    }

    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);

    private final AsyncObjectProperty<IPlatformDescription> fpPlatformDesc = new SimpleAsyncObjectProperty<>(this);

    private final IFlightValidationService flightValidationService;

    @Inject
    HardwareCompatibilityValidator(
            IFlightValidationService flightValidationService,
            ILanguageHelper languageHelper,
            @Assisted CancellationSource cancellationSource) {
        this.flightValidationService = flightValidationService;

        ReadOnlyAsyncObjectProperty<IHardwareConfiguration> droneHwConfig =
            PropertyPath.from(flightValidationService.droneProperty())
                .selectReadOnlyAsyncObject(IDrone::hardwareConfigurationProperty);

        // TODO: bind fpPlatformDesc directly to FlightPlan; add HardwareConfig property to FlightPlan

        addFlightPlanHardwareListener();

        final ChangeListener<FlightPlan> fpChangeListener =
            (obs, oldValue, newValue) -> {
                if (cancellationSource.isCancellationRequested()) {
                    return;
                }

                HardwareCompatibilityValidator.this.removeFlightPlanHardwareListener(oldValue);
                HardwareCompatibilityValidator.this.addFlightPlanHardwareListener();
            };

        flightValidationService.flightPlanProperty().addListener(fpChangeListener, Dispatcher.platform()::run);

        cancellationSource.addListener(
            mayInterruptIfRunning ->
                Dispatcher.platform()
                    .run(
                        () -> {
                            removeFlightPlanHardwareListener(flightValidationService.flightPlanProperty().get());
                            flightValidationService.flightPlanProperty().removeListener(fpChangeListener);
                        }));

        validationStatus.bind(
            Bindings.createObjectBinding(
                () -> {
                    IHardwareConfiguration hwConfig = droneHwConfig.get();
                    IPlatformDescription droneDesc = hwConfig == null ? null : hwConfig.getPlatformDescription();
                    IPlatformDescription fpDesc = fpPlatformDesc.get();
                    if (droneDesc == null || fpDesc == null) {
                        return new FlightValidationStatus(
                            AlertType.LOADING,
                            languageHelper.getString(HardwareCompatibilityValidator.class, "loadingMessage"));
                    }

                    String droneHardwareString = droneDesc.getName();
                    String fpHardwareString = fpDesc.getName();

                    if (Objects.equals(droneDesc.getId(), fpDesc.getId())) {
                        return new FlightValidationStatus(
                            AlertType.COMPLETED,
                            languageHelper.getString(
                                HardwareCompatibilityValidator.class, "okMessage", fpHardwareString));
                    } else {
                        return new FlightValidationStatus(
                            AlertType.ERROR,
                            languageHelper.getString(
                                HardwareCompatibilityValidator.class,
                                "hardwareMismatch",
                                fpHardwareString,
                                droneHardwareString));
                    }
                },
                droneHwConfig,
                fpPlatformDesc));
    }

    private void addFlightPlanHardwareListener() {
        Dispatcher.platform()
            .run(
                () -> {
                    FlightPlan fp = flightValidationService.flightPlanProperty().get();
                    if (fp != null) {
                        fp.getLegacyFlightplan().getHardwareConfiguration().addListener(this::updateFpHardwareConfig);
                    }

                    updateFpHardwareConfig(null);
                });
    }

    private void removeFlightPlanHardwareListener(FlightPlan fp) {
        if (fp != null) {
            fp.getLegacyFlightplan().getHardwareConfiguration().removeListener(this::updateFpHardwareConfig);
        }
    }

    private void updateFpHardwareConfig(INotificationObject.ChangeEvent changeEvent) {
        FlightPlan fp = flightValidationService.flightPlanProperty().get();
        if (fp == null) {
            fpPlatformDesc.set(null);
            return;
        }

        fpPlatformDesc.set(fp.getLegacyFlightplan().getHardwareConfiguration().getPlatformDescription());
    };

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.HARDWARE_COMPATIBILITY;
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
