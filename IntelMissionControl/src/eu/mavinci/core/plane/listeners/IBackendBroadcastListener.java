/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.listeners.IListener;
import eu.mavinci.core.listeners.IListener;

public interface IBackendBroadcastListener extends IListener, IAirplaneListenerBackend {

    /**
     * Backend List (Airport.getBackendList()) changed in structure what means new ports or new backends! about changes
     * in update time or in @Backend class listeners will NOT be informed
     *
     * <p>this is called by the airport AFTER the messages above
     */
    public void backendListChanged();

}
