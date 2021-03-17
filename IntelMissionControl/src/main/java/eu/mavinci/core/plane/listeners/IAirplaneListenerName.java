/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerName extends IAirplaneListener {

    /**
     * Receive new Name of Airplane
     *
     * @param name
     */
    public void recv_nameChange(String name);

}
