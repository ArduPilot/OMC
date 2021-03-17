/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.project.serialization.DeserializationContext;

abstract class Goal extends AbstractGoal {

    private Mission mission;

    Goal() {}

    Goal(IGoal source) {
        super(source);
    }

    Goal(DeserializationContext context) {
        super(context);
    }

    Mission getMission() {
        return mission;
    }

    void setMission(Mission mission) {
        this.mission = mission;
    }

}
