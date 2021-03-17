/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.time.Duration;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.asyncfx.AsyncFX;
import org.asyncfx.concurrent.Future.RunnableWithProgress;
import org.asyncfx.concurrent.Future.SupplierWithProgress;
import org.jetbrains.annotations.NotNull;

class DefaultFutureFactory implements Executor {

    static DefaultFutureFactory getInstance() {
        return INSTANCE;
    }

    private static final DefaultFutureFactory INSTANCE = new DefaultFutureFactory();

    private final FutureExecutorService executorService = FutureExecutorService.getInstance();
    private final ThreadLocal<Stack<AbstractFuture<?>>> executionScope = ThreadLocal.withInitial(Stack::new);

    private int maxDirectExecutions = 10;

    boolean isFactoryThread() {
        return executorService.isExecutorThread();
    }

    boolean canExecuteDirectly() {
        if (!executorService.isExecutorThread()) {
            return false;
        }

        return executionScope.get().size() < maxDirectExecutions;
    }

    public int getMaxDirectExecutions() {
        return maxDirectExecutions;
    }

    public void setMaxDirectExecutions(int value) {
        maxDirectExecutions = value;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        executorService.execute(command);
    }

    public <V> Future<V> fromListenableFuture(ListenableFuture<V> future) {
        FutureCompletionSource<V> futureCompletionSource =
            new FutureCompletionSource<>() {

                protected void cancellationRequested(boolean mayInterruptIfRunning) {
                    future.cancel(mayInterruptIfRunning);
                }
            };

        future.addListener(
            () -> {
                if (future.isCancelled()) {
                    futureCompletionSource.setCancelled();
                } else {
                    try {
                        futureCompletionSource.setResult(future.get());
                    } catch (ExecutionException | InterruptedException ex) {
                        futureCompletionSource.setException(ex);
                    }
                }
            },
            MoreExecutors.directExecutor());

        return futureCompletionSource.getFuture();
    }

    public <V> Future<V> successful(V value) {
        if (canCallDirectly()) {
            AsyncFX.Accessor.trackElidedAsyncSubmit();
            return new SuccessfulFuture<>(value);
        }

        CompletableFuture<V> future = new CompletableFuture<>();
        future.completeWithResult(value);
        return future;
    }

    public <V> Future<V> failed(Throwable throwable) {
        if (canCallDirectly()) {
            AsyncFX.Accessor.trackElidedAsyncSubmit();
            return new FailedFuture<>(throwable);
        }

        CompletableFuture<V> future = new CompletableFuture<>();
        future.completeWithException(throwable);
        return future;
    }

    public <V> Future<V> cancelled() {
        if (canCallDirectly()) {
            AsyncFX.Accessor.trackElidedAsyncSubmit();
            return new CancelledFuture<>();
        }

        CompletableFuture<V> future = new CompletableFuture<>();
        future.completeWithCancellation(null);
        return future;
    }

    public Future<Future<?>[]> whenAll(Future<?>... futures) {
        if (futures.length == 0) {
            throw new IllegalArgumentException("future");
        }

        var result =
            new CompletableFuture<Future<?>[]>() {
                private AtomicInteger completedCount = new AtomicInteger(0);

                void signalComplete() {
                    if (completedCount.incrementAndGet() == futures.length) {
                        completeWithResult(futures);
                    }
                }

                boolean cancellationRequested(boolean mayInterruptIfRunning) {
                    boolean result = false;
                    for (Future<?> future : futures) {
                        result |= future.cancel(mayInterruptIfRunning);
                    }

                    return result;
                }
            };

        for (var future : futures) {
            future.whenDone(result::signalComplete);
        }

        return result;
    }

    Future<Void> newRunFuture(Runnable runnable) {
        if (canCallDirectly()) {
            AsyncFX.Accessor.trackElidedAsyncSubmit();
            return callDirectly(runnable);
        }

        return executorService.submit(runnable, CancellationSource.DEFAULT);
    }

    Future<Void> newRunFuture(Runnable runnable, CancellationSource cancellationSource) {
        if (canCallDirectly()) {
            AsyncFX.Accessor.trackElidedAsyncSubmit();
            return callDirectly(runnable);
        }

        return executorService.submit(runnable, cancellationSource);
    }

    <V> Future<V> newGetFuture(Supplier<V> supplier) {
        if (canCallDirectly()) {
            AsyncFX.Accessor.trackElidedAsyncSubmit();
            return callDirectly(supplier);
        }

        return executorService.submit(supplier, CancellationSource.DEFAULT);
    }

    <V> Future<V> newGetFuture(Supplier<V> supplier, CancellationSource cancellationSource) {
        if (canCallDirectly()) {
            AsyncFX.Accessor.trackElidedAsyncSubmit();
            return callDirectly(supplier);
        }

        return executorService.submit(supplier, cancellationSource);
    }

    Future<Void> newRunFuture(RunnableWithProgress runnable) {
        if (canCallDirectly()) {
            AsyncFX.Accessor.trackElidedAsyncSubmit();
            return callDirectly(runnable);
        }

        return executorService.submit(runnable, CancellationSource.DEFAULT);
    }

    Future<Void> newRunFuture(RunnableWithProgress runnable, CancellationSource cancellationSource) {
        if (canCallDirectly()) {
            AsyncFX.Accessor.trackElidedAsyncSubmit();
            return callDirectly(runnable);
        }

        return executorService.submit(runnable, cancellationSource);
    }

    <V> Future<V> newGetFuture(SupplierWithProgress<V> supplier) {
        if (canCallDirectly()) {
            AsyncFX.Accessor.trackElidedAsyncSubmit();
            return callDirectly(supplier);
        }

        return executorService.submit(supplier, CancellationSource.DEFAULT);
    }

    <V> Future<V> newGetFuture(SupplierWithProgress<V> supplier, CancellationSource cancellationSource) {
        if (canCallDirectly()) {
            AsyncFX.Accessor.trackElidedAsyncSubmit();
            return callDirectly(supplier);
        }

        return executorService.submit(supplier, cancellationSource);
    }

    Future<Void> newRunFuture(Runnable runnable, Duration delay) {
        return executorService.schedule(runnable, delay, CancellationSource.DEFAULT);
    }

    Future<Void> newRunFuture(Runnable runnable, Duration delay, CancellationSource cancellationSource) {
        return executorService.schedule(runnable, delay, cancellationSource);
    }

    Future<Void> newRunFuture(Runnable runnable, Duration delay, Duration period) {
        return executorService.schedule(runnable, delay, period, CancellationSource.DEFAULT);
    }

    Future<Void> newRunFuture(
            Runnable runnable, Duration delay, Duration period, CancellationSource cancellationSource) {
        return executorService.schedule(runnable, delay, period, cancellationSource);
    }

    <T> Future<T> newGetFuture(Supplier<T> supplier, Duration delay) {
        return executorService.schedule(supplier, delay, CancellationSource.DEFAULT);
    }

    <T> Future<T> newGetFuture(Supplier<T> supplier, Duration delay, CancellationSource cancellationSource) {
        return executorService.schedule(supplier, delay, cancellationSource);
    }

    Future<Void> newRunFuture(RunnableWithProgress runnable, Duration delay) {
        return executorService.schedule(runnable, delay, CancellationSource.DEFAULT);
    }

    Future<Void> newRunFuture(RunnableWithProgress runnable, Duration delay, CancellationSource cancellationSource) {
        return executorService.schedule(runnable, delay, cancellationSource);
    }

    Future<Void> newRunFuture(RunnableWithProgress runnable, Duration delay, Duration period) {
        return executorService.schedule(runnable, delay, period, CancellationSource.DEFAULT);
    }

    Future<Void> newRunFuture(
            RunnableWithProgress runnable, Duration delay, Duration period, CancellationSource cancellationSource) {
        return executorService.schedule(runnable, delay, period, cancellationSource);
    }

    <T> Future<T> newGetFuture(SupplierWithProgress<T> supplier, Duration delay) {
        return executorService.schedule(supplier, delay, CancellationSource.DEFAULT);
    }

    <T> Future<T> newGetFuture(
            SupplierWithProgress<T> supplier, Duration delay, CancellationSource cancellationSource) {
        return executorService.schedule(supplier, delay, cancellationSource);
    }

    void pushExecutionScope(AbstractFuture<?> future) {
        if (AsyncFX.isFutureElisionOptimizationEnabled()) {
            executionScope.get().push(future);
        }
    }

    void popExecutionScope() {
        if (AsyncFX.isFutureElisionOptimizationEnabled()) {
            executionScope.get().pop();
        }
    }

    private boolean canCallDirectly() {
        if (!AsyncFX.isFutureElisionOptimizationEnabled()) {
            return false;
        }

        return !executionScope.get().isEmpty();
    }

    private Future<Void> callDirectly(Runnable runnable) {
        try {
            runnable.run();
            return new SuccessfulFuture<>(null);
        } catch (Throwable throwable) {
            return new FailedFuture<>(throwable);
        }
    }

    private Future<Void> callDirectly(RunnableWithProgress runnable) {
        try {
            ProgressInfo progressInfo = executionScope.get().peek().getProgressInfo();
            runnable.run(progressInfo);
            return progressInfo.isCancellationRequested() ? new CancelledFuture<>() : new SuccessfulFuture<>(null);
        } catch (Throwable throwable) {
            return new FailedFuture<>(throwable);
        }
    }

    private <T> Future<T> callDirectly(Supplier<T> supplier) {
        try {
            return new SuccessfulFuture<>(supplier.get());
        } catch (Throwable throwable) {
            return new FailedFuture<>(throwable);
        }
    }

    private <T> Future<T> callDirectly(SupplierWithProgress<T> supplier) {
        try {
            ProgressInfo progressInfo = executionScope.get().peek().getProgressInfo();
            T result = supplier.get(progressInfo);
            return progressInfo.isCancellationRequested() ? new CancelledFuture<>() : new SuccessfulFuture<>(result);
        } catch (Throwable throwable) {
            return new FailedFuture<>(throwable);
        }
    }

}
