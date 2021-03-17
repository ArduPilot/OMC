/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.connection;

import com.intel.missioncontrol.drone.connection.IReadOnlyConnectionItem;
import com.intel.missioncontrol.drone.connection.MavlinkDroneConnectionItem;
import com.intel.missioncontrol.drone.connection.TcpIpTransportType;
import com.intel.missioncontrol.hardware.IHardwareConfiguration;
import com.intel.missioncontrol.hardware.IHardwareConfigurationManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyProperty;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;

class ConnectionSettingsTableItem {
    private final UIAsyncBooleanProperty isOnline = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty isKnown = new UIAsyncBooleanProperty(this);
    private final UIAsyncStringProperty name = new UIAsyncStringProperty(this);
    private final UIAsyncStringProperty modelName = new UIAsyncStringProperty(this);
    private final UIAsyncObjectProperty<TcpIpTransportType> connectionTransportType = new UIAsyncObjectProperty<>(this);
    private final UIAsyncStringProperty host = new UIAsyncStringProperty(this);
    private final UIAsyncIntegerProperty port = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty systemId = new UIAsyncIntegerProperty(this);

    private final IReadOnlyConnectionItem connectionItem;

    ConnectionSettingsTableItem(IReadOnlyConnectionItem connectionItem) {
        name.bind(connectionItem.nameProperty());
        this.connectionItem = connectionItem;
    }

    ConnectionSettingsTableItem(
            IHardwareConfigurationManager hardwareConfigurationManager, MavlinkDroneConnectionItem connectionItem) {
        this.connectionItem = connectionItem;

        isOnline.bind(connectionItem.isOnlineProperty());
        isKnown.bind(connectionItem.isKnownProperty());
        name.bind(connectionItem.nameProperty());
        connectionTransportType.bind(connectionItem.transportTypeProperty());
        host.bind(connectionItem.hostProperty());
        port.bind(connectionItem.portProperty());
        systemId.bind(connectionItem.systemIdProperty());

        modelName.bind(
            Bindings.createStringBinding(
                () -> {
                    if (connectionItem.getPlatformId() == null) {
                        return null;
                    }

                    IHardwareConfiguration hardwareConfiguration =
                        hardwareConfigurationManager.getHardwareConfiguration(connectionItem.getPlatformId());

                    return hardwareConfiguration != null
                        ? hardwareConfiguration.getPlatformDescription().getName()
                        : null;
                },
                connectionItem.platformIdProperty()));
    }

    ReadOnlyProperty<Boolean> isOnlineProperty() {
        return isOnline;
    }

    ReadOnlyProperty<Boolean> isKnownProperty() {
        return isKnown;
    }

    ReadOnlyProperty<String> nameProperty() {
        return name;
    }

    ReadOnlyProperty<String> modelNameProperty() {
        return modelName;
    }

    ReadOnlyProperty<TcpIpTransportType> connectionTransportTypeProperty() {
        return connectionTransportType;
    }

    ReadOnlyProperty<String> hostProperty() {
        return host;
    }

    ReadOnlyProperty<Number> portProperty() {
        return port;
    }

    ReadOnlyProperty<Number> systemIdProperty() {
        return systemId;
    }

    IReadOnlyConnectionItem getConnectionItem() {
        return connectionItem;
    }
}
