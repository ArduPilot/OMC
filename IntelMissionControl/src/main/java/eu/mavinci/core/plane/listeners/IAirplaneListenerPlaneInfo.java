/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.PlaneInfo;
import eu.mavinci.core.plane.sendableobjects.PlaneInfo;

public interface IAirplaneListenerPlaneInfo extends IAirplaneListener {

    /** Receive version infos about the plane */
    public void recv_planeInfo(PlaneInfo info);

}
