/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.ArrayList;
import java.util.List;

/**
 * Debounces requests to apply a value asynchronously such that only a single value is applied at any given time.
 *
 * <ul>
 *   <li>If a request is made while a value is currently being applied, the request is recorded. When the earlier
 *       request completes, the next value will be applied.
 *   <li>If multiple requests are made while a value is currently being applied, all requests will be recorded. When the
 *       earlier request completes, the most recent request that is not yet cancelled will be applied. ALl other
 *       requests will be cancelled.
 * </ul>
 *
 * {@link ApplyAsyncDebouncer#applyAsync(Object)} returns a future that reflects the status of the request. If the
 * request was superseded by a later request, the future will be cancelled.
 */
public class ApplyAsyncDebouncer<T, R> {

    private final Future.FutureFunction<T, R> futureFunction;
    private List<FutureCompletionSource<T, R>> futureCompletionSources;
    private Future<R> future;

    public ApplyAsyncDebouncer(Future.FutureFunction<T, R> futureFunction) {
        this.futureFunction = futureFunction;
    }

    public synchronized Future<R> applyAsync(T value) {
        if (future == null) {
            future = futureFunction.apply(value);
            Future<R> future = this.future;
            future.whenDone(this::onCurrentFutureCompleted);
            return future;
        }

        if (futureCompletionSources == null) {
            futureCompletionSources = new ArrayList<>(3);
        }

        FutureCompletionSource<T, R> futureCompletionSource = new FutureCompletionSource<>();
        futureCompletionSource.value = value;
        futureCompletionSources.add(futureCompletionSource);
        return futureCompletionSource.getFuture();
    }

    public boolean cancel() {
        return cancel(false);
    }

    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (future != null) {
            return future.cancel(mayInterruptIfRunning);
        }

        return false;
    }

    private synchronized void onCurrentFutureCompleted(Future<R> future) {
        if (futureCompletionSources != null) {
            boolean cancelRest = false;

            for (int i = futureCompletionSources.size() - 1; i >= 0; --i) {
                if (cancelRest) {
                    futureCompletionSources.get(i).setCancelled();
                } else if (!futureCompletionSources.get(i).getFuture().isCancelled()) {
                    final List<FutureCompletionSource<T, R>> futureCompletionSources = this.futureCompletionSources;
                    this.future = futureFunction.apply(futureCompletionSources.get(i).value);
                    this.future.whenDone(
                        f -> {
                            onNextFutureCompleted(f, futureCompletionSources);
                            onCurrentFutureCompleted(f);
                        });

                    cancelRest = true;
                }
            }

            futureCompletionSources = null;
        } else {
            this.future = null;
        }
    }

    private synchronized void onNextFutureCompleted(
            Future<R> future, List<FutureCompletionSource<T, R>> futureCompletionSources) {
        if (future.isSuccess()) {
            for (FutureCompletionSource<T, R> futureCompletionSource : futureCompletionSources) {
                futureCompletionSource.setResult(future.getUnchecked());
            }
        } else if (future.isFailed()) {
            for (FutureCompletionSource<T, R> futureCompletionSource : futureCompletionSources) {
                futureCompletionSource.setException(future.getException());
            }
        } else {
            for (FutureCompletionSource<T, R> futureCompletionSource : futureCompletionSources) {
                futureCompletionSource.setCancelled(future.getException());
            }
        }
    }

    private static class FutureCompletionSource<T, R> extends org.asyncfx.concurrent.FutureCompletionSource<R> {
        T value;
    }

}
