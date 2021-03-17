/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import static org.asyncfx.concurrent.FutureFlags.PENDING_CANCELLATION;

import java.util.concurrent.Executor;

/**
 * Specialized future class that is used to represent the continuation of a future. In a chain of continuations, all but
 * the first future will be instances of this class.
 */
class ContinuationFuture<V, R> extends CompletableFuture<R> implements Runnable {

    private final FutureProvider<V, R> futureProvider;
    private final ContinuationOption continuationOption;
    private final int rank;
    private AbstractFuture<V> predecessor;
    private Future<R> activeFuture;
    private boolean mayInterruptIfRunning;

    ContinuationFuture(
            AbstractFuture<V> predecessor, FutureProvider<V, R> futureProvider, ContinuationOption continuationOption) {
        this.rank = predecessor.getRank() + 1;
        this.predecessor = predecessor;
        this.futureProvider = futureProvider;
        this.continuationOption = continuationOption;
    }

    @Override
    int getRank() {
        return rank;
    }

    @Override
    void completeWithResult(R value) {
        DefaultFutureFactory factory = DefaultFutureFactory.getInstance();
        if (factory.isFactoryThread()) {
            super.completeWithResult(value);
        } else {
            factory.execute(() -> super.completeWithResult(value));
        }
    }

    @Override
    void completeWithException(Throwable throwable) {
        DefaultFutureFactory factory = DefaultFutureFactory.getInstance();
        if (factory.isFactoryThread()) {
            super.completeWithException(throwable);
        } else {
            factory.execute(() -> super.completeWithException(throwable));
        }
    }

    @Override
    void completeWithCancellation(AggregateException exception) {
        DefaultFutureFactory factory = DefaultFutureFactory.getInstance();
        if (factory.isFactoryThread()) {
            super.completeWithCancellation(exception);
        } else {
            factory.execute(() -> super.completeWithCancellation(exception));
        }
    }

    @Override
    public void run() {
        synchronized (this) {
            if (hasFlagUnsafe(PENDING_CANCELLATION)) {
                completeWithCancellation(predecessor.getException());
                return;
            }
        }

        DefaultFutureFactory factory = DefaultFutureFactory.getInstance();
        try {
            factory.pushExecutionScope(this);
            Future<R> future = futureProvider.provide(predecessor, getProgressInfo());
            if (hasFlag(PENDING_CANCELLATION)) {
                future.cancel(mayInterruptIfRunning);
            }

            activeFuture = future;
            activeFuture.whenDone(this::onDone);
        } catch (Throwable throwable) {
            Thread currentThread = Thread.currentThread();
            if (currentThread instanceof FutureExecutorService.ExecutorThread) {
                ((FutureExecutorService.ExecutorThread)currentThread).uncaughtException(throwable);
            } else {
                throw throwable;
            }
        } finally {
            factory.popExecutionScope();
        }
    }

    @Override
    @MaybeUnsynchronized
    void runContinuations(ContinuationOption continuationOption) {
        synchronized (this) {
            predecessor = null;
        }

        super.runContinuations(continuationOption);
    }

    @MaybeUnsynchronized
    void runIfContinued(ContinuationOption condition, Executor executor) {
        boolean willContinue =
            this.continuationOption == ContinuationOption.ALWAYS
                || condition == ContinuationOption.SUCCESS && this.continuationOption.continueOnSuccess()
                || condition == ContinuationOption.FAILURE && this.continuationOption.continueOnFailure()
                || condition == ContinuationOption.CANCELLED && this.continuationOption.continueOnCancelled();

        if (willContinue) {
            executor.execute(this);
        } else if (condition == ContinuationOption.CANCELLED) {
            completeWithCancellation(null);
        } else {
            completeWithException(predecessor.getException());
        }
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (hasFlagUnsafe(PENDING_CANCELLATION)) {
            return false;
        }

        this.mayInterruptIfRunning = mayInterruptIfRunning;

        setFlagUnsafe(PENDING_CANCELLATION);

        if (predecessor != null && predecessor.cancel(mayInterruptIfRunning)) {
            return true;
        }

        if (super.cancel(mayInterruptIfRunning)) {
            return true;
        }

        return cancelContinuations(mayInterruptIfRunning);
    }

    @Override
    boolean cancellationRequested(boolean mayInterruptIfRunning) {
        if (activeFuture != null) {
            return activeFuture.cancel(mayInterruptIfRunning);
        }

        return true;
    }

    @SuppressWarnings("ConstantConditions")
    private void onDone(Future<R> future) {
        if (future.isSuccess()) {
            completeWithResult(future.getUnchecked());
        } else if (future.isFailed()) {
            AggregateException exception = predecessor.getException();
            if (exception != null) {
                completeWithException(
                    new AggregateException(future.getException().getCause(), exception.getThrowables()));
            } else {
                completeWithException(future.getException());
            }
        } else {
            completeWithCancellation(predecessor.getException());
        }
    }

}
