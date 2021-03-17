/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class PayloadConfiguration extends AbstractPayloadConfiguration {

    PayloadConfiguration() {}

    PayloadConfiguration(PayloadConfiguration source) {
        super(source);
    }

    PayloadConfiguration(PayloadConfigurationSnapshot source) {
        super(source);
    }

    PayloadConfiguration(CompositeDeserializationContext context) {
        super(context);
    }

}
