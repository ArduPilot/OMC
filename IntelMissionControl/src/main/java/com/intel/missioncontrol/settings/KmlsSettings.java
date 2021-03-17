/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;

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
