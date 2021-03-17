/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.PositionData;
import eu.mavinci.core.plane.sendableobjects.PositionData;

public interface IAirplaneListenerPosition extends IAirplaneListener {

    /**
     * Receive positon information. Position data contains 3D position and some other information (flightphase,
     * manual/automatic control, on board temperature, ground speed, ...)
     *
     * @see PositionData
     */
    public void recv_position(PositionData p);

}
