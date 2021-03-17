/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import javafx.application.Platform;
import org.asyncfx.AsyncFX;
import org.jetbrains.annotations.NotNull;

/** Specialized future that is used by {@link PlatformFutureFactory} to post operations to the UI thread. */
class PlatformFuture<V> extends CompletableFuture<V> {

    private static class PlatformExecutor implements Executor {
        @Override
        public void execute(@NotNull Runnable command) {
            if (Platform.isFxApplicationThread()) {
                long startTime = System.nanoTime();
                command.run();
                AsyncFX.Accessor.trackPlatformSubmit(System.nanoTime() - startTime);
            } else {
                Platform.runLater(
                    () -> {
                        long startTime = System.nanoTime();
                        command.run();
                        AsyncFX.Accessor.trackPlatformSubmit(System.nanoTime() - startTime);
                    });
            }
        }
    }

    interface CancellationHandler {
        void cancel(boolean mayInterruptIfRunning);
    }

    private static final PlatformExecutor EXECUTOR = new PlatformExecutor();

    private CancellationHandler cancellationHandler;

    @Override
    public V get() throws ExecutionException {
        if (isDone()) {
            return super.get();
        }

        long startTime = System.nanoTime();
        V ret = super.get();
        AsyncFX.Accessor.trackAwaitPlatform(System.nanoTime() - startTime);
        return ret;
    }

    @Override
    public V get(long timeout, @NotNull TimeUnit unit) throws ExecutionException, TimeoutException {
        if (isDone()) {
            return super.get(timeout, unit);
        }

        long startTime = System.nanoTime();
        V ret = super.get(timeout, unit);
        AsyncFX.Accessor.trackAwaitPlatform(System.nanoTime() - startTime);
        return ret;
    }

    @Override
    boolean cancellationRequested(boolean mayInterruptIfRunning) {
        if (cancellationHandler != null) {
            cancellationHandler.cancel(mayInterruptIfRunning);
        }

        return true;
    }

    synchronized void setCancellationHandler(CancellationHandler cancellationHandler) {
        this.cancellationHandler = cancellationHandler;
    }

    @Override
    public Future<V> whenSucceeded(Runnable runnable) {
        return super.whenSucceeded(runnable, EXECUTOR);
    }

    @Override
    public Future<V> whenSucceeded(Consumer<V> consumer) {
        return super.whenSucceeded(consumer, EXECUTOR);
    }

    @Override
    public Future<V> whenFailed(Runnable runnable) {
        return super.whenFailed(runnable, EXECUTOR);
    }

    @Override
    public Future<V> whenFailed(Consumer<AggregateException> consumer) {
        return super.whenFailed(consumer, EXECUTOR);
    }

    @Override
    public Future<V> whenDone(Runnable runnable) {
        return super.whenDone(runnable, EXECUTOR);
    }

    @Override
    public Future<V> whenDone(Consumer<Future<V>> consumer) {
        return super.whenDone(consumer, EXECUTOR);
    }

}
