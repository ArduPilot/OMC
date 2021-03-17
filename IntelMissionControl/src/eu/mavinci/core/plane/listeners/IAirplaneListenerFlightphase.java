/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerFlightphase extends IAirplaneListener {
    /** flight Phase */
    public void recv_flightPhase(Integer fp);

}
