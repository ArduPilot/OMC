/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings.rtk;

import com.intel.missioncontrol.settings.Serializable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/** @author Vladimir Iordanov */
@Serializable
public class InternalSettings {
    private final ObjectProperty<RtkSerial> serialSettings = new SimpleObjectProperty<>();

    public RtkSerial getSerialSettings() {
        return serialSettings.get();
    }

    public ObjectProperty<RtkSerial> serialSettingsProperty() {
        return serialSettings;
    }
}
