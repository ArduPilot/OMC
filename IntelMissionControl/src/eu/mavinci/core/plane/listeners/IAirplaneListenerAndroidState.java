/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.AndroidState;
import eu.mavinci.core.plane.sendableobjects.AndroidState;

public interface IAirplaneListenerAndroidState extends IAirplaneListener {

    /** Receive state information from the Backup Pilot Support Device */
    public void recv_androidState(AndroidState state);

}
