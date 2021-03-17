/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.beans.AsyncObservable;
import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.AsyncStringProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncStringProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.collections.LockedList;
import eu.mavinci.core.obfuscation.IKeepAll;

@Serializable
public class WmsServerSettings implements IKeepAll {

    private final AsyncStringProperty url = new SimpleAsyncStringProperty(this);
    private final AsyncStringProperty name = new SimpleAsyncStringProperty(this);
    private final AsyncBooleanProperty enabled = new SimpleAsyncBooleanProperty(this);

    private final AsyncListProperty<WmsLayerEnabledSettings> enabledElevationLayers =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<WmsLayerEnabledSettings>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(wms -> new AsyncObservable[] {wms.enabledProperty()}))
                .create());

    private final AsyncListProperty<WmsLayerEnabledSettings> enabledImageryLayers =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<WmsLayerEnabledSettings>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(wms -> new AsyncObservable[] {wms.enabledProperty()}))
                .create());

    public AsyncStringProperty urlProperty() {
        return url;
    }

    public AsyncBooleanProperty enabledProperty() {
        return enabled;
    }

    public String getUri() {
        return url.get();
    }

    public Boolean getEnabled() {
        return enabled.get();
    }

    public AsyncListProperty<WmsLayerEnabledSettings> enabledElevationLayersProperty() {
        return enabledElevationLayers;
    }

    public AsyncListProperty<WmsLayerEnabledSettings> enabledImageryLayersProperty() {
        return enabledImageryLayers;
    }

    @Override
    public boolean equals(Object obj) {
        return getUri().equals(((WmsServerSettings)obj).getUri());
    }

    public AsyncStringProperty nameProperty() {
        return name;
    }

    public WmsLayerEnabledSettings get(String name) {
        try (LockedList<WmsLayerEnabledSettings> settingsList = enabledImageryLayers.lock()) {
            for (WmsLayerEnabledSettings settings : settingsList) {
                if (settings.getName().equals(name)) {
                    return settings;
                }
            }
        }

        try (LockedList<WmsLayerEnabledSettings> settingsList = enabledElevationLayers.lock()) {
            for (WmsLayerEnabledSettings settings : settingsList) {
                if (settings.getName().equals(name)) {
                    return settings;
                }
            }
        }

        return null;
    }
}
