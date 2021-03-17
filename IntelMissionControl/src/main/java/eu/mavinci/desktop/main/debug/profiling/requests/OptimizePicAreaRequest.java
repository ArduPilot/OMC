/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling.requests;

import com.intel.missioncontrol.helper.Ensure;
import eu.mavinci.desktop.main.debug.profiling.MRequest;
import eu.mavinci.flightplan.Flightplan;
import eu.mavinci.flightplan.PicArea;
import eu.mavinci.flightplan.PicArea;

public class OptimizePicAreaRequest extends MRequest {

    PicArea picArea;

    public OptimizePicAreaRequest(PicArea picArea) {
        super(1000L, 3);
        this.picArea = picArea;
    }

    @Override
    public String toString() {
        Flightplan flightplan = picArea.getFlightplan();
        Ensure.notNull(flightplan, "flightplan");
        return super.toString() + "\nOptimizePicAreaRequest\n" + flightplan.toString();
    }

    static long slowest = 0;
    static long noSampled = 0;

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
