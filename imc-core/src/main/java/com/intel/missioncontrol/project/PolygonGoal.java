/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class PolygonGoal extends AbstractPolygonGoal {

    public PolygonGoal() {}

    public PolygonGoal(PolygonGoal source) {
        super(source);
    }

    public PolygonGoal(PolygonGoalSnapshot source) {
        super(source);
    }

    public PolygonGoal(CompositeDeserializationContext context) {
        super(context);
    }

    @Override
    void updateOrigin() {
        throw new NotImplementedException();
    }

}
