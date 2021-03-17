/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;
import eu.mavinci.core.plane.sendableobjects.PositionOrientationData;

public interface IAirplaneListenerPositionOrientation extends IAirplaneListener {

    /**
     * Receive positon and orientation information.
     *
     * @see PositionOrientationData
     */
    public void recv_positionOrientation(PositionOrientationData po);

}
