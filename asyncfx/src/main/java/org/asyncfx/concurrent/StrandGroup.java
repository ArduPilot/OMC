/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.function.Supplier;

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

    public Future<Void> runLater(Runnable runnable) {
        return getLeastBusyStrand().runLater(runnable);
    }

    public <T> Future<T> getLater(Supplier<T> supplier) {
        return getLeastBusyStrand().getLater(supplier);
    }

    public Future<Void> getLaterAsync(Future.FutureRunnable futureRunnable) {
        return getLeastBusyStrand().runLaterAsync(futureRunnable);
    }

    public <T> Future<T> getLaterAsync(Future.FutureSupplier<T> futureSupplier) {
        return getLeastBusyStrand().getLaterAsync(futureSupplier);
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
