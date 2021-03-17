/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerConnectionEstablished extends IAirplaneListener {

    /** This is thrown, after the TCP connection to a dediacted port is establised with backend */
    public void recv_connectionEstablished(String port);
}
