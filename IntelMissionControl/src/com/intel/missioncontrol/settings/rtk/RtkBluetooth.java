/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings.rtk;

import com.intel.missioncontrol.settings.Serializable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** @author Vladimir Iordanov */
@Serializable
public class RtkBluetooth {
    private final StringProperty device = new SimpleStringProperty();
    private final StringProperty service = new SimpleStringProperty();

    public String getDevice() {
        return device.get();
    }

    public StringProperty deviceProperty() {
        return device;
    }

    public String getService() {
        return service.get();
    }

    public StringProperty serviceProperty() {
        return service;
    }
}
