/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.INotificationObject;
import com.intel.missioncontrol.drone.ICamera;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.hardware.IGenericCameraConfiguration;
import com.intel.missioncontrol.hardware.IGenericCameraDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;

/** Check if selected camera in flight plan hardware setting is connected */
public class CameraValidator implements IFlightValidator {
    public interface Factory {
        CameraValidator create(CancellationSource cancellationSource);
    }

    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);

    private final AsyncObjectProperty<IGenericCameraDescription> fpCameraDesc = new SimpleAsyncObjectProperty<>(this);

    // the connected camera. null if not present or of wrong hardware type
    private final AsyncObjectProperty<ICamera> compatibleCamera = new SimpleAsyncObjectProperty<>(this);

    private final ReadOnlyAsyncListProperty<? extends ICamera> droneCameras;

    private final IFlightValidationService flightValidationService;

    @Inject
    CameraValidator(
            IFlightValidationService flightValidationService,
            ILanguageHelper languageHelper,
            @Assisted CancellationSource cancellationSource) {
        this.flightValidationService = flightValidationService;

        // all connected cameras
        droneCameras =
            PropertyPath.from(flightValidationService.droneProperty()).selectReadOnlyAsyncList(IDrone::camerasProperty);

        ReadOnlyAsyncObjectProperty<ICamera.Status> cameraStatus =
            PropertyPath.from(compatibleCamera).selectReadOnlyAsyncObject(ICamera::statusProperty);

        addFlightPlanHardwareListener();

        final ChangeListener<FlightPlan> fpChangeListener =
            (obs, oldValue, newValue) -> {
                if (cancellationSource.isCancellationRequested()) {
                    return;
                }

                CameraValidator.this.removeFlightPlanHardwareListener(oldValue);
                CameraValidator.this.addFlightPlanHardwareListener();
            };

        flightValidationService.flightPlanProperty().addListener(fpChangeListener, Dispatcher.platform());

        cancellationSource.addListener(
            mayInterruptIfRunning ->
                Dispatcher.platform()
                    .run(
                        () -> {
                            removeFlightPlanHardwareListener(flightValidationService.flightPlanProperty().get());
                            flightValidationService.flightPlanProperty().removeListener(fpChangeListener);
                        }));

        droneCameras.addListener((InvalidationListener)(o) -> Dispatcher.background().run(this::updateCamera));
        fpCameraDesc.addListener((o) -> Dispatcher.background().run(this::updateCamera));

        validationStatus.bind(
            Bindings.createObjectBinding(
                () -> {
                    IGenericCameraDescription fpCamDesc = fpCameraDesc.get();

                    if (fpCamDesc == null) {
                        return new FlightValidationStatus(
                            AlertType.LOADING, languageHelper.getString(CameraValidator.class, "loadingMessage"));
                    }

                    String fpCameraString = fpCamDesc.getName();

                    if (droneCameras.isEmpty()) {
                        return new FlightValidationStatus(
                            AlertType.WARNING,
                            languageHelper.getString(CameraValidator.class, "noCameraConnected", fpCameraString));
                    }

                    ICamera cam = compatibleCamera.get();
                    if (cam == null) {
                        return new FlightValidationStatus(
                            AlertType.WARNING,
                            languageHelper.getString(CameraValidator.class, "wrongCameraSelected", fpCameraString));
                    }

                    ICamera.Status camStatus = cameraStatus.get();
                    if (camStatus == null) {
                        camStatus = ICamera.Status.UNKNOWN;
                    }

                    switch (camStatus) {
                    case PARAMETER_ERROR:
                        return new FlightValidationStatus(
                            AlertType.WARNING, languageHelper.getString(CameraValidator.class, "parameterError"));
                    case OK:
                        return new FlightValidationStatus(
                            AlertType.COMPLETED, languageHelper.getString(CameraValidator.class, "okMessage"));
                    case UNKNOWN:
                    default:
                        return new FlightValidationStatus(
                            AlertType.LOADING, languageHelper.getString(CameraValidator.class, "loadingMessage"));
                    }
                },
                compatibleCamera,
                droneCameras.emptyProperty(),
                cameraStatus,
                fpCameraDesc));
    }

    private synchronized void updateCamera() {
        IGenericCameraDescription fpCamDesc = fpCameraDesc.get();
        if (fpCamDesc == null) {
            compatibleCamera.set(null);
            return;
        }

        try (var cams = droneCameras.lock()) {
            for (var cam : cams) {
                IGenericCameraDescription desc = cam.getCameraDescription();

                if (desc.getId().equals(fpCamDesc.getId())) {
                    compatibleCamera.getExecutor().execute(() -> compatibleCamera.set(cam));
                    return;
                }
            }
        }

        compatibleCamera.set(null);
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
            fpCameraDesc.set(null);
            return;
        }

        fpCameraDesc.set(
            fp.getLegacyFlightplan()
                .getHardwareConfiguration()
                .getPrimaryPayload(IGenericCameraConfiguration.class)
                .getDescription());
    };

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.CAMERA;
    }

}
