package com.intel.missioncontrol.ui.navigation;

public enum SidePanePage {
    NONE,
    RECENT_MISSIONS,

    START_PLANNING,
    CHOOSE_AOI,
    EDIT_FLIGHTPLAN,

    CONNECT_DRONE,
    CONNECT_DRONE_HELP,
    FLY_DRONE,

    DATA_IMPORT, // TODO IMC-3133 will be replaced
    VIEW_DATASET,
    VIEW_DATASET_HELP;

    public WorkflowStep getWorkflowStep() {
        switch (this) {
        case START_PLANNING:
        case CHOOSE_AOI:
        case EDIT_FLIGHTPLAN:
            return WorkflowStep.PLANNING;
        case DATA_IMPORT: // TODO IMC-3133 will be replaced
        case VIEW_DATASET:
        case VIEW_DATASET_HELP:
            return WorkflowStep.DATA_PREVIEW;
        case CONNECT_DRONE:
        case CONNECT_DRONE_HELP:
            return WorkflowStep.FLIGHT;
        case FLY_DRONE:
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
        case DATA_IMPORT: // TODO IMC-3133 will be replaced
            return SidePaneTab.DATA_IMPORT;
        case VIEW_DATASET:
            return SidePaneTab.VIEW_DATASET;
        case VIEW_DATASET_HELP:
            return SidePaneTab.VIEW_DATASET_HELP;
        case CONNECT_DRONE:
            return SidePaneTab.CONNECT_DRONE;
        case CONNECT_DRONE_HELP:
            return SidePaneTab.CONNECT_DRONE_HELP;
        case FLY_DRONE:
            return SidePaneTab.FLY_DRONE;
        default:
            return SidePaneTab.NONE;
        }
    }
}
