/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.NotImplementedException;
import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class FacadeObject extends AbstractFacadeObject {

    public FacadeObject() {}

    public FacadeObject(FacadeObject source) {
        super(source);
    }

    public FacadeObject(FacadeObjectSnapshot source) {
        super(source);
    }

    public FacadeObject(CompositeDeserializationContext context) {
        super(context);
    }

    @Override
    void updateOrigin() {
        throw new NotImplementedException();
    }

}
