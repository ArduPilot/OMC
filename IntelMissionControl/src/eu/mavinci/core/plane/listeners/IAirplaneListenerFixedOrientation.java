/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerFixedOrientation extends IAirplaneListener {

    /**
     * receive the fixed setted orientation
     *
     * @param roll
     * @param pitch
     * @param yaw
     */
    public void recv_fixedOrientation(Float roll, Float pitch, Float yaw);

}
