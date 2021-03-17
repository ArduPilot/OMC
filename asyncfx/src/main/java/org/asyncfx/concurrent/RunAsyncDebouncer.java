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
public class RunAsyncDebouncer {

    private final Future.FutureRunnable futureRunnable;
    private final Strand strand;
    private boolean mayReturnRunningFuture;
    private List<FutureCompletionSource<Void>> futureCompletionSources;
    private Future<Void> future;

    public RunAsyncDebouncer(Future.FutureRunnable futureRunnable) {
        this(futureRunnable, true);
    }

    public RunAsyncDebouncer(Future.FutureRunnable futureRunnable, boolean mayReturnRunningFuture) {
        this(futureRunnable, null, mayReturnRunningFuture);
    }

    public RunAsyncDebouncer(Future.FutureRunnable futureRunnable, Strand strand) {
        this(futureRunnable, strand, true);
    }

    public RunAsyncDebouncer(Future.FutureRunnable futureRunnable, Strand strand, boolean mayReturnRunningFuture) {
        this.futureRunnable = futureRunnable;
        this.mayReturnRunningFuture = mayReturnRunningFuture;
        this.strand = strand;
    }

    public synchronized Future<Void> runAsync() {
        if (future == null) {
            future = strand != null ? strand.runLaterAsync(futureRunnable) : futureRunnable.run();
            Future<Void> future = this.future;
            future.whenDone(this::onCurrentFutureCompleted);
            return future;
        }

        if (mayReturnRunningFuture) {
            return future;
        }

        if (futureCompletionSources == null) {
            futureCompletionSources = new ArrayList<>(3);
        }

        FutureCompletionSource<Void> futureCompletionSource = new FutureCompletionSource<>();
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

    protected void onCompletedSuccessfully() {}

    private synchronized void onCurrentFutureCompleted(Future<Void> future) {
        if (futureCompletionSources != null) {
            boolean allContinuationsCancelled = true;
            for (FutureCompletionSource<Void> futureCompletionSource : futureCompletionSources) {
                if (!futureCompletionSource.getFuture().isCancelled()) {
                    allContinuationsCancelled = false;
                    break;
                }
            }

            if (!allContinuationsCancelled) {
                final List<FutureCompletionSource<Void>> futureCompletionSources = this.futureCompletionSources;
                this.future = strand != null ? strand.runLaterAsync(futureRunnable) : futureRunnable.run();
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

        if (future.isSuccess()) {
            onCompletedSuccessfully();
        }
    }

    private synchronized void onNextFutureCompleted(
            Future<Void> future, List<FutureCompletionSource<Void>> futureCompletionSources) {
        if (future.isSuccess()) {
            for (FutureCompletionSource<Void> futureCompletionSource : futureCompletionSources) {
                futureCompletionSource.setResult(null);
            }
        } else if (future.isFailed()) {
            for (FutureCompletionSource<Void> futureCompletionSource : futureCompletionSources) {
                futureCompletionSource.setException(future.getException());
            }
        } else {
            for (FutureCompletionSource<Void> futureCompletionSource : futureCompletionSources) {
                futureCompletionSource.setCancelled(future.getException());
            }
        }
    }

}
