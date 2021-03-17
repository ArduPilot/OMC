/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class PolygonGeometry extends AbstractPolygonGeometry {

    public PolygonGeometry() {}

    public PolygonGeometry(PolygonGeometry source) {
        super(source);
    }

    public PolygonGeometry(PolygonGeometrySnapshot source) {
        super(source);
    }

    public PolygonGeometry(CompositeDeserializationContext context) {
        super(context);
    }

}
