/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class ExtrudedPolygonObject extends AbstractExtrudedPolygonObject {

    public ExtrudedPolygonObject() {}

    public ExtrudedPolygonObject(ExtrudedPolygonObject source) {
        super(source);
    }

    public ExtrudedPolygonObject(ExtrudedPolygonObjectSnapshot source) {
        super(source);
    }

    public ExtrudedPolygonObject(CompositeDeserializationContext context) {
        super(context);
    }

    @Override
    void updateOrigin() {
        throw new NotImplementedException();
    }

}
