/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.DebugData;
import eu.mavinci.core.plane.sendableobjects.DebugData;

public interface IAirplaneListenerDebug extends IAirplaneListener {

    /**
     * Receive arbitrary information in a slow rate
     *
     * @see DebugData
     */
    public void recv_debug(DebugData d);

}
