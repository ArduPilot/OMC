/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.drone.connection.GrayhawkDroneConnection;
import com.intel.missioncontrol.drone.connection.mavlink.IMavlinkParameter;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import io.dronefleet.mavlink.obstacle_avoidance.OaState;
import io.dronefleet.mavlink.obstacle_avoidance.ObstacleAvoidanceStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.slf4j.LoggerFactory;

public class GrayhawkDrone extends PX4Drone {
    private static final org.slf4j.Logger LOGGER =
        LoggerFactory.getLogger(com.intel.missioncontrol.drone.GrayhawkDrone.class);

    private final GrayhawkDroneConnection droneConnection;

    private final AsyncObjectProperty<GrayhawkObstacleAvoidance> obstacleAvoidance =
        new SimpleAsyncObjectProperty<>(this);

    private GrayhawkDrone(GrayhawkDroneConnection droneConnection, IHardwareConfiguration hardwareConfiguration) {
        super(droneConnection, hardwareConfiguration);
        this.droneConnection = droneConnection;
        obstacleAvoidance.set(new GrayhawkObstacleAvoidance(droneConnection));
    }

    public static GrayhawkDrone create(
            GrayhawkDroneConnection droneConnection, IHardwareConfiguration hardwareConfiguration) {
        GrayhawkDrone drone = new GrayhawkDrone(droneConnection, hardwareConfiguration);
        drone.initializeBindings();
        return drone;
    }

    @Override
    void initializeBindings() {
        super.initializeBindings();

        // obstacle avoidance status
        droneConnection
            .getGrayhawkMessagesReceiver()
            .registerObstacleAvoidanceStatusHandlerAsync(
                receivedPayload -> {
                    ObstacleAvoidanceStatus status = receivedPayload.getPayload();

                    LOGGER.debug("Received ObstacleAvoidanceStatus: " + status);

                    DistanceSensor distanceSensor = obstacleAvoidance.get().getDistanceSensor();

                    boolean oa_capable_system = status.flags().flagsEnabled(OaState.OA_STATE_CAPABLE_SYSTEM);
                    if (!oa_capable_system) {
                        distanceSensor.alertLevelProperty().set(DistanceSensor.AlertLevel.UNKNOWN);
                        distanceSensor.closestDistanceMetersProperty().set(Double.NaN);
                        distanceSensor.telemetryOldProperty().set(false);
                        obstacleAvoidance.get().modeProperty().set(IObstacleAvoidance.Mode.NOT_AVAILABLE);
                        return;
                    }

                    boolean oa_active = status.flags().flagsEnabled(OaState.OA_STATE_ACTIVE);
                    if (!oa_active) {
                        distanceSensor.alertLevelProperty().set(DistanceSensor.AlertLevel.LEVEL0_CRITICAL);
                        distanceSensor.closestDistanceMetersProperty().set(Double.NaN);
                        distanceSensor.telemetryOldProperty().set(false);
                        obstacleAvoidance.get().modeProperty().set(IObstacleAvoidance.Mode.DISABLED);
                        return;
                    }

                    // for now, just get minimum distance:
                    List<Integer> distances = status.distances();
                    if (distances == null) {
                        distances = new ArrayList<>();
                    }

                    double distance_m =
                        distances
                            .stream()
                            .filter(d -> d > 0 && d < 0xFFFF)
                            .min(Comparator.comparing(Integer::valueOf))
                            .map(d -> (double)d * 0.01)
                            .orElse(Double.NaN);

                    if (Double.isNaN(distance_m) && distances.stream().anyMatch(d -> d == 0)) {
                        distance_m = Double.POSITIVE_INFINITY;
                    }

                    DistanceSensor.AlertLevel alertLevel = DistanceSensor.AlertLevel.UNKNOWN;

                    boolean anyError = status.errors().value() != 0; // TODO: exctract errors
                    boolean anyWarning = status.warnings().value() != 0; // TODO: exctract warnings
                    if (anyError) {
                        alertLevel = DistanceSensor.AlertLevel.LEVEL0_CRITICAL;
                    } else {
                        double distanceOaMin_m = (double)status.distanceOaMin() * 0.01;

                        // TODO define alert levels
                        if (distance_m >= distanceOaMin_m) {
                            alertLevel = DistanceSensor.AlertLevel.LEVEL3;
                        }
                    }

                    obstacleAvoidance.get().modeProperty().set(IObstacleAvoidance.Mode.ENABLED);
                    distanceSensor.alertLevelProperty().set(alertLevel);
                    distanceSensor.closestDistanceMetersProperty().set(distance_m);
                    distanceSensor.telemetryOldProperty().set(false);
                },
                // timeout
                () -> {
                    DistanceSensor distanceSensor = obstacleAvoidance.get().getDistanceSensor();
                    distanceSensor.telemetryOldProperty().set(true);
                })
            .whenFailed(
                e ->
                    raiseDroneConnectionExceptionEvent(
                        new DroneConnectionException(ObstacleAvoidanceStatus.class, false, e)));
    }

    @Override
    public ReadOnlyAsyncObjectProperty<? extends IObstacleAvoidance> obstacleAvoidanceProperty() {
        return obstacleAvoidance;
    }

    @Override
    protected List<IMavlinkParameter> createAutopilotParameterList(MavlinkFlightPlan mavlinkFlightPlan) {
        return super.createAutopilotParameterList(mavlinkFlightPlan);
    }

    @Override
    protected void applyFlightPlanInitialSettings(MavlinkFlightPlan mavlinkFlightPlan) {
        obstacleAvoidance.get().enableAsync(mavlinkFlightPlan.getFlightPlan().obstacleAvoidanceEnabledProperty().get());
    }
}
