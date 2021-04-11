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
            return SidePanePage.CONNECT_DRONE;
        case DATA_PREVIEW:
            return SidePanePage.VIEW_DATASET;// TODO IMC-3133 will be replaced
        default:
            return SidePanePage.NONE;
        }
    }
}
