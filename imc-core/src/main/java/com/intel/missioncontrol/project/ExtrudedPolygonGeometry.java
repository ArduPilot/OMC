/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class ExtrudedPolygonGeometry extends AbstractExtrudedPolygonGeometry {

    public ExtrudedPolygonGeometry() {}

    public ExtrudedPolygonGeometry(ExtrudedPolygonGeometry source) {
        super(source);
    }

    public ExtrudedPolygonGeometry(ExtrudedPolygonGeometrySnapshot source) {
        super(source);
    }

    public ExtrudedPolygonGeometry(CompositeDeserializationContext context) {
        super(context);
    }

}
