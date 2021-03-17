/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.intel.missioncontrol.settings.ISettings;
import com.intel.missioncontrol.settings.SettingsMetadata;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@SettingsMetadata(section = "mapLayers")
public class WWLayerSettings implements ISettings {

    private final StringProperty test = new SimpleStringProperty();

    public StringProperty testProperty() {
        return test;
    }

}
