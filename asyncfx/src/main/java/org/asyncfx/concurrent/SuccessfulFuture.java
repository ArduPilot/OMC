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

/** Specialized future class that can be used in situations when a future is created in the successful state. */
class SuccessfulFuture<V> extends AbstractFuture<V> {

    private final ProgressInfo progressInfo = new ProgressInfo(this);
    private final V value;

    SuccessfulFuture(V value) {
        this.value = value;
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
        return true;
    }

    @Override
    public boolean isFailed() {
        return false;
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
        return value;
    }

    @Override
    public V get(long timeout, @NotNull TimeUnit unit) {
        return value;
    }

    @Override
    public AggregateException getException() {
        return null;
    }

    @Override
    public Future<Void> thenRunAsync(FutureRunnable runnable) {
        return runnable.run();
    }

    @Override
    public Future<Void> thenRunAsync(FutureRunnableWithProgress runnable) {
        return runnable.run(progressInfo);
    }

    @Override
    public Future<Void> thenFinallyRunAsync(FutureFinallyRunnable runnable) {
        return runnable.run(getException());
    }

    @Override
    public Future<Void> thenFinallyRunAsync(FutureFinallyRunnableWithProgress runnable) {
        return runnable.run(getException(), progressInfo);
    }

    @Override
    public <R> Future<R> thenGetAsync(FutureSupplier<R> supplier) {
        return supplier.get();
    }

    @Override
    public <R> Future<R> thenGetAsync(FutureSupplierWithProgress<R> supplier) {
        return supplier.get(progressInfo);
    }

    @Override
    public <R> Future<R> thenFinallyGetAsync(FutureFinallySupplier<R> supplier) {
        return supplier.get(getException());
    }

    @Override
    public <R> Future<R> thenFinallyGetAsync(FutureFinallySupplierWithProgress<R> supplier) {
        return supplier.get(getException(), progressInfo);
    }

    @Override
    public Future<Void> thenAcceptAsync(FutureConsumer<V> consumer) {
        return consumer.accept(value);
    }

    @Override
    public Future<Void> thenAcceptAsync(FutureConsumerWithProgress<V> consumer) {
        return consumer.accept(value, progressInfo);
    }

    @Override
    public Future<Void> thenFinallyAcceptAsync(FutureFinallyConsumer<V> consumer) {
        return consumer.accept(value, getException());
    }

    @Override
    public Future<Void> thenFinallyAcceptAsync(FutureFinallyConsumerWithProgress<V> consumer) {
        return consumer.accept(value, getException(), progressInfo);
    }

    @Override
    public <R> Future<R> thenApplyAsync(FutureFunction<V, R> function) {
        return function.apply(value);
    }

    @Override
    public <R> Future<R> thenApplyAsync(FutureFunctionWithProgress<V, R> function) {
        return function.apply(value, progressInfo);
    }

    @Override
    public <R> Future<R> thenFinallyApplyAsync(FutureFinallyFunction<V, R> function) {
        return function.apply(value, getException());
    }

    @Override
    public <R> Future<R> thenFinallyApplyAsync(FutureFinallyFunctionWithProgress<V, R> function) {
        return function.apply(value, getException(), progressInfo);
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
    public void addListener(@NotNull Runnable runnable, @NotNull Executor executor) {
        executor.execute(runnable);
    }

    @Override
    public Future<V> whenSucceeded(Runnable runnable, Executor executor) {
        executor.execute(runnable);
        return this;
    }

    @Override
    public Future<V> whenSucceeded(Consumer<V> consumer, Executor executor) {
        executor.execute(() -> consumer.accept(getUnchecked()));
        return this;
    }

    @Override
    public Future<V> whenFailed(Runnable runnable, Executor executor) {
        return this;
    }

    @Override
    public Future<V> whenFailed(Consumer<AggregateException> consumer, Executor executor) {
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
