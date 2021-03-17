/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.AsyncDoubleProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncDoubleProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;

public class Storage implements IStorage {
    private final AsyncObjectProperty<Storage.Status> status =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<Status>().initialValue(Status.UNKNOWN).create());
    private final AsyncDoubleProperty availableSpaceMiB =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(0.0).create());
    private final AsyncDoubleProperty totalSpaceMiB =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(0.0).create());

    public enum Status {
        UNKNOWN,
        NO_STORAGE_DEVICE,
        STORAGE_DEVICE_ERROR,
        OK;
    }

    Storage() {}

    @Override
    public AsyncObjectProperty<Status> statusProperty() {
        return status;
    }

    @Override
    public AsyncDoubleProperty availableSpaceMiBProperty() {
        return availableSpaceMiB;
    }

    @Override
    public AsyncDoubleProperty totalSpaceMiBProperty() {
        return totalSpaceMiB;
    }
}
