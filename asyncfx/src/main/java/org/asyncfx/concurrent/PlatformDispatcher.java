/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.time.Duration;
import java.util.function.Supplier;
import javafx.application.Platform;
import org.asyncfx.AsyncFX;
import org.asyncfx.concurrent.Future.RunnableWithProgress;
import org.asyncfx.concurrent.Future.SupplierWithProgress;
import org.jetbrains.annotations.NotNull;

class PlatformDispatcher implements Dispatcher {

    static final PlatformDispatcher INSTANCE = new PlatformDispatcher();
    static Thread THREAD;

    static {
        Platform.runLater(() -> THREAD = Thread.currentThread());
    }

    @Override
    public boolean hasAccess() {
        return Platform.isFxApplicationThread();
    }

    @Override
    public boolean isSequential() {
        return true;
    }

    @Override
    public void run(@NotNull Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            long startTime = System.nanoTime();
            runnable.run();
            AsyncFX.Accessor.trackPlatformSubmit(System.nanoTime() - startTime);
        } else {
            PlatformFutureFactory.getInstance().execute(runnable);
        }
    }

    @Override
    public void runLater(Runnable runnable) {
        PlatformFutureFactory.getInstance().execute(runnable);
    }

    @Override
    public void runLater(Runnable runnable, Duration delay) {
        PlatformFutureFactory.getInstance().newRunFuture(runnable, delay);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable) {
        return PlatformFutureFactory.getInstance().newRunFuture(runnable);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, Duration delay) {
        return PlatformFutureFactory.getInstance().newRunFuture(runnable, delay);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, Duration delay, Duration period) {
        return PlatformFutureFactory.getInstance().newRunFuture(runnable, delay, period);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, CancellationSource cancellationSource) {
        return PlatformFutureFactory.getInstance().newRunFuture(runnable, cancellationSource);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, Duration delay, CancellationSource cancellationSource) {
        return PlatformFutureFactory.getInstance().newRunFuture(runnable, delay, cancellationSource);
    }

    @Override
    public Future<Void> runLaterAsync(
            Runnable runnable, Duration delay, Duration period, CancellationSource cancellationSource) {
        return PlatformFutureFactory.getInstance().newRunFuture(runnable, delay, period, cancellationSource);
    }

    @Override
    public Future<Void> runLaterAsync(RunnableWithProgress runnable) {
        return PlatformFutureFactory.getInstance().newRunFuture(runnable);
    }

    @Override
    public Future<Void> runLaterAsync(RunnableWithProgress runnable, CancellationSource cancellationSource) {
        return PlatformFutureFactory.getInstance().newRunFuture(runnable, cancellationSource);
    }

    @Override
    public Future<Void> runLaterAsync(RunnableWithProgress runnable, Duration delay) {
        return PlatformFutureFactory.getInstance().newRunFuture(runnable, delay);
    }

    @Override
    public Future<Void> runLaterAsync(RunnableWithProgress runnable, Duration delay, Duration period) {
        return PlatformFutureFactory.getInstance().newRunFuture(runnable, delay, period);
    }

    @Override
    public Future<Void> runLaterAsync(
            RunnableWithProgress runnable, Duration delay, CancellationSource cancellationSource) {
        return PlatformFutureFactory.getInstance().newRunFuture(runnable, delay, cancellationSource);
    }

    @Override
    public Future<Void> runLaterAsync(
            RunnableWithProgress runnable, Duration delay, Duration period, CancellationSource cancellationSource) {
        return PlatformFutureFactory.getInstance().newRunFuture(runnable, delay, period, cancellationSource);
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier) {
        return PlatformFutureFactory.getInstance().newGetFuture(supplier);
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier, Duration delay) {
        return PlatformFutureFactory.getInstance().newGetFuture(supplier, delay);
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier, CancellationSource cancellationSource) {
        return PlatformFutureFactory.getInstance().newGetFuture(supplier, cancellationSource);
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier, Duration delay, CancellationSource cancellationSource) {
        return PlatformFutureFactory.getInstance().newGetFuture(supplier, delay, cancellationSource);
    }

    @Override
    public <T> Future<T> getLaterAsync(SupplierWithProgress<T> supplier) {
        return PlatformFutureFactory.getInstance().newGetFuture(supplier);
    }

    @Override
    public <T> Future<T> getLaterAsync(SupplierWithProgress<T> supplier, CancellationSource cancellationSource) {
        return PlatformFutureFactory.getInstance().newGetFuture(supplier, cancellationSource);
    }

    @Override
    public <T> Future<T> getLaterAsync(SupplierWithProgress<T> supplier, Duration delay) {
        return PlatformFutureFactory.getInstance().newGetFuture(supplier, delay);
    }

    @Override
    public <T> Future<T> getLaterAsync(
            SupplierWithProgress<T> supplier, Duration delay, CancellationSource cancellationSource) {
        return PlatformFutureFactory.getInstance().newGetFuture(supplier, delay, cancellationSource);
    }

    @Override
    public String toString() {
        return "JavaFX application thread";
    }
}
