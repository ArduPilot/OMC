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

@SettingsMetadata(section = "plane")
public class PlaneSettings implements ISettings {

    private final AsyncListProperty<String> pinsHistory =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<String>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    public AsyncObservableList<String> getPinsHistory() {
        return pinsHistory.get();
    }

    public AsyncListProperty<String> pinsHistoryProperty() {
        return pinsHistory;
    }

    public void setPinsHistory(AsyncObservableList<String> pinsHistory) {
        this.pinsHistory.set(pinsHistory);
    }

}
