/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerStartPos extends IAirplaneListener {

    /**
     * start position = bounding box center / return home pos
     *
     * @param pressureZero 0-> non baro reset (moving of the connector / bounding box center) 1-> baro reset (refernce
     *     position where all altitudes are refernced to. only once after power on)
     */
    public void recv_startPos(Double lon, Double lat, Integer pressureZero);

}
