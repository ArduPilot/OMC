/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;

public interface IHealth {
    enum CalibrationStatus {
        UNKNOWN,
        CALIBRATION_NEEDED,
        OK
    }

    ReadOnlyAsyncObjectProperty<CalibrationStatus> calibrationStatusProperty();

    default CalibrationStatus getCalibrationStatus() {
        return calibrationStatusProperty().get();
    }
}
