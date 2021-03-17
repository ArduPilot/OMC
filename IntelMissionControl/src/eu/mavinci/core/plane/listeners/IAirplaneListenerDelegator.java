/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

/** */
package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.listeners.IListener;
import eu.mavinci.core.listeners.IListenerManager;
import eu.mavinci.core.plane.protocol.IInvokeable;
import eu.mavinci.core.listeners.IListener;
import eu.mavinci.core.listeners.IListenerManager;

public interface IAirplaneListenerDelegator extends IAirplaneListenerAll, IListenerManager, IInvokeable, ICommandListenerResult {

    public void addListenerAtSecond(IListener l);

    @Deprecated
    public void recv_startPos(Double lon, Double lat);

}
