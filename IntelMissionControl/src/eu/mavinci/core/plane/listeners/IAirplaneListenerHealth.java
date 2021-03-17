/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.HealthData;
import eu.mavinci.core.plane.sendableobjects.HealthData;

public interface IAirplaneListenerHealth extends IAirplaneListener {

    /** receive health data structure */
    public void recv_health(HealthData d);

}
