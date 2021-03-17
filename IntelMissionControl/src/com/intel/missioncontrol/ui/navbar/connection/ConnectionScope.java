/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.connection;

public interface ConnectionScope {
    void setDetectedUavListDisabled(boolean disabled);

    void setSelectedUav(UnmannedAerialVehicle uav);

    void setUavPinLabelVisible(boolean visible);

    void setUavPinsListVisible(boolean visible);

    void setUavPinsListDisabled(boolean disabled);

    void setUavPinValue(String uavPin);

    void setUsbConnectorInfoManaged(boolean managed);

    void setUsbConnectorInfoVisible(boolean visible);

    void setShortUavInfoManaged(boolean managed);

    void setShortUavInfoVisible(boolean visible);

    void setConnectionState(ConnectionState connectionState);

    void setConnectButtonDisabled(boolean disabled);

    void setUserDisconnectCheckVisible(boolean visible);

    void setUserDisconnectCheckMarked(boolean marked);

    void setDisconnectUAVNow(boolean disconnectNow);
}
