/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.airmap.airmapsdk.networking.services.MappingService;
import com.intel.missioncontrol.airmap.layer.AirMapTileLoader2;
import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.AsyncStringProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncStringProperty;

@Serializable
public class AirspacesTypeEnabledSettings {

    private final AsyncStringProperty name;
    private final AsyncBooleanProperty enabled;

    private AirspacesTypeEnabledSettings() {
        this.name = new SimpleAsyncStringProperty(this);
        this.enabled = new SimpleAsyncBooleanProperty(this);
    }

    public AirspacesTypeEnabledSettings(String name, boolean enabledByDefault) {
        this();
        this.name.set(name);
        this.enabled.overrideMetadata(new PropertyMetadata.Builder<Boolean>().initialValue(enabledByDefault).create());
    }

    @PostDeserialize
    @SuppressWarnings("unused")
    private void postDeserialize() {
        // After deserialization, we need to override the metadata of the existing properties to support default values.
        String name = this.name.get();
        for (MappingService.AirMapAirspaceType entry : AirMapTileLoader2.getAirmapSearchTypes()) {
            if (entry.toString().equals(name)) {
                this.name.overrideMetadata(
                    new PropertyMetadata.Builder<String>().initialValue(entry.toString()).create());

                // because overrideMetadata also resets the value that we read from json...
                boolean enabled = this.enabled.get();
                this.enabled.overrideMetadata(
                    new PropertyMetadata.Builder<Boolean>().initialValue(entry.isDefaultEnabled()).create());
                this.enabled.set(enabled);
                break;
            }
        }
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
