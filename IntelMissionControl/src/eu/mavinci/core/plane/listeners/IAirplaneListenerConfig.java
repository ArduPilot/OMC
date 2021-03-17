/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

import eu.mavinci.core.plane.sendableobjects.Config_variables;
import eu.mavinci.core.plane.sendableobjects.Config_variables;

public interface IAirplaneListenerConfig extends IAirplaneListener {

    /**
     * Receive configuration data. Config data contains all configuration options, such as controller parameters.
     *
     * @see Config_variables
     */
    public void recv_config(Config_variables c);

}
