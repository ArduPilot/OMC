/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.serialization.SerializationOptions;

public class ProjectSerializationOptions implements SerializationOptions {

    private final boolean serializeTrackingState;

    public ProjectSerializationOptions(boolean serializeTrackingState) {
        this.serializeTrackingState = serializeTrackingState;
    }

    public boolean isSerializeTrackingState() {
        return serializeTrackingState;
    }

}
