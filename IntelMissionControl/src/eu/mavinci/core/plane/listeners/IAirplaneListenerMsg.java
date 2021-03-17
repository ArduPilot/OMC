/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.AirplaneMsgType;

public interface IAirplaneListenerMsg extends IAirplaneListener {

    /**
     * Receive a msg string. The message string is a message from air and should be displayed on screen.
     *
     * @param lvl
     * @see AirplaneMsgType
     */
    public void recv_msg(Integer lvl, String data);

}
