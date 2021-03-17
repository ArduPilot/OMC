/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navigation;

public enum SidePanePage {
    NONE,
    RECENT_MISSIONS,

    START_PLANNING,
    CHOOSE_AOI,
    EDIT_FLIGHTPLAN,

    FLIGHT_DISCONNECTED,
    FLIGHT_CONNECTED,

    DATA_IMPORT,
    TRANSFERRING_DATA,
    VIEW_DATASET,
    VIEW_DATASET_HELP;

    public WorkflowStep getWorkflowStep() {
        switch (this) {
        case START_PLANNING:
        case CHOOSE_AOI:
        case EDIT_FLIGHTPLAN:
            return WorkflowStep.PLANNING;
        case DATA_IMPORT:
        case VIEW_DATASET:
        case TRANSFERRING_DATA:
        case VIEW_DATASET_HELP:
            return WorkflowStep.DATA_PREVIEW;
        case FLIGHT_DISCONNECTED:
        case FLIGHT_CONNECTED:
            return WorkflowStep.FLIGHT;
        default:
            return WorkflowStep.NONE;
        }
    }

    public SidePaneTab getTab() {
        switch (this) {
        case RECENT_MISSIONS:
            return SidePaneTab.RECENT_MISSIONS;
        case START_PLANNING:
            return SidePaneTab.START_PLANNING;
        case CHOOSE_AOI:
        case EDIT_FLIGHTPLAN:
            return SidePaneTab.EDIT_FLIGHTPLAN;
        case DATA_IMPORT:
            return SidePaneTab.DATA_IMPORT;
        case VIEW_DATASET:
            return SidePaneTab.VIEW_DATASET;
        case TRANSFERRING_DATA:
            return SidePaneTab.TRANSFERRING_DATA;
        case VIEW_DATASET_HELP:
            return SidePaneTab.VIEW_DATASET_HELP;
        case FLIGHT_DISCONNECTED:
            return SidePaneTab.FLIGHT_DISCONNECTED;
        case FLIGHT_CONNECTED:
            return SidePaneTab.FLIGHT_CONNECTED;
        default:
            return SidePaneTab.NONE;
        }
    }
}
