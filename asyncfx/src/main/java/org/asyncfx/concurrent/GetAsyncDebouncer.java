/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.ArrayList;
import java.util.List;

public class GetAsyncDebouncer<V> {

    private final Future.FutureSupplier<V> futureSupplier;
    private boolean mayReturnRunningFuture;
    private List<FutureCompletionSource<V>> futureCompletionSources;
    private Future<V> future;

    public GetAsyncDebouncer(Future.FutureSupplier<V> futureSupplier) {
        this(futureSupplier, true);
    }

    public GetAsyncDebouncer(Future.FutureSupplier<V> futureSupplier, boolean mayReturnRunningFuture) {
        this.futureSupplier = futureSupplier;
        this.mayReturnRunningFuture = mayReturnRunningFuture;
    }

    public synchronized Future<V> getAsync() {
        if (future == null) {
            future = futureSupplier.get();
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
                this.future = futureSupplier.get();
                this.future.whenDone(f -> onNextFutureCompleted(f, futureCompletionSources));
                this.future.whenDone(this::onCurrentFutureCompleted);
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
