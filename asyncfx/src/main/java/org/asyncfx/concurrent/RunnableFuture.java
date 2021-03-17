/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import static org.asyncfx.concurrent.FutureFlags.DONE;
import static org.asyncfx.concurrent.FutureFlags.RUNNING;

import java.util.concurrent.CancellationException;

/**
 * This is a specialized future class that is used by {@link FutureExecutorService} to represent operations running on a
 * background thread that do not return a value.
 */
class RunnableFuture extends CompletableFuture<Void> implements Runnable {

    interface CancellationHandler {
        void cancel(boolean mayInterruptIfRunning);
    }

    private final Runnable runnable;
    private final RunnableWithProgress runnableWithProgress;
    private final boolean indefinite;
    private CancellationHandler cancellationHandler;
    private Thread executingThread;

    RunnableFuture(Runnable runnable, boolean indefinite) {
        this.runnable = runnable;
        this.runnableWithProgress = null;
        this.indefinite = indefinite;
    }

    RunnableFuture(RunnableWithProgress runnable, boolean indefinite) {
        this.runnable = null;
        this.runnableWithProgress = runnable;
        this.indefinite = indefinite;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run() {
        ProgressInfo progressInfo = getProgressInfo();

        synchronized (this) {
            if (isDone()) {
                return;
            }

            if (progressInfo.isCancellationRequested()) {
                completeWithCancellation(null);
                return;
            }

            executingThread = Thread.currentThread();
            setFlagUnsafe(RUNNING);
        }

        try {
            if (runnable != null) {
                runnable.run();
            } else {
                runnableWithProgress.run(progressInfo);
            }

            synchronized (this) {
                executingThread = null;
            }
        } catch (Throwable throwable) {
            completeWithException(throwable);

            if (indefinite) {
                throw new CancellationException();
            }
        }

        if (progressInfo.isCancellationRequested()) {
            completeWithCancellation(null);

            if (indefinite) {
                throw new CancellationException();
            }
        } else if (!indefinite) {
            completeWithResult(null);
        }

        synchronized (this) {
            clearFlagUnsafe(RUNNING);
        }
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
