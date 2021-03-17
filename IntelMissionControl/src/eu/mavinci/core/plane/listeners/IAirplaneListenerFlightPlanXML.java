/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerFlightPlanXML extends IAirplaneListener {

    /** new XML Flightplan sending has taken place */
    public void recv_setFlightPlanXML(String plan, Boolean reentry, Boolean succeed);

}
