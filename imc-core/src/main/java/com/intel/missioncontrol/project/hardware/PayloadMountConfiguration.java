/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class PayloadMountConfiguration extends AbstractPayloadMountConfiguration {

    public PayloadMountConfiguration() {}

    public PayloadMountConfiguration(PayloadMountConfiguration source) {
        super(source);
    }

    public PayloadMountConfiguration(PayloadMountConfigurationSnapshot source) {
        super(source);
    }

    public PayloadMountConfiguration(CompositeDeserializationContext context) {
        super(context);
    }

}
