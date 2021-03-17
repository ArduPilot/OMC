/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling.requests;

import eu.mavinci.core.plane.ICAirplane;
import eu.mavinci.desktop.main.debug.profiling.MRequest;
import eu.mavinci.core.plane.ICAirplane;

public class LaunchSessionRequest extends MRequest {
    final ICAirplane plane;

    public LaunchSessionRequest(ICAirplane plane) {
        super(5000L, 3);
        this.plane = plane;
    }

    @Override
    public String toString() {
        return super.toString() + "\nLaunchSessionRequest{plane=" + plane + "}\n";
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
