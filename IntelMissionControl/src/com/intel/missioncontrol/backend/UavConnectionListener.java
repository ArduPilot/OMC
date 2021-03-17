/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.backend;

import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionScope;
import com.intel.missioncontrol.ui.navbar.connection.ConnectionState;
import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.listeners.IAirplaneListenerConnectionState;
import eu.mavinci.plane.IAirplane;

public class UavConnectionListener implements IAirplaneListenerConnectionState {

    private final ConnectionScope scope;
    private volatile IAirplane airplane;
    private AirplaneConnectorState previousState;

    public UavConnectionListener(ConnectionScope scope) {
        this.scope = scope;
        this.previousState = AirplaneConnectorState.unconnected;
    }

    public void setPlane(IAirplane airplane) {
        if (airplane == null && this.airplane != null) {
            this.airplane.removeListener(this);
        }

        this.airplane = airplane;
    }

    // ALERT this code SUCKS!
    // We need to remove listener from airplane if IMC loses connection to UAV and connector fires disconnect event.
    @Override
    public void connectionStateChange(AirplaneConnectorState newState) {
        if (previousState != AirplaneConnectorState.unconnected && newState == AirplaneConnectorState.unconnected) {
            Dispatcher.postToUI(
                () -> {
                    scope.setDetectedUavListDisabled(false);

                    scope.setUavPinLabelVisible(false);

                    scope.setUavPinsListVisible(false);
                    scope.setUavPinsListDisabled(false);
                    scope.setUavPinValue(null);

                    scope.setUsbConnectorInfoManaged(true);
                    scope.setUsbConnectorInfoVisible(false);

                    scope.setShortUavInfoManaged(false);
                    scope.setShortUavInfoVisible(false);

                    scope.setConnectionState(ConnectionState.NOT_CONNECTED);

                    scope.setConnectButtonDisabled(true);

                    scope.setUserDisconnectCheckVisible(false);
                    scope.setUserDisconnectCheckMarked(false);
                });

            airplane.removeListener(this);
            airplane = null;
        }

        previousState = newState;
    }
}
