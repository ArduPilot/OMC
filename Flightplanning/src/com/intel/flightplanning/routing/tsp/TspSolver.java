/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.flightplanning.routing.tsp;


public abstract class TspSolver {

    public abstract double solve(TspPath<?> path);

    public abstract double solve();

    public abstract TspSolver setPath();

    public abstract TspPath<?> getPath();
}
