/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

/** Specialized future class that can be used in situations when a future is created in the failed state. */
class FailedFuture<V> extends AbstractFuture<V> {

    private final ProgressInfo progressInfo = new ProgressInfo(this);
    private AggregateException exception;

    FailedFuture(Throwable throwable) {
        this.exception =
            throwable instanceof AggregateException ? (AggregateException)throwable : new AggregateException(throwable);
    }

    @Override
    ProgressInfo getProgressInfo() {
        return progressInfo;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean isFailed() {
        return true;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public V get() {
        throw exception;
    }

    @Override
    public V get(long timeout, @NotNull TimeUnit unit) {
        throw exception;
    }

    @Override
    public Future<Void> thenRunAsync(FutureRunnable runnable) {
        return new FailedFuture<>(exception);
    }

    @Override
    public Future<Void> thenRunAsync(FutureRunnableWithProgress runnable) {
        return new FailedFuture<>(exception);
    }

    @Override
    public Future<Void> thenFinallyRunAsync(FutureFinallyRunnable runnable) {
        return createCompletableFuture(runnable.run(exception));
    }

    @Override
    public Future<Void> thenFinallyRunAsync(FutureFinallyRunnableWithProgress runnable) {
        return createCompletableFuture(runnable.run(exception, progressInfo));
    }

    @Override
    public <R> Future<R> thenGetAsync(FutureSupplier<R> supplier) {
        return new FailedFuture<>(exception);
    }

    @Override
    public <R> Future<R> thenGetAsync(FutureSupplierWithProgress<R> supplier) {
        return new FailedFuture<>(exception);
    }

    @Override
    public <R> Future<R> thenFinallyGetAsync(FutureFinallySupplier<R> supplier) {
        return createCompletableFuture(supplier.get(exception));
    }

    @Override
    public <R> Future<R> thenFinallyGetAsync(FutureFinallySupplierWithProgress<R> supplier) {
        return createCompletableFuture(supplier.get(exception, progressInfo));
    }

    @Override
    public Future<Void> thenAcceptAsync(FutureConsumer<V> consumer) {
        return new FailedFuture<>(exception);
    }

    @Override
    public Future<Void> thenAcceptAsync(FutureConsumerWithProgress<V> consumer) {
        return new FailedFuture<>(exception);
    }

    @Override
    public Future<Void> thenFinallyAcceptAsync(FutureFinallyConsumer<V> consumer) {
        return createCompletableFuture(consumer.accept(null, exception));
    }

    @Override
    public Future<Void> thenFinallyAcceptAsync(FutureFinallyConsumerWithProgress<V> consumer) {
        return createCompletableFuture(consumer.accept(null, exception, progressInfo));
    }

    @Override
    public <R> Future<R> thenApplyAsync(FutureFunction<V, R> function) {
        return new FailedFuture<>(exception);
    }

    @Override
    public <R> Future<R> thenApplyAsync(FutureFunctionWithProgress<V, R> function) {
        return new FailedFuture<>(exception);
    }

    @Override
    public <R> Future<R> thenFinallyApplyAsync(FutureFinallyFunction<V, R> function) {
        return createCompletableFuture(function.apply(null, exception));
    }

    @Override
    public <R> Future<R> thenFinallyApplyAsync(FutureFinallyFunctionWithProgress<V, R> function) {
        return createCompletableFuture(function.apply(null, exception, progressInfo));
    }

    @Override
    public Future<V> whenCancelled(Runnable runnable) {
        return this;
    }

    @Override
    public Future<V> whenCancelled(Runnable runnable, Executor executor) {
        return this;
    }

    @Override
    public AggregateException getException() {
        return exception;
    }

    @Override
    public void addListener(@NotNull Runnable runnable, @NotNull Executor executor) {
        executor.execute(runnable);
    }

    private <T> Future<T> createCompletableFuture(Future<T> wrappedFuture) {
        CompletableFuture<T> future = new CompletableFuture<>();
        wrappedFuture.whenDone(
            f -> {
                if (f.isSuccess()) {
                    future.completeWithResult(f.getUnchecked());
                } else if (f.isFailed()) {
                    future.completeWithException(
                        new AggregateException(f.getException().getCause(), exception.getThrowables()));
                } else {
                    future.completeWithCancellation(exception);
                }
            });

        return future;
    }

    @Override
    public Future<V> whenSucceeded(Runnable runnable, Executor executor) {
        return this;
    }

    @Override
    public Future<V> whenSucceeded(Consumer<V> consumer, Executor executor) {
        return this;
    }

    @Override
    public Future<V> whenFailed(Runnable runnable, Executor executor) {
        executor.execute(runnable);
        return this;
    }

    @Override
    public Future<V> whenFailed(Consumer<AggregateException> consumer, Executor executor) {
        executor.execute(() -> consumer.accept(getException()));
        return this;
    }

    @Override
    public Future<V> whenDone(Runnable runnable, Executor executor) {
        executor.execute(runnable);
        return this;
    }

    @Override
    public Future<V> whenDone(Consumer<Future<V>> consumer, Executor executor) {
        executor.execute(() -> consumer.accept(this));
        return this;
    }

}
