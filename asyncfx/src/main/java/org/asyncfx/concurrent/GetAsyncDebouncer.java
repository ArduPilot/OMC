/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.ArrayList;
import java.util.List;

/**
 * Debounces invocations of async operations such that only a single operation is running at any given time. If the
 * operation is invoked while another instance of the operation is already running, the call either returns the
 * currently running operation (if {@see mayReturnRunningFuture} is true}, or returns an operation that will be started
 * once the currently running operation has completed (if {@see mayReturnRunningFuture} is false).
 */
public class GetAsyncDebouncer<V> {

    private final Future.FutureSupplier<V> futureSupplier;
    private final Strand strand;
    private boolean mayReturnRunningFuture;
    private List<FutureCompletionSource<V>> futureCompletionSources;
    private Future<V> future;

    public GetAsyncDebouncer(Future.FutureSupplier<V> futureSupplier) {
        this(futureSupplier, true);
    }

    public GetAsyncDebouncer(Future.FutureSupplier<V> futureSupplier, boolean mayReturnRunningFuture) {
        this(futureSupplier, null, mayReturnRunningFuture);
    }

    public GetAsyncDebouncer(Future.FutureSupplier<V> futureSupplier, Strand strand) {
        this(futureSupplier, strand, true);
    }

    public GetAsyncDebouncer(Future.FutureSupplier<V> futureSupplier, Strand strand, boolean mayReturnRunningFuture) {
        this.futureSupplier = futureSupplier;
        this.mayReturnRunningFuture = mayReturnRunningFuture;
        this.strand = strand;
    }

    public synchronized Future<V> getAsync() {
        if (future == null) {
            future = strand != null ? strand.getLaterAsync(futureSupplier) : futureSupplier.get();
            Future<V> future = this.future;
            future.whenDone(this::onCurrentFutureCompleted);
            return future;
        }

        if (mayReturnRunningFuture) {
            return future;
        }

        if (futureCompletionSources == null) {
            futureCompletionSources = new ArrayList<>(3);
        }

        FutureCompletionSource<V> futureCompletionSource = new FutureCompletionSource<>();
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

    private synchronized void onCurrentFutureCompleted(Future<V> future) {
        if (futureCompletionSources != null) {
            boolean allContinuationsCancelled = true;
            for (FutureCompletionSource<V> futureCompletionSource : futureCompletionSources) {
                if (!futureCompletionSource.getFuture().isCancelled()) {
                    allContinuationsCancelled = false;
                    break;
                }
            }

            if (!allContinuationsCancelled) {
                final List<FutureCompletionSource<V>> futureCompletionSources = this.futureCompletionSources;
                this.future = strand != null ? strand.getLaterAsync(futureSupplier) : futureSupplier.get();
                this.future.whenDone(
                    f -> {
                        onNextFutureCompleted(f, futureCompletionSources);
                        onCurrentFutureCompleted(f);
                    });
            }

            this.futureCompletionSources = null;
        } else {
            this.future = null;
        }
    }

    private synchronized void onNextFutureCompleted(
            Future<V> future, List<FutureCompletionSource<V>> futureCompletionSources) {
        if (future.isSuccess()) {
            for (FutureCompletionSource<V> futureCompletionSource : futureCompletionSources) {
                futureCompletionSource.setResult(future.getUnchecked());
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

}
