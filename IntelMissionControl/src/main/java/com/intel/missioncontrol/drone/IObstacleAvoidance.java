/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.concurrent.Future;

public interface IObstacleAvoidance {
    enum Mode {
        UNKNOWN,
        NOT_AVAILABLE,
        DISABLED,
        ENABLED
        // TODO: could add FIXED_DISTANCE mode here
    }

    /** list of distance sensors */
    ReadOnlyAsyncListProperty<? extends IDistanceSensor> distanceSensorsProperty();

    default AsyncObservableList<? extends IDistanceSensor> getDistanceSensors() {
        return distanceSensorsProperty().get();
    }

    /** Aggregated shortest distance and alert level for all distance sensors */
    IDistanceSensor getAggregatedDistanceSensor();

    /** Indicates current obstacle avoidance mode */
    ReadOnlyAsyncObjectProperty<Mode> modeProperty();

    default Mode getMode() {
        return modeProperty().get();
    }

    /** Enable or disable obstacle avoidance */
    Future<Void> enableAsync(boolean enable);
}
