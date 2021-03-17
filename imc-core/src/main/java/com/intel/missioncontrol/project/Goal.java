/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

abstract class Goal extends AbstractGoal {

    Goal() {}

    Goal(Goal source) {
        super(source);
    }

    Goal(GoalSnapshot source) {
        super(source);
    }

    Goal(CompositeDeserializationContext context) {
        super(context);
    }

}
