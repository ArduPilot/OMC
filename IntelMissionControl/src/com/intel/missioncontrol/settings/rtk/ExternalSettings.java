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
public class ExternalSettings {
    private final ObjectProperty<RtkSerial> serialSettings = new SimpleObjectProperty<>();
    private final ObjectProperty<RtkUdp> udpSettings = new SimpleObjectProperty<>();
    private final ObjectProperty<RtkBluetooth> bluetoothSettings = new SimpleObjectProperty<>();

    public RtkSerial getSerialSettings() {
        return serialSettings.get();
    }

    public ObjectProperty<RtkSerial> serialSettingsProperty() {
        return serialSettings;
    }

    public RtkUdp getUdpSettings() {
        return udpSettings.get();
    }

    public ObjectProperty<RtkUdp> udpSettingsProperty() {
        return udpSettings;
    }

    public RtkBluetooth getBluetoothSettings() {
        return bluetoothSettings.get();
    }

    public ObjectProperty<RtkBluetooth> bluetoothSettingsProperty() {
        return bluetoothSettings;
    }
}
