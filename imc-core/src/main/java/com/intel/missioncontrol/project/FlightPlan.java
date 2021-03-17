/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project;

import com.intel.missioncontrol.serialization.CompositeDeserializationContext;

public class FlightPlan extends AbstractFlightPlan {

    public FlightPlan() {
        super();
    }

    public FlightPlan(FlightPlan source) {
        super(source);
    }

    public FlightPlan(FlightPlanSnapshot source) {
        super(source);
    }

    public FlightPlan(CompositeDeserializationContext context) {
        super(context);
    }

}
