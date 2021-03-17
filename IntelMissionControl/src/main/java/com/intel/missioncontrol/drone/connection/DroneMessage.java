/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import java.util.Date;

public class DroneMessage {
    public enum Severity {
        INFO,
        WARNING,
        ERROR
    }

    private final String message;
    private final Date timeStamp;
    private final Severity severity;

    public DroneMessage(String message, Date timeStamp, Severity severity) {
        this.message = message;
        this.timeStamp = timeStamp;
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public Severity getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return severity + ": " + message;
    }
}
