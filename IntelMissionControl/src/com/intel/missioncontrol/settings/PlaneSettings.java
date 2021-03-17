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
