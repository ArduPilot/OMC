/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.main.debug.profiling.requests;

import eu.mavinci.core.flightplan.CFlightplan;
import eu.mavinci.desktop.main.debug.profiling.MRequest;
import eu.mavinci.core.flightplan.CFlightplan;

public class SendFpRequest extends MRequest {

    CFlightplan fp;

    public SendFpRequest(CFlightplan fp) {
        super(1000L, 3);
        this.fp = fp;
    }

    @Override
    public String toString() {
        return super.toString() + "\nSendFpRequest\n" + fp.toString();
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
