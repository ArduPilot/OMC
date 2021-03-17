/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.tcp;

import eu.mavinci.core.plane.AirplaneConnectorState;
import eu.mavinci.core.plane.IAirplaneConnector;
import eu.mavinci.core.plane.listeners.IAirplaneListener;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDelegator;
import eu.mavinci.desktop.main.debug.Debug;
import java.util.logging.Level;

public abstract class AAirplaneConnector implements IAirplaneConnector, IAirplaneListener {

    protected IAirplaneListenerDelegator rootHandler;

    protected volatile AirplaneConnectorState curConnectorState = AirplaneConnectorState.unconnected;

    public AirplaneConnectorState getConnectionState() {
        return curConnectorState;
    }

    long lastFullyConnectedTime = 0;

    public long getLastFullyConnectedTime() {
        return lastFullyConnectedTime;
    }

    protected synchronized void fireConnectionState(AirplaneConnectorState newConnectorState) {
        if (curConnectorState == newConnectorState) {
            return;
        }

        if (newConnectorState == AirplaneConnectorState.fullyConnected) {
            lastFullyConnectedTime = System.currentTimeMillis();
        }

        // System.out.println("fireNewConState" + newConnectorState);
        // Thread.dumpStack();
        curConnectorState = newConnectorState;
        try {
            IAirplaneListenerDelegator rootHandlerT = rootHandler;
            if (rootHandlerT != null) {
                rootHandlerT.connectionStateChange(newConnectorState);
            }
        } catch (Exception e) {
            Debug.getLog().log(Level.WARNING, "Problems propagate new connectionDialog state", e);
        }
    }

    public void setRootHandler(IAirplaneListenerDelegator handler) {
        if (rootHandler != null) {
            rootHandler.removeListener(this);
        }

        rootHandler = handler;
        rootHandler.addListenerAtSecond(this);
    }

}
