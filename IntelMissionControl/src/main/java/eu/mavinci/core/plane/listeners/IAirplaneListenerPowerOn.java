/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerPowerOn extends IAirplaneListener {

    /** receive power on Notification from airplane */
    public void recv_powerOn();

}
