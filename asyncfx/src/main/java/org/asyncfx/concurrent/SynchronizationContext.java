/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.concurrent.Executor;
import java.util.function.Supplier;
import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the context of a thread of execution. For example, if the getCurrent() method is called on the JavaFX
 * application thread, the returned synchronization context represents the JavaFX application thread and can be used to
 * execute code on it. On the other hand, if the getCurrent() method is called on a background thread, the returned
 * synchronization context does not represent exactly this thread, but any available background thread. This class can
 * be used to capture the context of the current thread and run code in this context without depending on a specific
 * code dispatching implementation.
 */
public abstract class SynchronizationContext implements Executor {

    /** Synchronization context for threads that do not provide their own implementation. */
    private static class DefaultSynchronizationContext extends SynchronizationContext {
        @Override
        public Future<Void> runLaterAsync(Runnable runnable) {
            return Dispatcher.background().runLaterAsync(runnable);
        }

        @Override
        public <T> Future<T> getLaterAsync(Supplier<T> supplier) {
            return Dispatcher.background().getLaterAsync(supplier);
        }

        @Override
        public Future<Void> runAsync(Runnable runnable) {
            if (hasAccess()) {
                try {
                    runnable.run();
                    return Futures.successful();
                } catch (Throwable throwable) {
                    return Futures.failed(throwable);
                }
            }

            return Dispatcher.background().runLaterAsync(runnable);
        }

        @Override
        public <V> Future<V> getAsync(Supplier<V> supplier) {
            if (hasAccess()) {
                try {
                    return Futures.successful(supplier.get());
                } catch (Throwable throwable) {
                    return Futures.failed(throwable);
                }
            }

            return Dispatcher.background().getLaterAsync(supplier);
        }

        @Override
        public boolean hasAccess() {
            return FutureExecutorService.getInstance().isExecutorThread();
        }

        @Override
        public String toString() {
            return "background thread";
        }
    }

    /** Synchronization context for the JavaFX application thread. */
    private static class FxApplicationThreadSynchronizationContext extends SynchronizationContext {
        @Override
        public Future<Void> runLaterAsync(Runnable runnable) {
            return Dispatcher.platform().runLaterAsync(runnable);
        }

        @Override
        public <T> Future<T> getLaterAsync(Supplier<T> supplier) {
            return Dispatcher.platform().getLaterAsync(supplier);
        }

        @Override
        public Future<Void> runAsync(Runnable runnable) {
            if (Platform.isFxApplicationThread()) {
                try {
                    runnable.run();
                    return Futures.successful(null);
                } catch (Throwable e) {
                    return Futures.failed(e);
                }
            }

            return Dispatcher.platform().runLaterAsync(runnable);
        }

        @Override
        public <V> Future<V> getAsync(Supplier<V> supplier) {
            if (Platform.isFxApplicationThread()) {
                try {
                    return Futures.successful(supplier.get());
                } catch (Throwable e) {
                    return Futures.failed(e);
                }
            }

            return Dispatcher.background().getLaterAsync(supplier);
        }

        @Override
        public boolean hasAccess() {
            return Platform.isFxApplicationThread();
        }

        @Override
        public String toString() {
            return "JavaFX application thread";
        }
    }

    private static final ThreadLocal<SynchronizationContext> synchronizationContext =
        ThreadLocal.withInitial(
            () -> {
                if (Platform.isFxApplicationThread()) {
                    return new FxApplicationThreadSynchronizationContext();
                }

                return new DefaultSynchronizationContext();
            });

    /** Gets the synchronization context of the current thread. */
    public static SynchronizationContext getCurrent() {
        return synchronizationContext.get();
    }

    /** Sets the synchronization context for the current thread. */
    public static void setSynchronizationContext(SynchronizationContext synchronizationContext) {
        SynchronizationContext.synchronizationContext.set(synchronizationContext);
    }

    /**
     * Defers execution of the specified runnable to the thread that this synchronization context is associated with and
     * returns immediately.
     */
    public abstract Future<Void> runLaterAsync(Runnable runnable);

    /**
     * Defers execution of the specified callable to the thread that this synchronization context is associated with and
     * returns immediately.
     */
    public abstract <V> Future<V> getLaterAsync(Supplier<V> supplier);

    /**
     * Defers execution of the specified runnable, or executes the runnable directly if invoked from the thread that
     * this synchronization context is associated with.
     */
    public abstract Future<Void> runAsync(Runnable runnable);

    /**
     * Defers execution of the specified callable, or executes the callable directly if invoked from the thread that
     * this synchronization context is associated with.
     */
    public abstract <V> Future<V> getAsync(Supplier<V> supplier);

    /** Returns whether the calling thread is associated with this synchronization context. */
    public abstract boolean hasAccess();

    public final void verifyAccess() {
        if (!hasAccess()) {
            throw new IllegalStateException(
                "Illegal cross-thread access: expected = "
                    + toString()
                    + "; currentThread = "
                    + Thread.currentThread().getName()
                    + ".");
        }
    }

    /**
     * This is equivalent to calling SynchronizationContext::dispatch and is provided to satisfy the requirements of the
     * {@link Executor} interface.
     */
    @Override
    public final void execute(@NotNull Runnable command) {
        runAsync(command);
    }

}
