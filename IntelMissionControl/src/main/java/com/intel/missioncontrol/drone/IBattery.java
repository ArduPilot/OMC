/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.ReadOnlyAsyncBooleanProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncDoubleProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;

public interface IBattery {
    ReadOnlyAsyncDoubleProperty remainingChargePercentageProperty();

    default double getRemainingChargePercentage() {
        return remainingChargePercentageProperty().get();
    }

    ReadOnlyAsyncDoubleProperty voltageProperty();

    default double getVoltage() {
        return voltageProperty().get();
    }

    ReadOnlyAsyncObjectProperty<BatteryAlertLevel> alertLevelProperty();

    default BatteryAlertLevel getAlertLevel() {
        return alertLevelProperty().get();
    }

    ReadOnlyAsyncBooleanProperty telemetryOldProperty();

}
