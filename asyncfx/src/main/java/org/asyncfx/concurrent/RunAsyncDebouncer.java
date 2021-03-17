/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.ArrayList;
import java.util.List;

public class RunAsyncDebouncer {

    private final Future.FutureRunnable futureRunnable;
    private boolean mayReturnRunningFuture;
    private List<FutureCompletionSource<Void>> futureCompletionSources;
    private Future<Void> future;

    public RunAsyncDebouncer(Future.FutureRunnable futureRunnable) {
        this(futureRunnable, true);
    }

    public RunAsyncDebouncer(Future.FutureRunnable futureRunnable, boolean mayReturnRunningFuture) {
        this.futureRunnable = futureRunnable;
        this.mayReturnRunningFuture = mayReturnRunningFuture;
    }

    public synchronized Future<Void> runAsync() {
        if (future == null) {
            future = futureRunnable.run();
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
                this.future = futureRunnable.run();
                this.future.whenDone(f -> onNextFutureCompleted(f, futureCompletionSources));
                this.future.whenDone(this::onCurrentFutureCompleted);
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
