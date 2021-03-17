/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.OrientationData;
import eu.mavinci.core.plane.sendableobjects.OrientationData;

public interface IAirplaneListenerOrientation extends IAirplaneListener {

    /**
     * Receive orientation information. Orientation data contains roll, pitch and yaw angles along with some other
     * information.
     *
     * @see OrientationData
     */
    public void recv_orientation(OrientationData o);

}
