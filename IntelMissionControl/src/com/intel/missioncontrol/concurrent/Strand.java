/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import java.util.concurrent.Callable;

/**
 * Represents a serialized stream of operations. Operations posted through this strand are guaranteed to not run
 * concurrently. They will be executed serially in the order they were posted.
 */
public final class Strand {

    private final Object syncToken = new Object();
    private FluentFuture<?> currentFuture;
    private int count;

    public FluentFuture<Void> post(Runnable runnable) {
        synchronized (syncToken) {
            FluentFuture<Void> future;

            Runnable func =
                () -> {
                    runnable.run();

                    synchronized (syncToken) {
                        --count;
                    }
                };

            if (currentFuture != null) {
                ++count;
                future = currentFuture.continueWith(f -> Dispatcher.post(func));
            } else {
                count = 1;
                future = FluentFuture.from(Dispatcher.post(func));
            }

            currentFuture = future;
            return future;
        }
    }

    public <T> FluentFuture<T> post(Callable<T> callable) {
        synchronized (syncToken) {
            FluentFuture<T> future;

            Callable<T> func =
                () -> {
                    T result = callable.call();

                    synchronized (syncToken) {
                        --count;
                    }

                    return result;
                };

            if (currentFuture != null) {
                ++count;
                future = currentFuture.continueWith(f -> Dispatcher.post(func));
            } else {
                count = 1;
                future = FluentFuture.from(Dispatcher.post(func));
            }

            currentFuture = future;
            return future;
        }
    }

    int getScheduledCount() {
        return count;
    }

}
