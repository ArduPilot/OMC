/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.rasterizer.processors;

import com.intel.flightplanning.core.FlightPlan;
import com.intel.flightplanning.core.annotations.NeedsRework;

public class AddStartingPoint {
    @NeedsRework
    FlightPlan processFlightPlan(FlightPlan inputFlightPlan) {
        return inputFlightPlan;
    }
}
