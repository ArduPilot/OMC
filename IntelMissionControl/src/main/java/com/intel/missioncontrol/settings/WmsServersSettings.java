/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import org.asyncfx.beans.AsyncObservable;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;

@SettingsMetadata(section = "wmssLoaded")
public class WmsServersSettings implements ISettings {

    public static final String SENTINEL_URL =
        "TODO SentinelHub URL";

    private AsyncListProperty<WmsServerSettings> wmss =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<WmsServerSettings>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(
                        wms ->
                            new AsyncObservable[] {
                                wms.enabledImageryLayersProperty(),
                                wms.enabledElevationLayersProperty(),
                                wms.enabledProperty(),
                                wms.nameProperty()
                            }))
                .create());

    // TODO should have been readonly but WmsManager has to bind it
    public AsyncListProperty<WmsServerSettings> wmssProperty() {
        return wmss;
    }
}
