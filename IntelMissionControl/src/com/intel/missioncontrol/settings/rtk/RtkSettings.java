/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings.rtk;

import com.intel.missioncontrol.settings.ISettings;
import com.intel.missioncontrol.settings.SettingsMetadata;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/** @author Vladimir Iordanov */
@Deprecated(since = "Change to async properties")
@SettingsMetadata(section = "rtkSettings")
public class RtkSettings implements ISettings {
    private final ObjectProperty<NtripSettings> ntripSettings = new SimpleObjectProperty<>();
    private final ObjectProperty<InternalSettings> internalSettings = new SimpleObjectProperty<>();
    private final ObjectProperty<ExternalSettings> externalSettings = new SimpleObjectProperty<>();
    private final ObjectProperty<String> lastUsedConfig = new SimpleObjectProperty<>();

    public NtripSettings getNtripSettings() {
        return ntripSettings.get();
    }

    public ObjectProperty<NtripSettings> ntripSettingsProperty() {
        return ntripSettings;
    }

    public InternalSettings getInternalSettings() {
        return internalSettings.get();
    }

    public ObjectProperty<InternalSettings> internalSettingsProperty() {
        return internalSettings;
    }

    public ExternalSettings getExternalSettings() {
        return externalSettings.get();
    }

    public ObjectProperty<ExternalSettings> externalSettingsProperty() {
        return externalSettings;
    }

    public String getLastUsedConfig() {
        return lastUsedConfig.get();
    }

    public ObjectProperty<String> lastUsedConfigProperty() {
        return lastUsedConfig;
    }

    public static RtkSettings createDefault() {
        RtkSettings rtkSettings = new RtkSettings();
        NtripSettings ntripSettings = new NtripSettings();
        ntripSettings.urlSettingsProperty().setValue(new RtkHttpConnection());

        InternalSettings internalSettings = new InternalSettings();
        internalSettings.serialSettingsProperty().setValue(new RtkSerial());

        ExternalSettings externalSettings = new ExternalSettings();
        externalSettings.serialSettingsProperty().setValue(new RtkSerial());
        externalSettings.udpSettingsProperty().setValue(new RtkUdp());
        externalSettings.bluetoothSettingsProperty().setValue(new RtkBluetooth());

        rtkSettings.ntripSettingsProperty().setValue(ntripSettings);
        rtkSettings.internalSettingsProperty().setValue(internalSettings);
        rtkSettings.externalSettingsProperty().setValue(externalSettings);

        return rtkSettings;
    }
}
