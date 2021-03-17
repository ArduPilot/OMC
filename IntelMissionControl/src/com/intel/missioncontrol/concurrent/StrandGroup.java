/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import java.util.concurrent.Callable;

/**
 * Executes code in a fixed number of parallel strands. In contrast to using {@link Dispatcher} directly, this provides
 * a way to control the amount of parallelism.
 */
public class StrandGroup {

    private final Strand[] strands;

    public StrandGroup(int size) {
        strands = new Strand[size];
        for (int i = 0; i < size; ++i) {
            strands[i] = new Strand();
        }
    }

    public FluentFuture<Void> post(Runnable runnable) {
        return getLeastBusyStrand().post(runnable);
    }

    public <T> FluentFuture<T> post(Callable<T> callable) {
        return getLeastBusyStrand().post(callable);
    }

    private Strand getLeastBusyStrand() {
        synchronized (strands) {
            int bestStrand = 0;
            int lowestCount = Integer.MAX_VALUE;

            for (int i = 0; i < strands.length; ++i) {
                if (strands[i].getScheduledCount() < lowestCount) {
                    lowestCount = strands[i].getScheduledCount();
                    bestStrand = i;
                }
            }

            return strands[bestStrand];
        }
    }

}
