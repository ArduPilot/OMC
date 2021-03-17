/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.ui.navbar.connection.model.ConnectionItem;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

@SettingsMetadata(section = "connection")
public class ConnectionSettings implements ISettings {
    private final BooleanProperty acceptIncomingConnections = new SimpleBooleanProperty(true);
    private final IntegerProperty receivingPort = new SimpleIntegerProperty(14550);
    private final ListProperty<ConnectionItem> connectionItemsList =
        new SimpleListProperty<>(FXCollections.observableArrayList());

    public BooleanProperty acceptIncomingConnectionsProperty() {
        return acceptIncomingConnections;
    }

    public IntegerProperty receivingPortProperty() {
        return receivingPort;
    }

    public ListProperty<ConnectionItem> connectionItemsListProperty() {
        return connectionItemsList;
    }

}
