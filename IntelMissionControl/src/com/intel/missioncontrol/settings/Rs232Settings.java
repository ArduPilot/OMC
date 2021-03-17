/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

@Deprecated(since = "Switch to async properties")
@SettingsMetadata(section = "rs232Settings")
public class Rs232Settings implements ISettings {

    public SimpleStringProperty getPortProperty(String key) {
        return new SimpleStringProperty(this, key + ".port");
    }

    public SimpleStringProperty getPortProperty(String key, String defaultValue) {
        return new SimpleStringProperty(this, key + ".port", defaultValue);
    }

    public SimpleIntegerProperty getBitRateProperty(String key, int defaultValue) {
        return new SimpleIntegerProperty(this, key + ".bitRate", defaultValue);
    }

    public SimpleIntegerProperty getBitRateProperty(String key) {
        return new SimpleIntegerProperty(this, key + ".bitRate");
    }

    public Integer getBitRate(String key, int defaultValue) {
        return getBitRateProperty(key + ".bitRate", defaultValue).get();
    }

    public Integer getDataBits(String key, int defaultValue) {
        return getBitRateProperty(key + ".dataBits", defaultValue).get();
    }

    public SimpleIntegerProperty getDataBitsProperty(String key) {
        return new SimpleIntegerProperty(this, key + ".dataBits");
    }

    public Integer getStopBits(String key, int defaultValue) {
        return getBitRateProperty(key + ".stopBits", defaultValue).get();
    }

    public SimpleIntegerProperty getStopBitsProperty(String key) {
        return new SimpleIntegerProperty(this, key + ".stopBits");
    }

    public Integer getParity(String key, int defaultValue) {
        return getBitRateProperty(key + ".parity", defaultValue).get();
    }

    public SimpleIntegerProperty getParityProperty(String key) {
        return new SimpleIntegerProperty(this, key + ".parity");
    }

}
