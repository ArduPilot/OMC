/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.ReadOnlyAsyncDoubleProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;

public interface IStorage {
    ReadOnlyAsyncObjectProperty<Storage.Status> statusProperty();

    default Storage.Status getStatus() {
        return statusProperty().get();
    }

    ReadOnlyAsyncDoubleProperty availableSpaceMiBProperty();

    default double getAvailableSpaceMiB() {
        return availableSpaceMiBProperty().get();
    }
}
