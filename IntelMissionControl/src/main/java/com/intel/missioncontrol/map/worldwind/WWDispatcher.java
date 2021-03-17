/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import gov.nasa.worldwind.javafx.WWGLNode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.FutureCompletionSource;

public class WWDispatcher implements Dispatcher {

    private List<Runnable> deferredActions = new ArrayList<>();
    private WWGLNode worldWindNode;

    private final Consumer<Runnable> worldWindNodeWrapper =
        new Consumer<>() {
            @Override
            public synchronized void accept(Runnable runnable) {
                if (worldWindNode == null) {
                    deferredActions.add(runnable);
                } else {
                    worldWindNode.accept(runnable, false);
                }
            }
        };

    public void setWWNode(WWGLNode worldWindNode) {
        synchronized (worldWindNodeWrapper) {
            this.worldWindNode = worldWindNode;

            for (Runnable runnable : deferredActions) {
                worldWindNode.accept(runnable, false);
            }

            deferredActions = null;
        }
    }

    @Override
    public boolean hasAccess() {
        synchronized (worldWindNodeWrapper) {
            return worldWindNode != null && Thread.currentThread() == worldWindNode.getRenderThread();
        }
    }

    @Override
    public boolean isSequential() {
        return true;
    }

    @Override
    public void run(Runnable runnable) {
        if (hasAccess()) {
            runnable.run();
        } else {
            worldWindNodeWrapper.accept(runnable);
        }
    }

    @Override
    public void runLater(Runnable runnable) {
        worldWindNodeWrapper.accept(runnable);
    }

    @Override
    public void runLater(Runnable runnable, Duration delay) {
        Dispatcher.background().runLaterAsync(() -> {}, delay).thenRun(runnable);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable) {
        FutureCompletionSource<Void> futureCompletionSource = new FutureCompletionSource<>();
        worldWindNodeWrapper.accept(
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
                    worldWindNodeWrapper.accept(
                        () -> {
                            runnable.run();
                            futureCompletionSource.setResult(null);
                        }),
                delay);

        return futureCompletionSource.getFuture();
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, Duration delay, Duration period) {
        return Dispatcher.background().runLaterAsync(() -> worldWindNodeWrapper.accept(runnable), delay, period);
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable, CancellationSource cancellationSource) {
        FutureCompletionSource<Void> futureCompletionSource = new FutureCompletionSource<>(cancellationSource);
        worldWindNodeWrapper.accept(
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
                    worldWindNodeWrapper.accept(
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
            .runLaterAsync(() -> worldWindNodeWrapper.accept(runnable), delay, period, cancellationSource);
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
        worldWindNodeWrapper.accept(() -> futureCompletionSource.setResult(supplier.get()));
        return futureCompletionSource.getFuture();
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier, Duration delay) {
        FutureCompletionSource<T> futureCompletionSource = new FutureCompletionSource<>();
        Dispatcher.background()
            .runLaterAsync(() -> {}, delay)
            .thenRun(() -> worldWindNodeWrapper.accept(() -> futureCompletionSource.setResult(supplier.get())));
        return futureCompletionSource.getFuture();
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier, CancellationSource cancellationSource) {
        FutureCompletionSource<T> futureCompletionSource = new FutureCompletionSource<>(cancellationSource);
        worldWindNodeWrapper.accept(() -> futureCompletionSource.setResult(supplier.get()));
        return futureCompletionSource.getFuture();
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier, Duration delay, CancellationSource cancellationSource) {
        FutureCompletionSource<T> futureCompletionSource = new FutureCompletionSource<>(cancellationSource);
        Dispatcher.background()
            .runLaterAsync(() -> {}, delay, cancellationSource)
            .thenRun(() -> worldWindNodeWrapper.accept(() -> futureCompletionSource.setResult(supplier.get())));
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
    public String toString() {
        return "WorldWind rendering thread";
    }

}
