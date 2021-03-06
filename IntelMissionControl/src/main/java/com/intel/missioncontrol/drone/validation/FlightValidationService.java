/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.google.inject.Inject;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlightValidationService implements IFlightValidationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightValidationService.class);

    @SuppressWarnings("FieldCanBeLocal")
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    private final InvalidationListener updateCombinedStatusMethod = o -> updateCombinedStatus();

    private final AsyncListProperty<IFlightValidator> validators = new SimpleAsyncListProperty<>(this);
    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<FlightPlan> flightPlan = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<FlightValidationStatus> combinedStatus = new SimpleAsyncObjectProperty<>(this);

    private final AnnoyingTestValidator.Factory annoyingTestValidatorFactory;
    private final AutomaticModeValidator.Factory automaticModeValidatorFactory;
    private final BatteryValidator.Factory batteryValidatorFactory;
    private final DaylightValidator.Factory daylightValidatorFactory;
    private final FlightPlanWarningsValidator.Factory flightPlanWarningsValidatorFactory;
    private final GnssFixValidator.Factory gnssFixValidatorFactory;
    private final HardwareCompatibilityValidator.Factory hardwareCompatibilityValidatorFactory;
    private final RemoteControlValidator.Factory remoteControlValidatorFactory;
    private final RestrictedCountryValidator.Factory restrictedCountryValidatorFactory;
    private final SensorCalibrationValidator.Factory sensorCalibrationValidatorFactory;
    private final StorageValidator.Factory storageValidatorFactory;
    private final CameraValidator.Factory cameraValidatorFactory;
    private final VideoStreamValidator.Factory videoStreamValidatorFactory;
    private final TakeoffPositionValidator.Factory takeoffPositionValidatorFactory;
    private final ObstacleAvoidanceValidator.Factory obstacleAvoidanceValidatorFactory;
    private final FlightValidationStatus errorStatus;
    private final FlightValidationStatus warningStatus;
    private final FlightValidationStatus loadingStatus;
    private final FlightValidationStatus okStatus;
    private CancellationSource cts = new CancellationSource();

    @Inject
    public FlightValidationService(
            ILanguageHelper languageHelper,
            AnnoyingTestValidator.Factory annoyingTestValidatorFactory,
            AutomaticModeValidator.Factory automaticModeValidatorFactory,
            BatteryValidator.Factory batteryValidatorFactory,
            DaylightValidator.Factory daylightValidatorFactory,
            FlightPlanWarningsValidator.Factory flightPlanWarningsValidatorFactory,
            GnssFixValidator.Factory gnssFixValidatorFactory,
            HardwareCompatibilityValidator.Factory hardwareCompatibilityValidatorFactory,
            RemoteControlValidator.Factory remoteControlValidatorFactory,
            RestrictedCountryValidator.Factory restrictedCountryValidatorFactory,
            SensorCalibrationValidator.Factory sensorCalibrationValidatorFactory,
            StorageValidator.Factory storageValidatorFactory,
            CameraValidator.Factory cameraValidatorFactory,
            VideoStreamValidator.Factory videoStreamValidatorFactory,
            TakeoffPositionValidator.Factory takeoffPositionValidatorFactory,
            ObstacleAvoidanceValidator.Factory obstacleAvoidanceValidatorFactory) {
        this.annoyingTestValidatorFactory = annoyingTestValidatorFactory;
        this.automaticModeValidatorFactory = automaticModeValidatorFactory;
        this.batteryValidatorFactory = batteryValidatorFactory;
        this.daylightValidatorFactory = daylightValidatorFactory;
        this.flightPlanWarningsValidatorFactory = flightPlanWarningsValidatorFactory;
        this.gnssFixValidatorFactory = gnssFixValidatorFactory;
        this.hardwareCompatibilityValidatorFactory = hardwareCompatibilityValidatorFactory;
        this.remoteControlValidatorFactory = remoteControlValidatorFactory;
        this.restrictedCountryValidatorFactory = restrictedCountryValidatorFactory;
        this.sensorCalibrationValidatorFactory = sensorCalibrationValidatorFactory;
        this.storageValidatorFactory = storageValidatorFactory;
        this.cameraValidatorFactory = cameraValidatorFactory;
        this.videoStreamValidatorFactory = videoStreamValidatorFactory;
        this.takeoffPositionValidatorFactory = takeoffPositionValidatorFactory;
        this.obstacleAvoidanceValidatorFactory = obstacleAvoidanceValidatorFactory;

        errorStatus =
            new FlightValidationStatus(
                AlertType.ERROR, languageHelper.getString(FlightValidationService.class, "autoChecksHaveErrors"));

        warningStatus =
            new FlightValidationStatus(
                AlertType.WARNING, languageHelper.getString(FlightValidationService.class, "autoChecksHaveWarnings"));

        loadingStatus =
            new FlightValidationStatus(
                AlertType.LOADING, languageHelper.getString(FlightValidationService.class, "autoChecksInProgress"));

        okStatus =
            new FlightValidationStatus(
                AlertType.COMPLETED, languageHelper.getString(FlightValidationService.class, "autoChecksAllPassed"));

        ReadOnlyAsyncObjectProperty<IHardwareConfiguration> hardwareConfiguration =
            propertyPathStore.from(drone).selectReadOnlyAsyncObject(IDrone::hardwareConfigurationProperty);

        validators.set(FXAsyncCollections.observableArrayList());

        IHardwareConfiguration hwConfig = hardwareConfiguration.get();
        applyPlatformDescription(hwConfig == null ? null : hwConfig.getPlatformDescription());
        hardwareConfiguration.addListener(
            (o, oldValue, newValue) ->
                applyPlatformDescription(newValue == null ? null : newValue.getPlatformDescription()),
            Dispatcher.background()::run);

        updateCombinedStatus();
    }

    private void applyPlatformDescription(IPlatformDescription platformDescription) {
        // clean up old validators:
        cts.cancel();
        cts = new CancellationSource();

        try (LockedList<IFlightValidator> validators = this.validators.lock()) {
            for (var v : validators) {
                v.validationStatusProperty().removeListener(updateCombinedStatusMethod);
            }
        }

        // new validators:
        List<IFlightValidator> newValidators = new ArrayList<>();
        if (platformDescription != null) {
            LOGGER.info("Apply hardware configuration: " + platformDescription.getId());

            List<FlightValidatorType> flightValidatorTypes = platformDescription.getFlightValidatorTypes();
            for (FlightValidatorType t : flightValidatorTypes) {
                switch (t) {
                case HARDWARE_COMPATIBILITY:
                    newValidators.add(hardwareCompatibilityValidatorFactory.create(cts));
                    break;
                case FLIGHTPLAN_WARNINGS:
                    newValidators.add(flightPlanWarningsValidatorFactory.create());
                    break;
                case BATTERY:
                    newValidators.add(batteryValidatorFactory.create());
                    break;
                case TAKEOFF_POSITION:
                    newValidators.add(takeoffPositionValidatorFactory.create(cts));
                    break;
                case DAYLIGHT:
                    newValidators.add(daylightValidatorFactory.create(cts));
                    break;
                case RESTRICTED_COUNTRY:
                    newValidators.add(restrictedCountryValidatorFactory.create(cts));
                    break;
                case SENSOR_CALIBRATION:
                    newValidators.add(sensorCalibrationValidatorFactory.create());
                    break;
                case REMOTE_CONTROL:
                    newValidators.add(remoteControlValidatorFactory.create());
                    break;
                case STORAGE:
                    newValidators.add(storageValidatorFactory.create());
                    break;
                case AUTOMATIC_MODE:
                    newValidators.add(automaticModeValidatorFactory.create());
                    break;
                case GNSS_FIX:
                    newValidators.add(gnssFixValidatorFactory.create());
                    break;
                case CAMERA:
                    newValidators.add(cameraValidatorFactory.create(cts));
                    break;
                case ANNOYING_TEST:
                    newValidators.add(annoyingTestValidatorFactory.create(cts));
                case VIDEO_STREAM:
                    newValidators.add(videoStreamValidatorFactory.create());
                    break;
                case OBSTACLE_AVOIDANCE:
                    newValidators.add(obstacleAvoidanceValidatorFactory.create(cts));
                    break;
                case COMBINED:
                default:
                    LOGGER.warn(
                        "Invalid flight validator type " + t + " for platform id: " + platformDescription.getId());
                }
            }
        } else {
            LOGGER.info("Apply hardware configuration: [none]");
        }

        for (IFlightValidator v : newValidators) {
            v.validationStatusProperty().addListener(updateCombinedStatusMethod);
        }

        this.validators.setAll(newValidators);
    }

    private void updateCombinedStatus() {
        // Combined status: first of: Any error / any warning / any running / ok.
        boolean errors = false, warnings = false, loadings = false;
        try (LockedList<IFlightValidator> validators = this.validators.lock()) {
            for (IFlightValidator validator : validators) {
                FlightValidationStatus v = validator.getValidationStatus();
                if (v == null || v.getAlertType() == AlertType.LOADING) {
                    loadings = true;
                } else if (v.getAlertType() == AlertType.WARNING) {
                    warnings = true;
                } else if (v.getAlertType() == AlertType.ERROR) {
                    errors = true;
                    break;
                }
            }
        }

        if(errors) {
            combinedStatus.set(errorStatus);
        } else if (warnings) {
            combinedStatus.set(warningStatus);
        } else if (loadings) {
            combinedStatus.set(loadingStatus);
        } else {
            combinedStatus.set(okStatus);
        }
    }

    public AsyncObjectProperty<IDrone> droneProperty() {
        return drone;
    }

    public AsyncObjectProperty<FlightPlan> flightPlanProperty() {
        return flightPlan;
    }

    @Override
    public ReadOnlyAsyncListProperty<IFlightValidator> validatorsProperty() {
        return validators;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> combinedStatusProperty() {
        return combinedStatus;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return combinedStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.COMBINED;
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
