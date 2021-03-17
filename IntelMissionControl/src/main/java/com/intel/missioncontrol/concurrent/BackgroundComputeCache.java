/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import com.intel.missioncontrol.SuppressLinter;
import java.util.HashMap;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.StrandGroup;

/**
 * A non volatile cache for computation results. For each input it looks up if there is already a cached result for it.
 * if not it will compute the result in a background thread. the result will be provided in each case as listenable
 * future
 *
 * <p>Executes code in a fixed number of parallel strands. In contrast to using {@link Dispatcher} directly, this
 * provides way to control the amount of parallelism.
 *
 * @param <T> type of computation result
 * @param <V> type of computation input parameter
 */
public class BackgroundComputeCache<T, V> {

    public interface ITransform<T, V> {
        T transform(V input);
    }

    private final ITransform<T, V> transformer;
    private final StrandGroup strandGroup;
    private final HashMap<V, Future<T>> results = new HashMap<>();
    private final Object syncToken = new Object();

    public BackgroundComputeCache(int maxParallelExecutors, ITransform<T, V> transformer) {
        this.transformer = transformer;
        strandGroup = new StrandGroup(maxParallelExecutors);
    }

    public void setIdentical(V from, V to) {
        synchronized (syncToken) {
            results.put(to, results.get(from));
        }
    }

    @SuppressLinter(value = "AsyncMethodNaming", reviewer = "mstrauss", justification = "okay here")
    public Future<T> get(V input) {
        synchronized (syncToken) {
            Future<T> future = results.get(input);
            if (future == null) {
                future = strandGroup.getLater(() -> transformer.transform(input));
                results.put(input, future);
            }

            return future;
        }
    }
}
