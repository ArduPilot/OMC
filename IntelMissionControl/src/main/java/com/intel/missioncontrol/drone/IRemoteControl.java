/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;

public interface IRemoteControl {
    ReadOnlyAsyncObjectProperty<RemoteControl.Status> statusProperty();

    default RemoteControl.Status getStatus() {
        return statusProperty().get();
    }

    ReadOnlyAsyncObjectProperty<? extends IBattery> batteryProperty();

    default IBattery getBattery() {
        return batteryProperty().get();
    }
}
