/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import static org.asyncfx.concurrent.FutureFlags.DONE;
import static org.asyncfx.concurrent.FutureFlags.RUNNING;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * This is a specialized future class that is used by {@link FutureExecutorService} to represent operations running on a
 * background thread that return a value.
 */
class CallableFuture<V> extends CompletableFuture<V> implements Callable<V> {

    interface CancellationHandler {
        void cancel(boolean mayInterruptIfRunning);
    }

    private final Supplier<V> supplier;
    private final SupplierWithProgress<V> supplierWithProgress;
    private CancellationHandler cancellationHandler;
    private Thread executingThread;

    CallableFuture(Supplier<V> supplier) {
        this.supplier = supplier;
        this.supplierWithProgress = null;
    }

    CallableFuture(SupplierWithProgress<V> supplier) {
        this.supplier = null;
        this.supplierWithProgress = supplier;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public V call() {
        ProgressInfo progressInfo = getProgressInfo();

        synchronized (this) {
            if (isDone()) {
                return null;
            }

            if (progressInfo.isCancellationRequested()) {
                completeWithCancellation(null);
                return null;
            }

            executingThread = Thread.currentThread();
            setFlagUnsafe(RUNNING);
        }

        try {
            V value;

            if (supplier != null) {
                value = supplier.get();
            } else {
                value = supplierWithProgress.get(progressInfo);
            }

            synchronized (this) {
                executingThread = null;
            }

            if (progressInfo.isCancellationRequested()) {
                completeWithCancellation(null);
            } else {
                completeWithResult(value);
            }
        } catch (Throwable throwable) {
            completeWithException(throwable);
        }

        synchronized (this) {
            clearFlagUnsafe(RUNNING);
        }

        return null;
    }

    @Override
    boolean cancellationRequested(boolean mayInterruptIfRunning) {
        if (mayInterruptIfRunning) {
            if (executingThread != null) {
                executingThread.interrupt();
            }
        }

        if (cancellationHandler != null) {
            cancellationHandler.cancel(false);
        }

        return true;
    }

    synchronized void setCancellationHandler(CancellationHandler cancellationHandler) {
        this.cancellationHandler = cancellationHandler;
    }

    synchronized void complete() {
        if (!hasFlagUnsafe(RUNNING) && !hasFlagUnsafe(DONE)) {
            completeWithCancellation(null);
        }
    }

}
