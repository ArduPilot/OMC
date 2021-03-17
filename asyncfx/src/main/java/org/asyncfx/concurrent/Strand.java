/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

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
    private Future currentFuture;

    public Future<Void> runLater(Runnable runnable) {
        return runLaterAsync(() -> Dispatcher.background().runLaterAsync(runnable));
    }

    public <V> Future<V> getLater(Supplier<V> supplier) {
        return getLaterAsync(() -> Dispatcher.background().getLaterAsync(supplier));
    }

    public synchronized Future<Void> runLaterAsync(Future.FutureRunnable futureRunnable) {
        if (currentFuture == null) {
            Future<Void> nextFuture = futureRunnable.run();
            currentFuture = nextFuture;
            nextFuture.whenDone(this::currentFutureDone);
            return nextFuture;
        }

        CompletableFuture<Void> proxyFuture = new CompletableFuture<>();

        postedItems.add(
            previousFuture -> {
                return futureRunnable
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
                        });
            });

        return proxyFuture;
    }

    public synchronized Future<Void> runLaterAsync(Future.FutureFinallyRunnable futureRunnable) {
        if (currentFuture == null) {
            Future<Void> nextFuture = futureRunnable.run(null);
            currentFuture = nextFuture;
            nextFuture.whenDone(this::currentFutureDone);
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
        if (currentFuture == null) {
            Future<V> nextFuture = futureSupplier.get();
            currentFuture = nextFuture;
            nextFuture.whenDone(this::currentFutureDone);
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
        if (currentFuture == null) {
            Future<V> nextFuture = futureSupplier.get(null);
            currentFuture = nextFuture;
            nextFuture.whenDone(this::currentFutureDone);
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
        if (posted == null) {
            currentFuture = null;
            return;
        }

        Future<?> nextFuture = posted.apply(previousFuture);
        while (posted != null && nextFuture.isDone()) {
            posted = this.postedItems.poll();
            if (posted != null) {
                nextFuture = posted.apply(nextFuture);
            }
        }

        currentFuture = nextFuture;
        nextFuture.whenDone(this::currentFutureDone);
    }

}
