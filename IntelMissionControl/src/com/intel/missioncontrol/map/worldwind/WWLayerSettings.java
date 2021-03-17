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

    private final StringProperty mapboxAccessToken = new SimpleStringProperty("");

    private final StringProperty mapboxMapIdSat = new SimpleStringProperty("");

    private final StringProperty mapboxMapIdHybrid = new SimpleStringProperty("");

    private final StringProperty mapboxMapIdStreets = new SimpleStringProperty("");

    private final StringProperty googleAccessToken = new SimpleStringProperty("");

    private final StringProperty hereAppCode = new SimpleStringProperty("");
    private final StringProperty hereAppId = new SimpleStringProperty("");

    public StringProperty hereAppCodeProperty() {
        return hereAppCode;
    }

    public StringProperty hereAppIdProperty() {
        return hereAppId;
    }

    public StringProperty googleAccessTokenProperty() {
        return googleAccessToken;
    }

    public StringProperty mapboxAccessTokenProperty() {
        return mapboxAccessToken;
    }

    public StringProperty mapboxMapIdHybridProperty() {
        return mapboxMapIdHybrid;
    }

    public StringProperty mapboxMapIdSatProperty() {
        return mapboxMapIdSat;
    }

    public StringProperty mapboxMapIdStreetsProperty() {
        return mapboxMapIdStreets;
    }
}
