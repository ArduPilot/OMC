/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class PolygonObject extends AbstractPolygonObject {

    public PolygonObject() {}

    public PolygonObject(PolygonObject source) {
        super(source);
    }

    public PolygonObject(PolygonObjectSnapshot source) {
        super(source);
    }

    public PolygonObject(CompositeDeserializationContext context) {
        super(context);
    }

    @Override
    void updateOrigin() {
        throw new NotImplementedException();
    }

}
