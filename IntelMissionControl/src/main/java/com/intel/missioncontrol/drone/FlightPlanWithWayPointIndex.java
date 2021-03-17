/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone;

import com.intel.missioncontrol.mission.FlightPlan;

public class FlightPlanWithWayPointIndex {
    private final FlightPlan flightPlan;
    private final int wayPointIndex;

    public FlightPlanWithWayPointIndex(FlightPlan flightPlan, int wayPointIndex) {
        this.flightPlan = flightPlan;
        this.wayPointIndex = wayPointIndex;
    }

    public int getWayPointIndex() {
        return wayPointIndex;
    }

    public FlightPlan getFlightPlan() {
        return flightPlan;
    }
}
