/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import gov.nasa.worldwind.javafx.WWGLNode;
import java.time.Duration;
import java.util.function.Supplier;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;
import org.jetbrains.annotations.NotNull;

public class WWDispatcher implements Dispatcher {

    private WWGLNode worldWindNode;

    public void setWWNode(WWGLNode worldWindNode) {
        this.worldWindNode = worldWindNode;
    }

    @Override
    public boolean hasAccess() {
        return worldWindNode != null && Thread.currentThread() == worldWindNode.getRenderThread();
    }

    @Override
    public void run(Runnable runnable) {
        if (hasAccess()) {
            runnable.run();
        } else {
            worldWindNode.accept(runnable);
        }
    }

    @Override
    public void runLater(Runnable runnable) {
        worldWindNode.accept(runnable);
    }

    @Override
    public void runLater(Runnable runnable, Duration delay) {
        Dispatcher.background().runLaterAsync(() -> {}, delay).thenRun(runnable);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable) {
        FutureCompletionSource<Void> futureCompletionSource = new FutureCompletionSource<>();
        worldWindNode.accept(
            () -> {
                runnable.run();
                futureCompletionSource.setResult(null);
            });

        return futureCompletionSource.getFuture();
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, Duration delay) {
        FutureCompletionSource<Void> futureCompletionSource = new FutureCompletionSource<>();
        Dispatcher.background()
            .runLaterAsync(
                () ->
                    worldWindNode.accept(
                        () -> {
                            runnable.run();
                            futureCompletionSource.setResult(null);
                        }),
                delay);

        return futureCompletionSource.getFuture();
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, Duration delay, Duration period) {
        return Dispatcher.background().runLaterAsync(() -> worldWindNode.accept(runnable), delay, period);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, CancellationSource cancellationSource) {
        FutureCompletionSource<Void> futureCompletionSource = new FutureCompletionSource<>(cancellationSource);
        worldWindNode.accept(
            () -> {
                runnable.run();
                futureCompletionSource.setResult(null);
            });

        return futureCompletionSource.getFuture();
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, Duration delay, CancellationSource cancellationSource) {
        FutureCompletionSource<Void> futureCompletionSource = new FutureCompletionSource<>(cancellationSource);
        Dispatcher.background()
            .runLaterAsync(
                () ->
                    worldWindNode.accept(
                        () -> {
                            runnable.run();
                            futureCompletionSource.setResult(null);
                        }),
                delay,
                cancellationSource);

        return futureCompletionSource.getFuture();
    }

    @Override
    public Future<Void> runLaterAsync(
            Runnable runnable, Duration delay, Duration period, CancellationSource cancellationSource) {
        return Dispatcher.background()
            .runLaterAsync(() -> worldWindNode.accept(runnable), delay, period, cancellationSource);
    }

    @Override
    public Future<Void> runLaterAsync(Future.RunnableWithProgress runnable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<Void> runLaterAsync(Future.RunnableWithProgress runnable, Duration delay) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<Void> runLaterAsync(Future.RunnableWithProgress runnable, Duration delay, Duration period) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<Void> runLaterAsync(Future.RunnableWithProgress runnable, CancellationSource cancellationSource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<Void> runLaterAsync(
            Future.RunnableWithProgress runnable, Duration delay, CancellationSource cancellationSource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<Void> runLaterAsync(
            Future.RunnableWithProgress runnable,
            Duration delay,
            Duration period,
            CancellationSource cancellationSource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier) {
        FutureCompletionSource<T> futureCompletionSource = new FutureCompletionSource<>();
        worldWindNode.accept(() -> futureCompletionSource.setResult(supplier.get()));
        return futureCompletionSource.getFuture();
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier, Duration delay) {
        FutureCompletionSource<T> futureCompletionSource = new FutureCompletionSource<>();
        Dispatcher.background()
            .runLaterAsync(() -> {}, delay)
            .thenRun(() -> worldWindNode.accept(() -> futureCompletionSource.setResult(supplier.get())));
        return futureCompletionSource.getFuture();
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier, CancellationSource cancellationSource) {
        FutureCompletionSource<T> futureCompletionSource = new FutureCompletionSource<>(cancellationSource);
        worldWindNode.accept(() -> futureCompletionSource.setResult(supplier.get()));
        return futureCompletionSource.getFuture();
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier, Duration delay, CancellationSource cancellationSource) {
        FutureCompletionSource<T> futureCompletionSource = new FutureCompletionSource<>(cancellationSource);
        Dispatcher.background()
            .runLaterAsync(() -> {}, delay, cancellationSource)
            .thenRun(() -> worldWindNode.accept(() -> futureCompletionSource.setResult(supplier.get())));
        return futureCompletionSource.getFuture();
    }

    @Override
    public <T> Future<T> getLaterAsync(Future.SupplierWithProgress<T> supplier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Future<T> getLaterAsync(Future.SupplierWithProgress<T> supplier, CancellationSource cancellationSource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Future<T> getLaterAsync(Future.SupplierWithProgress<T> supplier, Duration delay) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Future<T> getLaterAsync(
            Future.SupplierWithProgress<T> supplier, Duration delay, CancellationSource cancellationSource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(@NotNull Runnable command) {
        run(command);
    }

    @Override
    public String toString() {
        return "WorldWind rendering thread";
    }

}
