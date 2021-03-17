/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.time.Duration;
import java.util.function.Supplier;
import org.asyncfx.concurrent.Future.RunnableWithProgress;
import org.asyncfx.concurrent.Future.SupplierWithProgress;
import org.jetbrains.annotations.NotNull;

class BackgroundDispatcher implements Dispatcher {

    static final BackgroundDispatcher INSTANCE = new BackgroundDispatcher();

    @Override
    public boolean hasAccess() {
        return DefaultFutureFactory.getInstance().isFactoryThread();
    }

    @Override
    public void execute(@NotNull Runnable runnable) {
        run(runnable);
    }

    @Override
    public void run(@NotNull Runnable runnable) {
        if (DefaultFutureFactory.getInstance().isFactoryThread()) {
            runnable.run();
        } else {
            FutureExecutorService.getInstance().execute(runnable);
        }
    }

    @Override
    public void runLater(Runnable runnable) {
        FutureExecutorService.getInstance().execute(runnable);
    }

    @Override
    public void runLater(Runnable runnable, Duration delay) {
        DefaultFutureFactory.getInstance().newRunFuture(runnable, delay);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable) {
        return DefaultFutureFactory.getInstance().newRunFuture(runnable);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, Duration delay) {
        return DefaultFutureFactory.getInstance().newRunFuture(runnable, delay);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, Duration delay, Duration period) {
        return DefaultFutureFactory.getInstance().newRunFuture(runnable, delay, period);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, CancellationSource cancellationSource) {
        return DefaultFutureFactory.getInstance().newRunFuture(runnable, cancellationSource);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, Duration delay, CancellationSource cancellationSource) {
        return DefaultFutureFactory.getInstance().newRunFuture(runnable, delay, cancellationSource);
    }

    @Override
    public Future<Void> runLaterAsync(
            Runnable runnable, Duration delay, Duration period, CancellationSource cancellationSource) {
        return DefaultFutureFactory.getInstance().newRunFuture(runnable, delay, period, cancellationSource);
    }

    @Override
    public Future<Void> runLaterAsync(RunnableWithProgress runnable) {
        return DefaultFutureFactory.getInstance().newRunFuture(runnable);
    }

    @Override
    public Future<Void> runLaterAsync(RunnableWithProgress runnable, CancellationSource cancellationSource) {
        return DefaultFutureFactory.getInstance().newRunFuture(runnable, cancellationSource);
    }

    @Override
    public Future<Void> runLaterAsync(RunnableWithProgress runnable, Duration delay) {
        return DefaultFutureFactory.getInstance().newRunFuture(runnable, delay);
    }

    @Override
    public Future<Void> runLaterAsync(RunnableWithProgress runnable, Duration delay, Duration period) {
        return DefaultFutureFactory.getInstance().newRunFuture(runnable, delay, period);
    }

    @Override
    public Future<Void> runLaterAsync(
            RunnableWithProgress runnable, Duration delay, CancellationSource cancellationSource) {
        return DefaultFutureFactory.getInstance().newRunFuture(runnable, delay, cancellationSource);
    }

    @Override
    public Future<Void> runLaterAsync(
            RunnableWithProgress runnable, Duration delay, Duration period, CancellationSource cancellationSource) {
        return DefaultFutureFactory.getInstance().newRunFuture(runnable, delay, period, cancellationSource);
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier) {
        return DefaultFutureFactory.getInstance().newGetFuture(supplier);
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier, Duration delay) {
        return DefaultFutureFactory.getInstance().newGetFuture(supplier, delay);
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier, CancellationSource cancellationSource) {
        return DefaultFutureFactory.getInstance().newGetFuture(supplier, cancellationSource);
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier, Duration delay, CancellationSource cancellationSource) {
        return DefaultFutureFactory.getInstance().newGetFuture(supplier, delay, cancellationSource);
    }

    @Override
    public <T> Future<T> getLaterAsync(SupplierWithProgress<T> supplier) {
        return DefaultFutureFactory.getInstance().newGetFuture(supplier);
    }

    @Override
    public <T> Future<T> getLaterAsync(SupplierWithProgress<T> supplier, CancellationSource cancellationSource) {
        return DefaultFutureFactory.getInstance().newGetFuture(supplier, cancellationSource);
    }

    @Override
    public <T> Future<T> getLaterAsync(SupplierWithProgress<T> supplier, Duration delay) {
        return DefaultFutureFactory.getInstance().newGetFuture(supplier, delay);
    }

    @Override
    public <T> Future<T> getLaterAsync(
            SupplierWithProgress<T> supplier, Duration delay, CancellationSource cancellationSource) {
        return DefaultFutureFactory.getInstance().newGetFuture(supplier, delay, cancellationSource);
    }

    @Override
    public String toString() {
        return "background thread";
    }
}
