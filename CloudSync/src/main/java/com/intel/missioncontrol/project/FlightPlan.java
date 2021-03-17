/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.project.serialization.DeserializationContext;

public class FlightPlan extends AbstractFlightPlan {

    public FlightPlan() {
        super();
    }

    public FlightPlan(IFlightPlan source) {
        super(source);
    }

    public FlightPlan(DeserializationContext context) {
        super(context);
    }

}
