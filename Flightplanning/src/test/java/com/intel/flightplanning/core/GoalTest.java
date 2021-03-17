/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

import org.junit.jupiter.api.Test;

public class GoalTest {

    @Test
    public void testExceptionInit() {
        var g = new Goal.Builder().setCropHeightMax(0f).createGoal();
    }

    @Test
    public void testInit() {
        var gb = new Goal.Builder();
        gb.setCropHeightMax(0f)
            .setMaxObjectDistance(100f)
            .setCropHeightMin(0f)
            .setCropHeightMaxEnabled(0f)
            .setCropHeightMinEnabled(0f)
            .setMinGroundDistance(1f)
            .setMinObjectDistance(1f)
            .setOverlapInFlight(60f)
            .setOverlapInFlightMin(40f)
            .setOverlapParallel(60f)
            .setTargetAlt(10f)
            .setTargetGSD(0.1f)
            .setTargetDistance(1f);
        var g = gb.createGoal();
    }

}
