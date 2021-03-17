/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import org.asyncfx.beans.property.AsyncIntegerProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncStringProperty;

interface IMavlinkConnectionItem extends IConnectionItem {
    AsyncStringProperty hostProperty();

    default String getHost() {
        return hostProperty().get();
    }

    AsyncIntegerProperty portProperty();

    default int getPort() {
        return portProperty().get();
    }

    AsyncObjectProperty<TcpIpTransportType> transportTypeProperty();

    default TcpIpTransportType getTransportType() {
        return transportTypeProperty().get();
    }

    AsyncIntegerProperty systemIdProperty();

    default int getSystemId() {
        return systemIdProperty().get();
    }
}
