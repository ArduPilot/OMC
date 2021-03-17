/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling.requests;

import eu.mavinci.desktop.main.debug.profiling.MRequest;

import java.io.File;

public class AirspaceUnLoadRequest extends MRequest {

    File file;

    public AirspaceUnLoadRequest(File file) {
        super(100L, 3);
        this.file = file;
    }

    @Override
    public String toString() {
        return super.toString() + "\nAirspaceUnLoadRequest:" + file + "\n";
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
