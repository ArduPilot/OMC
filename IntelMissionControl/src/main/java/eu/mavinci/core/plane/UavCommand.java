/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

/* Some commands for the UAV */

public enum UavCommand {

    TAKE_OFF(0, "Take Off"),
    RUN_MISSION(1, "Run Mission"),
    RESUME_MISSION(2, "Resume Mission"),
    SEND_MISSION(3, "Send Mission"),
    PAUSE_MISSION(4, "Mission Pause"),
    MISSION_REQUEST_LIST(5, "Mission Request"),
    MISSION_REQUEST_ITEM(6, "Mission Item Request"),
    RETURN_TO_LAUNCH(7, "Return to launch"),
    UAV_LAND(8, "Land Uav"),
    OTHER(9, "Other");

    private int value;
    private String displayName;

    UavCommand(int value, String displayName) {
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
