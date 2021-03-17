/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling.requests;

import eu.mavinci.desktop.main.debug.profiling.MRequest;

public class MapVisChangeRequest extends MRequest {
    public MapVisChangeRequest() {
        super(50L, 20);
    }

    @Override
    public String toString() {
        return super.toString() + "\nMapVisChangeRequest\n";
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
