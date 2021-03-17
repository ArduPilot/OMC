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

public class Battery implements IBattery {
    private final AsyncDoubleProperty remainingChargePercentage =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(Double.NaN).create());
    private final AsyncDoubleProperty voltage =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(Double.NaN).create());
    private final AsyncObjectProperty<BatteryAlertLevel> alertLevel = new SimpleAsyncObjectProperty<>(this);
    private final AsyncBooleanProperty telemetryOld =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    public Battery() {}

    @Override
    public AsyncDoubleProperty remainingChargePercentageProperty() {
        return remainingChargePercentage;
    }

    @Override
    public AsyncDoubleProperty voltageProperty() {
        return voltage;
    }

    @Override
    public AsyncObjectProperty<BatteryAlertLevel> alertLevelProperty() {
        return alertLevel;
    }

    @Override
    public AsyncBooleanProperty telemetryOldProperty() {
        return telemetryOld;
    }
}
