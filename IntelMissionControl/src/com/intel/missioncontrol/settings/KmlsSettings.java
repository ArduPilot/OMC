/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;

@SettingsMetadata(section = "kmlsLoaded")
public class KmlsSettings implements ISettings {

    private AsyncListProperty<KmlSettings> kmls =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<KmlSettings>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    public AsyncListProperty<KmlSettings> kmlsProperty() {
        return kmls;
    }

    public KmlSettings newSettingsInstance() {
        return new KmlSettings();
    }

}
