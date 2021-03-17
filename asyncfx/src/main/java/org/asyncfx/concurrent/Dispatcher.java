/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.asyncfx.concurrent.Future.RunnableWithProgress;
import org.asyncfx.concurrent.Future.SupplierWithProgress;

public interface Dispatcher extends Executor {

    /** Returns a dispatcher that executes code on the JavaFX application thread. */
    static Dispatcher platform() {
        return PlatformDispatcher.INSTANCE;
    }

    /** Returns a dispatcher that executes code on a background thread pool. */
    static Dispatcher background() {
        return BackgroundDispatcher.INSTANCE;
    }

    static Dispatcher fromThread(Thread thread) {
        if (thread == PlatformDispatcher.THREAD) {
            return PlatformDispatcher.INSTANCE;
        }

        if (thread instanceof FutureExecutorService.ExecutorThread) {
            return BackgroundDispatcher.INSTANCE;
        }

        return null;
    }

    boolean hasAccess();

    default void verifyAccess() {
        if (!hasAccess()) {
            throw new IllegalStateException(
                "Illegal cross-thread access: expected = "
                    + toString()
                    + "; currentThread = "
                    + Thread.currentThread());
        }
    }

    /**
     * Executes the given {@link Runnable} synchronously if the current thread has access to the dispatcher, or
     * asynchronously if the current thread does not have access to the dispatcher.
     */
    void run(Runnable runnable);

    /** Executes the given {@link Runnable} asynchronously and returns immediately. */
    void runLater(Runnable runnable);

    /** Executes the given {@link Runnable} asynchronously after a specified delay, and returns immediately. */
    void runLater(Runnable runnable, Duration delay);

    /** Returns a future that is executed asynchronously by calling the given {@link Runnable}. */
    Future<Void> runLaterAsync(Runnable runnable);

    /**
     * Returns a future that is executed asynchronously after a specified delay by calling the given {@link Runnable}.
     */
    Future<Void> runLaterAsync(Runnable runnable, Duration delay);

    /**
     * Returns a future that is executed asynchronously and periodically after a specified delay by calling the given
     * {@link Runnable}.
     */
    Future<Void> runLaterAsync(Runnable runnable, Duration delay, Duration period);

    /** Returns a future that is executed asynchronously by calling the given {@link Runnable}. */
    Future<Void> runLaterAsync(Runnable runnable, CancellationSource cancellationSource);

    /**
     * Returns a future that is executed asynchronously after a specified delay by calling the given {@link Runnable}.
     */
    Future<Void> runLaterAsync(Runnable runnable, Duration delay, CancellationSource cancellationSource);

    /**
     * Returns a future that is executed asynchronously and periodically after a specified delay by calling the given
     * {@link Runnable}.
     */
    Future<Void> runLaterAsync(
            Runnable runnable, Duration delay, Duration period, CancellationSource cancellationSource);

    /** Returns a future that is executed asynchronously by calling the given {@link RunnableWithProgress}. */
    Future<Void> runLaterAsync(RunnableWithProgress runnable);

    /**
     * Returns a future that is executed asynchronously after a specified delay by calling the given {@link
     * RunnableWithProgress}.
     */
    Future<Void> runLaterAsync(RunnableWithProgress runnable, Duration delay);

    /**
     * Returns a future that is executed asynchronously and periodically after a specified delay by calling the given
     * {@link RunnableWithProgress}.
     */
    Future<Void> runLaterAsync(RunnableWithProgress runnable, Duration delay, Duration period);

    /** Returns a future that is executed asynchronously by calling the given {@link RunnableWithProgress}. */
    Future<Void> runLaterAsync(RunnableWithProgress runnable, CancellationSource cancellationSource);

    /**
     * Returns a future that is executed asynchronously after a specified delay by calling the given {@link
     * RunnableWithProgress}.
     */
    Future<Void> runLaterAsync(RunnableWithProgress runnable, Duration delay, CancellationSource cancellationSource);

    /**
     * Returns a future that is executed asynchronously and periodically after a specified delay by calling the given
     * {@link RunnableWithProgress}.
     */
    Future<Void> runLaterAsync(
            RunnableWithProgress runnable, Duration delay, Duration period, CancellationSource cancellationSource);

    /** Returns a future that is executed asynchronously by calling the given {@link Supplier}. */
    <T> Future<T> getLaterAsync(Supplier<T> supplier);

    /**
     * Returns a future that is executed asynchronously after a specified delay by calling the given {@link Supplier}.
     */
    <T> Future<T> getLaterAsync(Supplier<T> supplier, Duration delay);

    /** Returns a future that is executed asynchronously by calling the given {@link Supplier}. */
    <T> Future<T> getLaterAsync(Supplier<T> supplier, CancellationSource cancellationSource);

    /**
     * Returns a future that is executed asynchronously after a specified delay by calling the given {@link Supplier}.
     */
    <T> Future<T> getLaterAsync(Supplier<T> supplier, Duration delay, CancellationSource cancellationSource);

    /** Returns a future that is executed asynchronously by calling the given {@link SupplierWithProgress}. */
    <T> Future<T> getLaterAsync(SupplierWithProgress<T> supplier);

    /**
     * Returns a future that is executed asynchronously after a specified delay by calling the given {@link
     * SupplierWithProgress}.
     */
    <T> Future<T> getLaterAsync(SupplierWithProgress<T> supplier, CancellationSource cancellationSource);

    /**
     * Returns a future that is executed asynchronously after a specified delay by calling the given {@link
     * SupplierWithProgress}.
     */
    <T> Future<T> getLaterAsync(SupplierWithProgress<T> supplier, Duration delay);

    /**
     * Returns a future that is executed asynchronously after a specified delay by calling the given {@link
     * SupplierWithProgress}.
     */
    <T> Future<T> getLaterAsync(
            SupplierWithProgress<T> supplier, Duration delay, CancellationSource cancellationSource);

}
