/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;

class RetryScheduler<TRes> {
    private final int repetitions;
    private final CancellationSource externalCancellationSource;
    private final Duration timeout;
    private FutureCompletionSource<TRes> futureCompletionSource;
    private CancellationSource cancellationSource;

    RetryScheduler(int repetitions, CancellationSource externalCancellationSource, Duration timeout) {
        this.repetitions = repetitions;
        this.externalCancellationSource = externalCancellationSource;
        this.timeout = timeout;
    }

    /**
     * Calls the given async callback fnc. If it succeeds, shouldRetry is called, and if it returns true, fnc is called
     * again after a delay given by timeout. This is repeated for a given maximum number of repetitions. In case of
     * failure no retries are attempted.
     *
     * @param fncAsync Callback receiving the current retry count, starting at 0, up to repetitions-1.
     * @param shouldRetry a callback, invoked after each fnc execution, indicating if a retry is needed.
     * @return A future indicating eventual success or failure (only after all repetitions are exhausted or any
     *     exceptions are thrown).
     */
    Future<TRes> runWithRetriesAsync(Function<Integer, Future<TRes>> fncAsync, Function<TRes, Boolean> shouldRetry) {
        if (futureCompletionSource != null && !futureCompletionSource.getFuture().isDone()) {
            throw new IllegalStateException("already scheduled");
        }

        futureCompletionSource = new FutureCompletionSource<>(externalCancellationSource);

        // Cancel retry scheduler when done
        cancellationSource = new CancellationSource();
        futureCompletionSource.getFuture().whenDone(tRes -> cancellationSource.cancel());

        runWithRetriesImpl(fncAsync, shouldRetry, 0);

        return futureCompletionSource.getFuture();
    }

    private void runWithRetriesImpl(
            Function<Integer, Future<TRes>> fncAsync, Function<TRes, Boolean> shouldRetry, int repetition) {
        if (repetition >= repetitions) {
            futureCompletionSource.setException(new TimeoutException("Max number of repetitions exceeded"));
            return;
        }

        try {
            fncAsync.apply(repetition)
                .whenSucceeded(
                    tRes -> {
                        try {
                            if (!shouldRetry.apply(tRes)) {
                                futureCompletionSource.setResult(tRes);
                            } else {
                                // Retry required
                                Dispatcher dispatcher = Dispatcher.background();
                                dispatcher.runLaterAsync(
                                    c -> runWithRetriesImpl(fncAsync, shouldRetry, repetition + 1),
                                    timeout,
                                    cancellationSource);
                            }
                        } catch (Exception e) {
                            futureCompletionSource.setException(e);
                        }
                    })
                .whenFailed(e -> futureCompletionSource.setException(e.getCause()));
        } catch (Exception e) {
            futureCompletionSource.setException(e);
        }
    }
}
