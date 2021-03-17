/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerIsSimulation extends IAirplaneListener {

    /** is it a simulation? */
    public void recv_isSimulation(Boolean simulation);

}
