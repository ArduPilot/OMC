/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class FacadeGeometry extends AbstractFacadeGeometry {

    public FacadeGeometry() {}

    public FacadeGeometry(FacadeGeometry source) {
        super(source);
    }

    public FacadeGeometry(FacadeGeometrySnapshot source) {
        super(source);
    }

    public FacadeGeometry(CompositeDeserializationContext context) {
        super(context);
    }

}
