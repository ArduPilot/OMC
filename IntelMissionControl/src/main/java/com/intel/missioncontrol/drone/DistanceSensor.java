/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncDoubleProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;

public class DistanceSensor implements IDistanceSensor {

    /** * AlertLevels sorted by criticality */
    public enum AlertLevel {
        UNKNOWN,
        LEVEL3,
        LEVEL2,
        LEVEL1_PREWARN,
        LEVEL0_CRITICAL
    }

    private final AsyncDoubleProperty closestDistanceMeters =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(Double.NaN).create());
    private final AsyncObjectProperty<AlertLevel> alertLevel =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<AlertLevel>().initialValue(AlertLevel.UNKNOWN).create());
    private final AsyncBooleanProperty telemetryOld =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    DistanceSensor() {}

    @Override
    public AsyncDoubleProperty closestDistanceMetersProperty() {
        return closestDistanceMeters;
    }

    @Override
    public AsyncObjectProperty<AlertLevel> alertLevelProperty() {
        return alertLevel;
    }

    @Override
    public AsyncBooleanProperty telemetryOldProperty() {
        return telemetryOld;
    }
}
