/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane;

import eu.mavinci.core.plane.listeners.IAirplaneListener;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDelegator;
import eu.mavinci.core.plane.listeners.IAirplaneListener;
import eu.mavinci.core.plane.listeners.IAirplaneListenerDelegator;

public interface IAirplaneConnector extends IAirplaneExternal, IAirplaneListener {

    // /**
    // * Set the Handler for receiving data from the airplane
    // *
    // * @param listener
    // */
    public void setRootHandler(IAirplaneListenerDelegator handler);
    // public IAirplaneListenerDelegator getRootHandler();

    public AirplaneConnectorState getConnectionState();

    /** @return true is this is connected to simulation */
    public boolean isSimulation();

    /** @return true is this is manned edition */
    public long getLastFullyConnectedTime();

    void cancelLaunch();

    void cancelLanding();
}
