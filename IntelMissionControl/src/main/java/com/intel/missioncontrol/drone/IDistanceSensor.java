/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncDoubleProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;

public interface IDistanceSensor {
    ReadOnlyAsyncDoubleProperty closestDistanceMetersProperty();

    default double getClosestDistanceMeters() {
        return closestDistanceMetersProperty().get();
    }

    ReadOnlyAsyncObjectProperty<DistanceSensor.AlertLevel> alertLevelProperty();

    default DistanceSensor.AlertLevel getAlertLevel() {
        return alertLevelProperty().get();
    }

    ReadOnlyAsyncBooleanProperty telemetryOldProperty();

    default boolean getTelemetryOld() {
        return telemetryOldProperty().get();
    }
}
