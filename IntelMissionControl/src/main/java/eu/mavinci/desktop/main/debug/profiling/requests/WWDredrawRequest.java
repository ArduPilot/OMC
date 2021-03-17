/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling.requests;

import eu.mavinci.desktop.main.debug.profiling.MRequest;
import gov.nasa.worldwind.WorldWindow;

public class WWDredrawRequest extends MRequest {

    WorldWindow wwd;

    public WWDredrawRequest(WorldWindow wwd) {
        super(100L, 50);
        this.wwd = wwd;
    }

    @Override
    public String toString() {
        return super.toString() + "\nWWDredrawRequest\n" + wwd.toString();
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
