/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a serialized stream of operations. Operations posted through this strand are guaranteed to not run
 * concurrently. They will be executed serially in the order they were posted.
 */
public final class Strand {

    private final Queue<Function<Future, Future>> postedItems = new ArrayDeque<>();
    private WeakReference<Future> currentFuture;

    public Future<Void> runLater(Runnable runnable) {
        return runLaterAsync(() -> Dispatcher.background().runLaterAsync(runnable));
    }

    public <V> Future<V> getLater(Supplier<V> supplier) {
        return getLaterAsync(() -> Dispatcher.background().getLaterAsync(supplier));
    }

    public synchronized Future<Void> runLaterAsync(Future.FutureRunnable futureRunnable) {
        Future<?> currentFuture = this.currentFuture != null ? this.currentFuture.get() : null;
        if (currentFuture == null) {
            Future<Void> nextFuture = futureRunnable.run();
            nextFuture.whenDone(this::currentFutureDone);
            this.currentFuture = new WeakReference<>(nextFuture);
            return nextFuture;
        }

        CompletableFuture<Void> proxyFuture = new CompletableFuture<>();

        postedItems.add(
            previousFuture ->
                futureRunnable
                    .run()
                    .whenDone(
                        f -> {
                            if (f.isSuccess()) {
                                proxyFuture.completeWithResult(f.getUnchecked());
                            } else if (f.isFailed()) {
                                proxyFuture.completeWithException(f.getException());
                            } else {
                                proxyFuture.completeWithCancellation(f.getException());
                            }
                        }));

        return proxyFuture;
    }

    public synchronized Future<Void> runLaterAsync(Future.FutureFinallyRunnable futureRunnable) {
        Future<?> currentFuture = this.currentFuture != null ? this.currentFuture.get() : null;
        if (currentFuture == null) {
            Future<Void> nextFuture = futureRunnable.run(null);
            nextFuture.whenDone(this::currentFutureDone);
            this.currentFuture = new WeakReference<>(nextFuture);
            return nextFuture;
        }

        CompletableFuture<Void> proxyFuture = new CompletableFuture<>();

        postedItems.add(
            previousFuture ->
                futureRunnable
                    .run(previousFuture.getException())
                    .whenDone(
                        f -> {
                            if (f.isSuccess()) {
                                proxyFuture.completeWithResult(f.getUnchecked());
                            } else if (f.isFailed()) {
                                proxyFuture.completeWithException(f.getException());
                            } else {
                                proxyFuture.completeWithCancellation(f.getException());
                            }
                        }));

        return proxyFuture;
    }

    public synchronized <V> Future<V> getLaterAsync(Future.FutureSupplier<V> futureSupplier) {
        Future<?> currentFuture = this.currentFuture != null ? this.currentFuture.get() : null;
        if (currentFuture == null) {
            Future<V> nextFuture = futureSupplier.get();
            nextFuture.whenDone(this::currentFutureDone);
            this.currentFuture = new WeakReference<>(nextFuture);
            return nextFuture;
        }

        CompletableFuture<V> proxyFuture = new CompletableFuture<>();

        postedItems.add(
            previousFuture ->
                futureSupplier
                    .get()
                    .whenDone(
                        f -> {
                            if (f.isSuccess()) {
                                proxyFuture.completeWithResult(f.getUnchecked());
                            } else if (f.isFailed()) {
                                proxyFuture.completeWithException(f.getException());
                            } else {
                                proxyFuture.completeWithCancellation(f.getException());
                            }
                        }));

        return proxyFuture;
    }

    public synchronized <V> Future<V> getLaterAsync(Future.FutureFinallySupplier<V> futureSupplier) {
        Future<?> currentFuture = this.currentFuture != null ? this.currentFuture.get() : null;
        if (currentFuture == null) {
            Future<V> nextFuture = futureSupplier.get(null);
            nextFuture.whenDone(this::currentFutureDone);
            this.currentFuture = new WeakReference<>(nextFuture);
            return nextFuture;
        }

        CompletableFuture<V> proxyFuture = new CompletableFuture<>();

        postedItems.add(
            previousFuture ->
                futureSupplier
                    .get(previousFuture.getException())
                    .whenDone(
                        f -> {
                            if (f.isSuccess()) {
                                proxyFuture.completeWithResult(f.getUnchecked());
                            } else if (f.isFailed()) {
                                proxyFuture.completeWithException(f.getException());
                            } else {
                                proxyFuture.completeWithCancellation(f.getException());
                            }
                        }));

        return proxyFuture;
    }

    public synchronized int getScheduledCount() {
        return postedItems.size();
    }

    private synchronized void currentFutureDone(Future previousFuture) {
        Function<Future, Future> posted = this.postedItems.poll();
        if (posted != null) {
            Future<?> nextFuture = posted.apply(previousFuture);
            nextFuture.whenDone(this::currentFutureDone);
            currentFuture = new WeakReference<>(nextFuture);
        }
    }

}
