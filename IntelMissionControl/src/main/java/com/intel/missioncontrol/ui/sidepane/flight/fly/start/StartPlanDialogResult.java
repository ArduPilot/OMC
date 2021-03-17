/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.start;

public class StartPlanDialogResult {
    private final StartPlanType startPlanType;
    private int startingWaypoint;

    public StartPlanDialogResult(StartPlanType startPlanType, int startingWaypoint) {
        this.startPlanType = startPlanType;
        this.startingWaypoint = startingWaypoint;
    }

    public StartPlanType getStartPlanType() {
        return startPlanType;
    }

    public int getStartingWaypoint() {
        return startingWaypoint;
    }
}
