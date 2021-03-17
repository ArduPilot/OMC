/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import java.net.InetSocketAddress;

public interface IAirplaneListenerSendingHost {

    /**
     * set the host (resp. IP-Address) where the next recv_backend(...) was reveiced from
     *
     * @param host
     */
    public void setSendHostOfNextReceive(InetSocketAddress host);
}
