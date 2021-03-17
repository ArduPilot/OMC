/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.plane.dronekit;

import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.plane.IAirplane;

public interface MavLinkConnector {
    ConnectionManager getConnectionManager();

    public void onNewAirplaneRegister(IAirplane airplane);
    public void onMissionChanged(Mission mission);
}
