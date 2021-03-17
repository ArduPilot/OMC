/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import eu.mavinci.desktop.gui.doublepanel.ntripclient.NtripConnectionSettings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

@Deprecated(since = "Switch to async properties")
@SettingsMetadata(section = "rtkNtripConnections")
public class NtripConnections implements ISettings {

    private ListProperty<NtripConnectionSettings> list = new SimpleListProperty<>(FXCollections.observableArrayList());

    public boolean contains(NtripConnectionSettings settings) {
        return list.contains(settings);
    }

    public void add(NtripConnectionSettings settings) {
        list.addAll(settings);
    }

    public ListProperty<NtripConnectionSettings> connections() {
        return list;
    }

    public void remove(NtripConnectionSettings settings) {
        list.remove(settings);
    }

}
