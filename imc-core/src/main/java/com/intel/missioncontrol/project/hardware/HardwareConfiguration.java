/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class HardwareConfiguration extends AbstractHardwareConfiguration {

    public HardwareConfiguration() {}

    public HardwareConfiguration(HardwareConfiguration source) {
        super(source);
    }

    public HardwareConfiguration(HardwareConfigurationSnapshot source) {
        super(source);
    }

    public HardwareConfiguration(CompositeDeserializationContext context) {
        super(context);
    }

}
