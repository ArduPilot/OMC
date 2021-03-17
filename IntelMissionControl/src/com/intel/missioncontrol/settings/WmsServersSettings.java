/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.beans.AsyncObservable;
import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;

@SettingsMetadata(section = "wmssLoaded")
public class WmsServersSettings implements ISettings {

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
