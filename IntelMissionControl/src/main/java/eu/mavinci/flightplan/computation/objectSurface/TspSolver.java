/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.flightplan.computation.objectSurface;

import com.intel.missioncontrol.utils.IBackgroundTaskManager;

public abstract class TspSolver {

    public abstract double solve(TspPath<?> path);

    public abstract double solve(IBackgroundTaskManager.BackgroundTask task);

    public abstract TspSolver setPath();

    public abstract TspPath<?> getPath();
}
