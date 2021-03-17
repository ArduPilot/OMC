/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.drone.connection.GrayhawkDroneConnection;
import com.intel.missioncontrol.drone.connection.mavlink.Parameter;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.concurrent.Future;

public class GrayhawkObstacleAvoidance implements IObstacleAvoidance {
    private final AsyncListProperty<DistanceSensor> distanceSensors =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<DistanceSensor>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final AsyncObjectProperty<Mode> mode =
        new SimpleAsyncObjectProperty<>(this, new PropertyMetadata.Builder<Mode>().initialValue(Mode.UNKNOWN).create());

    private final DistanceSensor distanceSensor;

    private final GrayhawkDroneConnection droneConnection;

    GrayhawkObstacleAvoidance(GrayhawkDroneConnection droneConnection) {
        this.droneConnection = droneConnection;
        distanceSensor = new DistanceSensor();
        distanceSensors.add(distanceSensor);
    }

    DistanceSensor getDistanceSensor() {
        return distanceSensor;
    }

    @Override
    public ReadOnlyAsyncListProperty<? extends IDistanceSensor> distanceSensorsProperty() {
        return distanceSensors;
    }

    @Override
    public IDistanceSensor getAggregatedDistanceSensor() {
        return distanceSensor;
    }

    @Override
    public AsyncObjectProperty<Mode> modeProperty() {
        return mode;
    }

    @Override
    public Future<Void> enableAsync(boolean enable) {
        return droneConnection
            .getParameterProtocolSender()
            .setParamAsync(Parameter.createInt32("INTEL_OA_ACTIVE", enable ? 1 : 0));
    }
}
