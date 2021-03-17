/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.drone.connection.MavlinkDroneConnectionItem;
import org.asyncfx.beans.AsyncObservable;
import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.PropertyMetadata;
import org.asyncfx.beans.property.SimpleAsyncBooleanProperty;
import org.asyncfx.beans.property.SimpleAsyncIntegerProperty;
import org.asyncfx.beans.property.SimpleAsyncListProperty;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;

@SettingsMetadata(section = "connectionDialog")
public class ConnectionSettings implements ISettings {
    private static final int defaultPort = 14550;

    private final AsyncBooleanProperty acceptIncomingConnections =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(true).create());
    private final AsyncBooleanProperty broadcastOwnHeartbeat =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());
    private final AsyncIntegerProperty receivingPort =
        new SimpleAsyncIntegerProperty(this, new PropertyMetadata.Builder<Number>().initialValue(defaultPort).create());
    private final AsyncListProperty<MavlinkDroneConnectionItem> connectionItemsList =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<MavlinkDroneConnectionItem>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(
                        e ->
                            new AsyncObservable[] {
                                e.nameProperty(),
                                e.platformIdProperty(),
                                e.hostProperty(),
                                e.portProperty(),
                                e.transportTypeProperty(),
                                e.systemIdProperty()
                            }))
                .create());

    @Override
    public void onLoaded() {
        try (LockedList<MavlinkDroneConnectionItem> list = connectionItemsList.lock()) {
            for (MavlinkDroneConnectionItem item : list) {
                item.initialize();
            }
        }
    }

    public AsyncBooleanProperty acceptIncomingConnectionsProperty() {
        return acceptIncomingConnections;
    }

    public AsyncBooleanProperty broadcastOwnHeartbeatProperty() {
        return broadcastOwnHeartbeat;
    }

    public AsyncIntegerProperty receivingPortProperty() {
        return receivingPort;
    }

    public AsyncListProperty<MavlinkDroneConnectionItem> connectionItemsListProperty() {
        return connectionItemsList;
    }
}
