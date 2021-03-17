/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;

@SettingsMetadata(section = "debug")
public class DebugSettings implements ISettings {

    private final AsyncBooleanProperty pullBroadcasts =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    public AsyncBooleanProperty pullBroadcastsProperty() {
        return pullBroadcasts;
    }

    public boolean isPullBroadcasts() {
        return pullBroadcasts.get();
    }

}
