/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;

public class Health implements IHealth {

    private final AsyncObjectProperty<CalibrationStatus> calibrationStatus =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<CalibrationStatus>().initialValue(CalibrationStatus.UNKNOWN).create());

    public Health() {}

    @Override
    public AsyncObjectProperty<CalibrationStatus> calibrationStatusProperty() {
        return calibrationStatus;
    }
}
