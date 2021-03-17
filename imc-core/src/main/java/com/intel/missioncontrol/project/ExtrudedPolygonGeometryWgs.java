/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class ExtrudedPolygonGeometryWgs extends AbstractExtrudedPolygonGeometryWgs {

    public ExtrudedPolygonGeometryWgs() {}

    public ExtrudedPolygonGeometryWgs(ExtrudedPolygonGeometryWgs source) {
        super(source);
    }

    public ExtrudedPolygonGeometryWgs(ExtrudedPolygonGeometryWgsSnapshot source) {
        super(source);
    }

    public ExtrudedPolygonGeometryWgs(CompositeDeserializationContext context) {
        super(context);
    }

}
