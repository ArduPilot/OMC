/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerPhotoLogName extends IAirplaneListener {

    /**
     * Receive file name of currently closed plg file
     *
     * @param name
     */
    public void recv_newPhotoLog(String name, Integer bytes);

}
