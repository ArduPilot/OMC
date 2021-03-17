/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.core;

import static org.junit.Assert.*;

import org.junit.Test;

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