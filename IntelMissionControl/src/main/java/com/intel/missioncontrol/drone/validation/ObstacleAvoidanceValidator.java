/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.IObstacleAvoidance;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;
import com.intel.missioncontrol.ui.validation.IResolveAction;
import com.intel.missioncontrol.ui.validation.SimpleResolveAction;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.CancellationSource;

/** Checks the Obstacle avoidance setting is matching with the connected drone */
public class ObstacleAvoidanceValidator implements IFlightValidator {
    private final SimpleResolveAction turnOnOaAction;
    private final SimpleResolveAction modifyFlightPlanAction;
    private final AsyncObjectProperty<FlightValidationStatus> validationStatus = new SimpleAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<FlightPlan> flightPlan = new SimpleAsyncObjectProperty<>(this);
    private final BooleanProperty oaCommandSend = new SimpleBooleanProperty(false);
    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);
    private AsyncObjectProperty<IResolveAction> firstResolveAction = new SimpleAsyncObjectProperty<>(this);
    private AsyncObjectProperty<IResolveAction> secondResolveAction = new SimpleAsyncObjectProperty<>(this);

    @Inject
    ObstacleAvoidanceValidator(
            IFlightValidationService flightValidationService,
            ILanguageHelper languageHelper,
            @Assisted CancellationSource cancellationSource) {
        flightPlan.bind(flightValidationService.flightPlanProperty());
        drone.bind(flightValidationService.droneProperty());

        ReadOnlyAsyncObjectProperty<IHardwareConfiguration> droneHwConfig =
            PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::hardwareConfigurationProperty);

        // TODO: bind fpPlatformDesc directly to FlightPlan; add HardwareConfig property to FlightPlan

        validationStatus.bind(
            Bindings.createObjectBinding(
                () -> {
                    IHardwareConfiguration hwConfig = droneHwConfig.get();
                    IPlatformDescription droneDesc = hwConfig == null ? null : hwConfig.getPlatformDescription();

                    if (droneDesc == null || flightPlan.get() == null) {
                        return new FlightValidationStatus(
                            AlertType.LOADING,
                            languageHelper.getString(ObstacleAvoidanceValidator.class, "loadingMessage"));
                    }

                    IObstacleAvoidance.Mode mode = drone.get().obstacleAvoidanceProperty().get().getMode();
                    if (flightPlan.get().obstacleAvoidanceEnabledProperty().getValue()) {
                        if (!mode.equals(IObstacleAvoidance.Mode.ENABLED)) {
                            return new FlightValidationStatus(
                                AlertType.ERROR,
                                languageHelper.getString(ObstacleAvoidanceValidator.class, "oaMismatch"));
                        }
                    }

                    return new FlightValidationStatus(
                        AlertType.COMPLETED, languageHelper.getString(ObstacleAvoidanceValidator.class, "okMessage"));
                },
                droneHwConfig,
                flightPlan,
                flightPlan.get().obstacleAvoidanceEnabledProperty(),
                oaCommandSend));

        turnOnOaAction =
            new SimpleResolveAction(
                languageHelper.getString(ObstacleAvoidanceValidator.class, "switchOaOn"),
                () -> {
                    if (drone.get() != null) {
                        drone.get().obstacleAvoidanceProperty().get().enableAsync(true);
                        oaCommandSend.setValue(true);
                    }
                });

        modifyFlightPlanAction =
            new SimpleResolveAction(
                languageHelper.getString(ObstacleAvoidanceValidator.class, "switchOffInMission"),
                () -> {
                    if (flightPlan.get() != null) {
                        flightPlan.get().obstacleAvoidanceEnabledProperty().setValue(false);
                    }
                });

        firstResolveAction.bind(
            validationStatus,
            value -> {
                if (value.getAlertType().equals(AlertType.WARNING) || value.getAlertType().equals(AlertType.ERROR)) {
                    return turnOnOaAction;
                }

                return null;
            });

        secondResolveAction.bind(
            validationStatus,
            value -> {
                if (value.getAlertType().equals(AlertType.WARNING) || value.getAlertType().equals(AlertType.ERROR)) {
                    return modifyFlightPlanAction;
                }

                return null;
            });
    }

    public ReadOnlyAsyncObjectProperty<FlightValidationStatus> validationStatusProperty() {
        return validationStatus;
    }

    @Override
    public FlightValidatorType getFlightValidatorType() {
        return FlightValidatorType.OBSTACLE_AVOIDANCE;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<IResolveAction> getFirstResolveAction() {
        return firstResolveAction;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<IResolveAction> getSecondResolveAction() {
        return secondResolveAction;
    }

    public interface Factory {
        ObstacleAvoidanceValidator create(CancellationSource cancellationSource);
    }

}
