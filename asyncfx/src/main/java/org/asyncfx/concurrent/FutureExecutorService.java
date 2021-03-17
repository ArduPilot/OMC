/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.asyncfx.AsyncFX;
import org.asyncfx.concurrent.Future.RunnableWithProgress;
import org.asyncfx.concurrent.Future.SupplierWithProgress;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FutureExecutorService} wraps asynchronous operations into {@link RunnableFuture} and {@link CallableFuture}
 * instances and submits them to a thread pool for asynchronous execution.
 */
@SuppressWarnings("UnstableApiUsage")
public class FutureExecutorService implements ListeningExecutorService {

    public static FutureExecutorService getInstance() {
        return INSTANCE;
    }

    private static final FutureExecutorService INSTANCE = new FutureExecutorService();

    static class ExecutorThread extends Thread {
        ExecutorThread(Runnable runnable, String name) {
            super(runnable, name);
        }

        void uncaughtException(Throwable throwable) {
            String message =
                "Uncaught exception in async execution [thread = " + Thread.currentThread().getName() + "]";
            Logger logger = getLogger();
            if (logger != null) {
                logger.error(message, throwable);
            } else {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (PrintStream stream = new PrintStream(out, true, StandardCharsets.UTF_8)) {
                    throwable.printStackTrace(stream);
                    System.err.println(message + "\r\n" + out.toString(StandardCharsets.UTF_8));
                }
            }
        }
    }

    private static Logger logger;

    private static synchronized Logger getLogger() {
        if (!AsyncFX.isRunningTests() && logger == null) {
            logger = LoggerFactory.getLogger(FutureExecutorService.class);
        }

        return logger;
    }

    private static AtomicInteger threadCount = new AtomicInteger(0);

    private final ListeningExecutorService EXECUTOR_SERVICE =
        MoreExecutors.listeningDecorator(
            java.util.concurrent.Executors.newCachedThreadPool(
                runnable -> {
                    Thread thread =
                        new ExecutorThread(
                            runnable,
                            FutureExecutorService.class.getSimpleName() + "-thread-" + threadCount.getAndIncrement());
                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler(
                        (t, e) -> {
                            if (e instanceof ThreadDeath) {
                                return;
                            }

                            String message = "Uncaught exception in async execution [thread = " + t.getName() + "]";
                            Logger logger = getLogger();
                            if (logger != null) {
                                logger.error(message, e);
                            } else {
                                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                                try (PrintStream stream = new PrintStream(out, true, StandardCharsets.UTF_8)) {
                                    e.printStackTrace(stream);
                                    System.err.println(message + "\r\n" + out.toString(StandardCharsets.UTF_8));
                                }
                            }
                        });

                    AsyncFX.Accessor.registerThread(thread);
                    return thread;
                }));

    private final ListeningScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE =
        MoreExecutors.listeningDecorator(
            java.util.concurrent.Executors.newScheduledThreadPool(
                2,
                runnable -> {
                    Thread thread =
                        new ExecutorThread(
                            runnable,
                            FutureExecutorService.class.getSimpleName() + "-thread-" + threadCount.getAndIncrement());
                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler(
                        (t, e) -> {
                            if (e instanceof ThreadDeath) {
                                return;
                            }

                            Logger logger = getLogger();
                            logger.error("Uncaught exception in async execution [thread = " + t.getName() + "]", e);
                        });

                    AsyncFX.Accessor.registerThread(thread);
                    return thread;
                }));

    private FutureExecutorService() {}

    boolean isExecutorThread() {
        return Thread.currentThread() instanceof ExecutorThread;
    }

    @Override
    public void shutdown() {
        EXECUTOR_SERVICE.shutdown();
        SCHEDULED_EXECUTOR_SERVICE.shutdown();
    }

    @NotNull
    @Override
    public List<Runnable> shutdownNow() {
        List<Runnable> awaitingExecution = new ArrayList<>(EXECUTOR_SERVICE.shutdownNow());
        awaitingExecution.addAll(SCHEDULED_EXECUTOR_SERVICE.shutdownNow());
        return awaitingExecution;
    }

    @Override
    public boolean isShutdown() {
        return EXECUTOR_SERVICE.isShutdown() && SCHEDULED_EXECUTOR_SERVICE.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return EXECUTOR_SERVICE.isTerminated() && SCHEDULED_EXECUTOR_SERVICE.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        long startTime = System.nanoTime();
        boolean timedOut = EXECUTOR_SERVICE.awaitTermination(timeout, unit);
        if (timedOut) {
            return false;
        }

        long elapsedNanos = System.nanoTime() - startTime;
        long remainingNanos = unit.toNanos(timeout) - elapsedNanos;
        if (remainingNanos > 0) {
            return SCHEDULED_EXECUTOR_SERVICE.awaitTermination(remainingNanos, TimeUnit.NANOSECONDS);
        }

        return false;
    }

    @NotNull
    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        AsyncFX.Accessor.trackAsyncSubmit(tasks.size());
        return EXECUTOR_SERVICE.invokeAll(tasks);
    }

    @NotNull
    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(
            @NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit)
            throws InterruptedException {
        AsyncFX.Accessor.trackAsyncSubmit(tasks.size());
        return EXECUTOR_SERVICE.invokeAll(tasks, timeout, unit);
    }

    @NotNull
    @Override
    public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        AsyncFX.Accessor.trackAsyncSubmit(tasks.size());
        return EXECUTOR_SERVICE.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        AsyncFX.Accessor.trackAsyncSubmit(tasks.size());
        return EXECUTOR_SERVICE.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        AsyncFX.Accessor.trackAsyncSubmit(1);
        EXECUTOR_SERVICE.execute(command);
    }

    @NotNull
    @Override
    public ListenableFuture<?> submit(@NotNull Runnable task) {
        AsyncFX.Accessor.trackAsyncSubmit(1);
        return EXECUTOR_SERVICE.submit(task);
    }

    @NotNull
    @Override
    public <T> ListenableFuture<T> submit(@NotNull Callable<T> task) {
        AsyncFX.Accessor.trackAsyncSubmit(1);
        return EXECUTOR_SERVICE.submit(task);
    }

    @NotNull
    @Override
    public <T> ListenableFuture<T> submit(@NotNull Runnable task, @NotNull T result) {
        AsyncFX.Accessor.trackAsyncSubmit(1);
        return EXECUTOR_SERVICE.submit(task, result);
    }

    public Future<Void> submit(Runnable runnable, CancellationSource cancellationSource) {
        AsyncFX.Accessor.trackAsyncSubmit(1);
        RunnableFuture future = new RunnableFuture(runnable, false);
        cancellationSource.registerFuture(future);
        EXECUTOR_SERVICE.submit(future);
        return future;
    }

    public Future<Void> submit(RunnableWithProgress runnable, CancellationSource cancellationSource) {
        AsyncFX.Accessor.trackAsyncSubmit(1);
        RunnableFuture future = new RunnableFuture(runnable, false);
        cancellationSource.registerFuture(future);
        EXECUTOR_SERVICE.submit(future);
        return future;
    }

    public <T> Future<T> submit(Supplier<T> supplier, CancellationSource cancellationSource) {
        AsyncFX.Accessor.trackAsyncSubmit(1);
        CallableFuture<T> future = new CallableFuture<>(supplier);
        cancellationSource.registerFuture(future);
        EXECUTOR_SERVICE.submit(future);
        return future;
    }

    public <T> Future<T> submit(SupplierWithProgress<T> supplier, CancellationSource cancellationSource) {
        AsyncFX.Accessor.trackAsyncSubmit(1);
        CallableFuture<T> future = new CallableFuture<>(supplier);
        cancellationSource.registerFuture(future);
        EXECUTOR_SERVICE.submit(future);
        return future;
    }

    public Future<Void> schedule(Runnable runnable, Duration delay, CancellationSource cancellationSource) {
        AsyncFX.Accessor.trackAsyncSubmit(1);
        RunnableFuture future = new RunnableFuture(runnable, false);
        cancellationSource.registerFuture(future);
        ListenableFuture<?> scheduledFuture =
            SCHEDULED_EXECUTOR_SERVICE.schedule(future, toMillisOrSaturate(delay), TimeUnit.MILLISECONDS);
        scheduledFuture.addListener(future::complete, MoreExecutors.directExecutor());
        future.setCancellationHandler(scheduledFuture::cancel);
        return future;
    }

    public Future<Void> schedule(RunnableWithProgress runnable, Duration delay, CancellationSource cancellationSource) {
        AsyncFX.Accessor.trackAsyncSubmit(1);
        RunnableFuture future = new RunnableFuture(runnable, false);
        cancellationSource.registerFuture(future);
        ListenableFuture<?> scheduledFuture =
            SCHEDULED_EXECUTOR_SERVICE.schedule(future, toMillisOrSaturate(delay), TimeUnit.MILLISECONDS);
        scheduledFuture.addListener(future::complete, MoreExecutors.directExecutor());
        future.setCancellationHandler(scheduledFuture::cancel);
        return future;
    }

    public <T> Future<T> schedule(Supplier<T> supplier, Duration delay, CancellationSource cancellationSource) {
        AsyncFX.Accessor.trackAsyncSubmit(1);
        CallableFuture<T> future = new CallableFuture<>(supplier);
        cancellationSource.registerFuture(future);
        ListenableFuture<?> scheduledFuture =
            SCHEDULED_EXECUTOR_SERVICE.schedule(future, toMillisOrSaturate(delay), TimeUnit.MILLISECONDS);
        scheduledFuture.addListener(future::complete, MoreExecutors.directExecutor());
        future.setCancellationHandler(scheduledFuture::cancel);
        return future;
    }

    public <T> Future<T> schedule(
            SupplierWithProgress<T> supplier, Duration delay, CancellationSource cancellationSource) {
        AsyncFX.Accessor.trackAsyncSubmit(1);
        CallableFuture<T> future = new CallableFuture<>(supplier);
        cancellationSource.registerFuture(future);
        ListenableFuture<?> scheduledFuture =
            SCHEDULED_EXECUTOR_SERVICE.schedule(future, toMillisOrSaturate(delay), TimeUnit.MILLISECONDS);
        scheduledFuture.addListener(future::complete, MoreExecutors.directExecutor());
        future.setCancellationHandler(scheduledFuture::cancel);
        return future;
    }

    public Future<Void> schedule(
            Runnable runnable, Duration delay, Duration period, CancellationSource cancellationSource) {
        if (period.isZero() || period.isNegative()) {
            throw new IllegalArgumentException("Period must be greater than zero.");
        }

        AsyncFX.Accessor.trackAsyncSubmit(1);
        RunnableFuture future = new RunnableFuture(runnable, true);
        cancellationSource.registerFuture(future);
        ListenableFuture<?> scheduledFuture =
            SCHEDULED_EXECUTOR_SERVICE.scheduleWithFixedDelay(
                future, toMillisOrSaturate(delay), toMillisOrSaturate(period), TimeUnit.MILLISECONDS);
        scheduledFuture.addListener(future::complete, MoreExecutors.directExecutor());
        future.setCancellationHandler(scheduledFuture::cancel);
        return future;
    }

    public Future<Void> schedule(
            RunnableWithProgress runnable, Duration delay, Duration period, CancellationSource cancellationSource) {
        if (period.isZero() || period.isNegative()) {
            throw new IllegalArgumentException("Period must be greater than zero.");
        }

        AsyncFX.Accessor.trackAsyncSubmit(1);
        RunnableFuture future = new RunnableFuture(runnable, true);
        cancellationSource.registerFuture(future);
        ListenableFuture<?> scheduledFuture =
            SCHEDULED_EXECUTOR_SERVICE.scheduleWithFixedDelay(
                future, toMillisOrSaturate(delay), toMillisOrSaturate(period), TimeUnit.MILLISECONDS);
        scheduledFuture.addListener(future::complete, MoreExecutors.directExecutor());
        future.setCancellationHandler(scheduledFuture::cancel);
        return future;
    }

    private long toMillisOrSaturate(Duration duration) {
        Duration maxDuration = Duration.ofMillis(Long.MAX_VALUE);
        Duration saturatedDuration = duration.compareTo(maxDuration) > 0 ? maxDuration : duration;
        return saturatedDuration.toMillis();
    }
}
