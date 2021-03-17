/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.validation;

import com.intel.missioncontrol.ui.sidepane.flight.fly.checks.AlertType;

public class FlightValidationStatus {
    private final AlertType alertType;
    private final String messageString;

    FlightValidationStatus(AlertType alertType, String messageString) {
        this.alertType = alertType;
        this.messageString = messageString;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public String getMessageString() {
        return messageString;
    }

    @Override
    public String toString() {
        return "FlightValidationStatus{" + "alertType=" + alertType + ", messageString='" + messageString + '\'' + '}';
    }
}
