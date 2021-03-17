/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class LensConfiguration extends AbstractLensConfiguration {

    public LensConfiguration() {}

    public LensConfiguration(LensConfiguration source) {
        super(source);
    }

    public LensConfiguration(LensConfigurationSnapshot source) {
        super(source);
    }

    LensConfiguration(CompositeDeserializationContext context) {
        super(context);
    }

}
