/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection;

import com.intel.missioncontrol.drone.IDrone;

@FunctionalInterface
public interface IDroneMessageListener {
    void onDroneMessage(IDrone sender, DroneMessage message);
}
