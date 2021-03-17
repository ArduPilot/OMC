/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.dataset;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.settings.ISettings;
import com.intel.missioncontrol.settings.SettingsMetadata;

@SettingsMetadata(section = "datasetVisibility")
public class DatasetLayerVisibilitySettings implements ISettings {

    private final AsyncBooleanProperty showCurrentFlightplan = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty showOtherDatasets = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty showCurrentDatasets =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());

    public AsyncBooleanProperty showCurrentFlightplanProperty() {
        return showCurrentFlightplan;
    }

    public AsyncBooleanProperty showOtherDatasetsProperty() {
        return showOtherDatasets;
    }

    public AsyncBooleanProperty showCurrentDatasetsProperty() {
        return showCurrentDatasets;
    }

    public boolean isShowCurrentFlightplan() {
        return showCurrentFlightplan.get();
    }

    public boolean isShowOtherDatasets() {
        return showOtherDatasets.get();
    }

    public Boolean isShowCurrentDatasets() {
        return showCurrentDatasets.get();
    }

}
