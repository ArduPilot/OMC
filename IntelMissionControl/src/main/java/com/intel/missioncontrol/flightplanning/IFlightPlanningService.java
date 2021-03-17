/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.flightplanning;

import org.asyncfx.concurrent.Future;

public interface IFlightPlanningService {
    Future<Void> calculateFlightPlan();
}
