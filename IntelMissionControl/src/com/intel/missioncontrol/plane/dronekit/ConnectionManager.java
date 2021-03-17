/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit;

import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;

public interface ConnectionManager {
    enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR;

        public boolean canConnect() {
            return this == DISCONNECTED || this == ERROR;
        }
    }

    interface ConnectionError {
        int getCode();
        String getMessage();
    }

    ReadOnlyProperty<ConnectionError> errorProperty();
    ReadOnlyProperty<ConnectionState> connectionStateProperty();
    ReadOnlyProperty<ConnectionParameter> connectionParamsProperty();
    ReadOnlyBooleanProperty connectedProperty();

    boolean isConnected();

    void connect(ConnectionParameter param);
    void disconnect();
}
