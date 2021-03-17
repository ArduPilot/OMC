/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.SimulationSettings;
import eu.mavinci.core.plane.sendableobjects.SimulationSettings;

public interface IAirplaneListenerSimulationSettings extends IAirplaneListener {
    /**
     * current speed of the simulation. speed > 1 => fast| speed <1 slow | speed ==-1 => as faast as possible!
     *
     * @param speed
     */
    public void recv_simulationSpeed(Float speed);

    public void recv_simulationSettings(SimulationSettings settings);

}
