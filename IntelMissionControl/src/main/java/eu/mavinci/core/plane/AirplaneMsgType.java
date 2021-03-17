/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

public enum AirplaneMsgType {
    DEBUG(0),
    INFORMATION(1),
    WARNING_OR_ERROR(2),
    POPUP_MUST_CONFIRM(3),
    NOTICE(4),
    WARNING(5),
    ERROR(6),
    CRITICAL(7),
    ALERT(8),
    EMERGENCY(9);

    private int severityLevel;

    AirplaneMsgType(int severityLevel) {
        this.severityLevel = severityLevel;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }
}
