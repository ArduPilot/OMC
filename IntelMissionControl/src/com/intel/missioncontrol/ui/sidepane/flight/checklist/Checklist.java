/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.checklist;

import eu.mavinci.core.plane.AirplaneType;

public class Checklist {

    private String planeName;
    private ChecklistItem[] checklistItem;

    public Checklist() {}

    public String getPlaneName() {
        return planeName;
    }

    public void setPlaneName(String planeName) {
        this.planeName = planeName;
    }

    public AirplaneType getAirplaneType() {
        return AirplaneType.getAirplaneType(planeName);
    }

    public ChecklistItem[] getChecklistItem() {
        return checklistItem;
    }

    public void setChecklistItem(ChecklistItem[] checklistItem) {
        this.checklistItem = checklistItem;
    }
}
