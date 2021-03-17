/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

import org.junit.Test;

public class CorridorGoalTest {

    @Test
    public void initTest() {
        var gb = new CorridorGoal.Builder();
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

    @Test
    public void initTesteException() {
        var gb = new CorridorGoal.Builder();
        gb.setCorridorMinLines(1)
            .setCorridorWidthInMeter(10f)
            .setCropHeightMax(0f)
            .setCropHeightMin(0f)
            .setMaxObjectDistance(1);
        var g = gb.createGoal();
    }

}
