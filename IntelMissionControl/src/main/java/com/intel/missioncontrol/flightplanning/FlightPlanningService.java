/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.flightplanning;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.geometry.Vec4;
import com.intel.missioncontrol.project.FlightPlan;
import com.intel.missioncontrol.project.FlightPlanSnapshot;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;

public class FlightPlanningService implements IFlightPlanningService {

    private final IApplicationContext applicationContext;

    @Inject
    public FlightPlanningService(IApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Future<Void> calculateFlightPlan() {
        FlightPlanSnapshot flightPlanSnapshot =
            new FlightPlanSnapshot(
                UUID.randomUUID(),
                "Flight plan",
                OffsetDateTime.now(),
                null,
                new Vec4(0, 0, 0, 0),
                new Vec4(0, 0, 0, 0),
                0.0,
                0.0,
                0.0,
                true,
                true,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                new ArrayList<>());
        List<FlightPlan> flightPlans = applicationContext.currentMissionProperty().get().flightPlansProperty();
        flightPlans.clear();
        flightPlans.add(new FlightPlan(flightPlanSnapshot));
        return Futures.successful();
    }
}
