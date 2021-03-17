/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.intel.missioncontrol.drone.DroneConnectionException;
import com.intel.missioncontrol.drone.IDrone;

@FunctionalInterface
public interface IDroneConnectionExceptionListener {
    void onDroneConnectionException(IDrone sender, DroneConnectionException e);
}
