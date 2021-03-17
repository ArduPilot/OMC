/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;

public class RemoteControl implements IRemoteControl {
    private final AsyncObjectProperty<RemoteControl.Status> status =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<Status>().initialValue(Status.UNKNOWN).create());
    private final AsyncObjectProperty<Battery> battery = new SimpleAsyncObjectProperty<>(this);

    public enum Status {
        UNKNOWN,
        NO_REMOTE_CONTROL,
        REMOTE_CONTROL_ERROR,
        OK
    }

    RemoteControl() {}

    @Override
    public AsyncObjectProperty<Status> statusProperty() {
        return status;
    }

    @Override
    public AsyncObjectProperty<? extends IBattery> batteryProperty() {
        return battery;
    }
}
