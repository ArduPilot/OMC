/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.geometry.Vec2;
import com.intel.missioncontrol.geospatial.ITransform;
import com.intel.missioncontrol.geospatial.ProjectedPosition;
import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

abstract class Placeable extends AbstractPlaceable {

    Placeable() {}

    Placeable(Placeable source) {
        super(source);
    }

    Placeable(PlaceableSnapshot source) {
        super(source);
    }

    Placeable(CompositeDeserializationContext context) {
        super(context);
    }

    public Vec2 transformToMission(Vec2 value) {
        throw new NotImplementedException();
    }

    public Vec2 transformFromMission(Vec2 value) {
        throw new NotImplementedException();
    }

    // consistency within each placeable that the center corresponds to the average of all corners
    void updateOrigin() {
        throw new NotImplementedException();
    }

    // if placeable has relative coordinates, then when parent transformation changes, it will require all relative
    // coordinates to update
    void applyTransformations(ITransform<ProjectedPosition> oldTransform, ITransform<ProjectedPosition> newTransform) {
        return;
    }

}
