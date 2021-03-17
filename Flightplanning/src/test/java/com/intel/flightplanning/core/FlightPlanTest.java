/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

import org.junit.jupiter.api.Test;


public class FlightPlanTest {

    @Test
    public void initTest() {
        var fp = new FlightPlan();
    }

    @Test
    public void initExceptionTest() {
        var fp = new FlightPlan(null);
    }
}
