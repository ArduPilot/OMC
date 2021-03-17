/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core.rasterizer.processors;

import com.intel.flightplanning.core.FlightPlan;
import com.intel.flightplanning.core.annotations.NeedsRework;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FlightPlanSplitter {
    @NeedsRework
    List<FlightPlan> processFlightPlan(FlightPlan inputFlightPlan) {
        return new ArrayList<FlightPlan>((Collection<? extends FlightPlan>) inputFlightPlan);
    }
}
