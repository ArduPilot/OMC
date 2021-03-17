/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.geospatial.ITransform;
import com.intel.missioncontrol.geospatial.ProjectedPosition;
import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class FacadeGoal extends AbstractFacadeGoal {

    public FacadeGoal() {}

    public FacadeGoal(FacadeGoal source) {
        super(source);
    }

    public FacadeGoal(FacadeGoalSnapshot source) {
        super(source);
    }

    public FacadeGoal(CompositeDeserializationContext context) {
        super(context);
    }

    @Override
    void updateOrigin() {
        throw new NotImplementedException();
    }

    void applyTransformations(ITransform<ProjectedPosition> oldTransform, ITransform<ProjectedPosition> newTransform) {
        throw new NotImplementedException();
    }

}
