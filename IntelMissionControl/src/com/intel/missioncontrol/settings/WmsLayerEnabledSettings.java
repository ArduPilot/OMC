/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.AsyncStringProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncStringProperty;

@Serializable
public class WmsLayerEnabledSettings {

    private final AsyncStringProperty name;
    private AsyncBooleanProperty enabled;

    public WmsLayerEnabledSettings() {
        this.name = new SimpleAsyncStringProperty(this);
        this.enabled =
            new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());
    }

    public WmsLayerEnabledSettings(String name, AsyncBooleanProperty enabledProperty) {
        this.name = new SimpleAsyncStringProperty(this);
        this.name.setValue(name);
        this.enabled = enabledProperty;
    }

    public AsyncStringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public AsyncBooleanProperty enabledProperty() {
        return enabled;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

}
