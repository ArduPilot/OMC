/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerFlightPlanASM extends IAirplaneListener {
    /** new ASM Flightplan sending has taken place */
    public void recv_setFlightPlanASM(String plan, Boolean reentry, Boolean succeed);

}
