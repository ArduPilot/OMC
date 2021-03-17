/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;

@SettingsMetadata(section = "debug")
public class DebugSettings implements ISettings {

    private final AsyncBooleanProperty pullBroadcasts =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty extractDescriptionsOnStartup =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty extractTemplatesOnStartup =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    private final AsyncBooleanProperty extractAirspacesOnStartup =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    public AsyncBooleanProperty pullBroadcastsProperty() {
        return pullBroadcasts;
    }

    public boolean isPullBroadcasts() {
        return pullBroadcasts.get();
    }

    public AsyncBooleanProperty extractDescriptionsOnStartupProperty() {
        return extractDescriptionsOnStartup;
    }

    public boolean isExtractDescriptionsOnStartup() {
        return extractDescriptionsOnStartup.get();
    }

    public AsyncBooleanProperty extractAirspacesOnStartupProperty() {
        return extractAirspacesOnStartup;
    }

    public boolean isExtractAirspacesOnStartup() {
        return extractAirspacesOnStartup.get();
    }

    public AsyncBooleanProperty extractTemplatesOnStartupProperty() {
        return extractTemplatesOnStartup;
    }

    public boolean isExtractTemplatesOnStartup() {
        return extractTemplatesOnStartup.get();
    }
}
