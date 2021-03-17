/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents the context of a thread of execution. For example, if the getCurrent() method is called on the JavaFX
 * application thread, the returned synchronization context represents the JavaFX application thread and can be used to
 * execute code on it. On the other hand, if the getCurrent() method is called on a background thread, the returned
 * synchronization context does not represent exactly this thread, but any available background thread. This class can
 * be used to capture the context of the current thread and run code in this context without depending on a specific
 * code dispatching implementation.
 */
public abstract class SynchronizationContext implements ListenableExecutor {

    private static Logger LOGGER = LogManager.getLogger(SynchronizationContext.class);

    /** Synchronization context for threads that do not provide their own implementation. */
    static class DefaultSynchronizationContext extends SynchronizationContext {
        @Override
        public void run(Runnable runnable) {
            runnable.run();
        }

        @Override
        public <T> T run(Callable<T> callable) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public FluentFuture<Void> post(Runnable runnable) {
            return Dispatcher.post(runnable);
        }

        @Override
        public <T> FluentFuture<T> post(Callable<T> callable) {
            return Dispatcher.post(callable);
        }

        @Override
        public FluentFuture<Void> dispatch(Runnable runnable) {
            try {
                if (Platform.isFxApplicationThread()) {
                    return Dispatcher.post(runnable);
                }

                runnable.run();
                return FluentFuture.fromResult(null);
            } catch (Throwable e) {
                LOGGER.debug("Exception in async execution:", e);
                return FluentFuture.fromThrowable(e);
            }
        }

        @Override
        public <V> FluentFuture<V> dispatch(Callable<V> callable) {
            try {
                if (Platform.isFxApplicationThread()) {
                    return Dispatcher.post(callable);
                }

                return FluentFuture.fromResult(callable.call());
            } catch (Throwable e) {
                LOGGER.debug("Exception in async execution:", e);
                return FluentFuture.fromThrowable(e);
            }
        }

        @Override
        public boolean hasAccess() {
            return true;
        }

        @Override
        public String toString() {
            return "background thread";
        }
    }

    /** Synchronization context for the JavaFX application thread. */
    static class FxApplicationThreadSynchronizationContext extends SynchronizationContext {
        @Override
        public void run(Runnable runnable) {
            Dispatcher.runOnUI(runnable);
        }

        @Override
        public <T> T run(Callable<T> callable) {
            return Dispatcher.runOnUI(callable);
        }

        @Override
        public FluentFuture<Void> post(Runnable runnable) {
            return Dispatcher.postToUI(runnable);
        }

        @Override
        public <T> FluentFuture<T> post(Callable<T> callable) {
            return Dispatcher.postToUI(callable);
        }

        @Override
        public FluentFuture<Void> dispatch(Runnable runnable) {
            if (Platform.isFxApplicationThread()) {
                try {
                    runnable.run();
                    return FluentFuture.fromResult(null);
                } catch (Throwable e) {
                    LOGGER.debug("Exception in async execution:", e);
                    return FluentFuture.fromThrowable(e);
                }
            }

            return Dispatcher.postToUI(runnable);
        }

        @Override
        public <V> FluentFuture<V> dispatch(Callable<V> callable) {
            if (Platform.isFxApplicationThread()) {
                try {
                    return FluentFuture.fromResult(callable.call());
                } catch (Throwable e) {
                    LOGGER.debug("Exception in async execution:", e);
                    return FluentFuture.fromThrowable(e);
                }
            }

            return Dispatcher.postToUI(callable);
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
     * waits for the operation to finish.
     */
    public abstract void run(Runnable runnable);

    /**
     * Defers execution of the specified callable to the thread that this synchronization context is associated with and
     * waits for the operation to finish.
     */
    public abstract <T> T run(Callable<T> callable);

    /**
     * Defers execution of the specified runnable to the thread that this synchronization context is associated with and
     * returns immediately.
     */
    public abstract FluentFuture<Void> post(Runnable runnable);

    /**
     * Defers execution of the specified callable to the thread that this synchronization context is associated with and
     * returns immediately.
     */
    public abstract <V> FluentFuture<V> post(Callable<V> runnable);

    /**
     * Defers execution of the specified runnable, or executes the runnable directly if invoked from the thread that
     * this synchronization context is associated with.
     */
    public abstract FluentFuture<Void> dispatch(Runnable runnable);

    /**
     * Defers execution of the specified callable, or executes the callable directly if invoked from the thread that
     * this synchronization context is associated with.
     */
    public abstract <V> FluentFuture<V> dispatch(Callable<V> callable);

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
    public final void execute(@NonNull Runnable command) {
        dispatch(command);
    }

    /**
     * This is equivalent to calling SynchronizationContext::dispatch and is provided to satisfy the requirements of the
     * {@link ListenableExecutor} interface.
     */
    @Override
    public ListenableFuture<Void> executeListen(Runnable command) {
        return dispatch(command);
    }

}
