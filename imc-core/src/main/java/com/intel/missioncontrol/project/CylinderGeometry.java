/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class CylinderGeometry extends AbstractCylinderGeometry {

    public CylinderGeometry() {}

    public CylinderGeometry(CylinderGeometry source) {
        super(source);
    }

    public CylinderGeometry(CylinderGeometrySnapshot source) {
        super(source);
    }

    public CylinderGeometry(CompositeDeserializationContext context) {
        super(context);
    }

}
