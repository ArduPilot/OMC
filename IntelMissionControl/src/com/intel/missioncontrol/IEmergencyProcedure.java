/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import eu.mavinci.core.plane.AirplaneEventActions;

//TODO: provide implementation for this interface based on the chosen actions.
public interface IEmergencyProcedure {
    public String getName();
}

/*
static class CircleDownProcedure implements IEmergencyProcedure {
    /** Maximum descent speed */
/*    double getMaximumDescentSpeed() { return 0; }
}

static class ReturnToLandingProcedure implements IEmergencyProcedure {
    double getHomePosition();
}

    void foo() {
        // somewhere
        IEmergencyProcedure procedure;
        if (procedure instanceof ReturnToLandingProcedure) {
            ReturnToLandingProcedure rtl = (ReturnToLandingProcedure) procedure;

            ((ReturnToLandingProcedure) procedure).getHomePosition();

        }
    }
*/