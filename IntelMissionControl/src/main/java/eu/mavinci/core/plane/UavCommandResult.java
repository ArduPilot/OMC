/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

public enum UavCommandResult {
    SUCCESS(0, "Success"),
    ERROR(1, "Error"),
    TIMEOUT(2,"Time Out"),
    DENIED(3, "Denied"),
    INVALID(4, "Invalid"),
    OTHER(5, "other");
    
    private int value;
    private String displayName;

    UavCommandResult(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getValue() {
        return value;
    }
}
