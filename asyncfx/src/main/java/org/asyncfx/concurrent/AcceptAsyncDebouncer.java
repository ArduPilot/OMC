/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.ArrayList;
import java.util.List;

/**
 * Debounces requests to accept a value asynchronously such that only a single value is accepted at any given time.
 *
 * <ul>
 *   <li>If a request is made while a value is currently being accepted, the request is recorded. When the earlier
 *       request completes, the next value will be accepted.
 *   <li>If multiple requests are made while a value is currently being accepted, all requests will be recorded. When
 *       the earlier request completes, the most recent request that is not yet cancelled will be accepted. ALl other
 *       requests will be cancelled.
 * </ul>
 *
 * {@link AcceptAsyncDebouncer#acceptAsync(Object)} returns a future that reflects the status of the request. If the
 * request was superseded by a later request, the future will be cancelled.
 */
public class AcceptAsyncDebouncer<V> {

    private final Future.FutureConsumer<V> futureConsumer;
    private List<FutureCompletionSource<V>> futureCompletionSources;
    private Future<Void> future;

    public AcceptAsyncDebouncer(Future.FutureConsumer<V> futureConsumer) {
        this.futureConsumer = futureConsumer;
    }

    public synchronized Future<Void> acceptAsync(V value) {
        if (future == null) {
            future = futureConsumer.accept(value);
            Future<Void> future = this.future;
            future.whenDone(this::onCurrentFutureCompleted);
            return future;
        }

        if (futureCompletionSources == null) {
            futureCompletionSources = new ArrayList<>(3);
        }

        FutureCompletionSource<V> futureCompletionSource = new FutureCompletionSource<>();
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

    private synchronized void onCurrentFutureCompleted(Future<Void> future) {
        if (futureCompletionSources != null) {
            boolean cancelRest = false;

            for (int i = futureCompletionSources.size() - 1; i >= 0; --i) {
                if (cancelRest) {
                    futureCompletionSources.get(i).setCancelled();
                } else if (!futureCompletionSources.get(i).getFuture().isCancelled()) {
                    final List<FutureCompletionSource<V>> futureCompletionSources = this.futureCompletionSources;
                    this.future = futureConsumer.accept(futureCompletionSources.get(i).value);
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
            Future<Void> future, List<FutureCompletionSource<V>> futureCompletionSources) {
        if (future.isSuccess()) {
            for (FutureCompletionSource<V> futureCompletionSource : futureCompletionSources) {
                futureCompletionSource.setResult(null);
            }
        } else if (future.isFailed()) {
            for (FutureCompletionSource<V> futureCompletionSource : futureCompletionSources) {
                futureCompletionSource.setException(future.getException());
            }
        } else {
            for (FutureCompletionSource<V> futureCompletionSource : futureCompletionSources) {
                futureCompletionSource.setCancelled(future.getException());
            }
        }
    }

    private static class FutureCompletionSource<V> extends org.asyncfx.concurrent.FutureCompletionSource<Void> {
        V value;
    }

}
