/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling.requests;

import com.intel.missioncontrol.mission.Mission;
import eu.mavinci.desktop.main.debug.profiling.MRequest;

public class OpenMissionRequest extends MRequest {
    static long slowest = 0;
    static long noSampled = 0;
    private final Mission mission;

    public OpenMissionRequest(Mission m) {
        super(100, 3);
        this.mission = m;
    }

    @Override
    public String toString() {
        return "Open mission profile request" + ": " + mission.getName();
    }

    @Override
    public synchronized boolean isSlowestUpToNow(long duration) {
        if (duration > slowest) {
            slowest = duration;
            return true;
        }

        return false;
    }

    public synchronized void sampleThis() {
        noSampled++;
    }

    @Override
    public long getCountUpToNow() {
        return noSampled;
    }
}
