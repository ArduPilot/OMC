/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.hardware;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class GenericCameraConfiguration extends AbstractGenericCameraConfiguration {

    public GenericCameraConfiguration() {}

    public GenericCameraConfiguration(GenericCameraConfiguration source) {
        super(source);
    }

    public GenericCameraConfiguration(GenericCameraConfigurationSnapshot source) {
        super(source);
    }

    GenericCameraConfiguration(CompositeDeserializationContext context) {
        super(context);
    }

}
