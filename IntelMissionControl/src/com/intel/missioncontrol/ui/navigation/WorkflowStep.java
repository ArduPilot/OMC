/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navigation;

public enum WorkflowStep {
    NONE,
    PLANNING,
    FLIGHT,
    DATA_PREVIEW;

    public SidePanePage getFirstSidePanePage() {
        switch (this) {
        case NONE:
            return SidePanePage.RECENT_MISSIONS;
        case PLANNING:
            return SidePanePage.START_PLANNING;
        case FLIGHT:
            return SidePanePage.FLIGHT_CONNECTED;
        case DATA_PREVIEW:
            return SidePanePage.DATA_IMPORT;
        default:
            return SidePanePage.NONE;
        }
    }
}
