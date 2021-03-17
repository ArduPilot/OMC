/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

public class DroneConnectionException extends RuntimeException {

    private final boolean isRecoverable;

    private final Class type;

    public Class getType() {
        return type;
    }

    public boolean isRecoverable() {
        return isRecoverable;
    }

    public DroneConnectionException(Class type, boolean isRecoverable) {
        this.isRecoverable = isRecoverable;
        this.type = type;
    }

    public DroneConnectionException(Class type, boolean isRecoverable, String message) {
        super(message);
        this.isRecoverable = isRecoverable;
        this.type = type;
    }

    public DroneConnectionException(Class type, boolean isRecoverable, String message, Throwable cause) {
        super(message, cause);
        this.isRecoverable = isRecoverable;
        this.type = type;
    }

    public DroneConnectionException(Class type, boolean isRecoverable, Throwable cause) {
        super(cause);
        this.isRecoverable = isRecoverable;
        this.type = type;
    }
}
