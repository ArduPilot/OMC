/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.AirplaneConnectorState;

public interface IAirplaneListenerConnectionState extends IAirplaneListener {

    /** new state of airplane connection */
    public void connectionStateChange(AirplaneConnectorState newState);
}
