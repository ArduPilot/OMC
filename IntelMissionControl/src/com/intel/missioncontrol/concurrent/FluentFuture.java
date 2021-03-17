/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.intel.missioncontrol.common.Expect;
import eu.mavinci.core.obfuscation.IKeepAll;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.util.Pair;

/**
 * FluentFuture is a wrapper around an existing ListenableFuture instance and provides an easier-to-use interface to
 * react to the result of the future. When used with {@link Dispatcher}, it can be used to offload work to a background
 * thread and execute the continuation methods (onFailure, onSuccess, onDone) on the JavaFX application thread (or any
 * other thread via a custom executor).
 */
@SuppressWarnings("UnusedDeclaration")
public class FluentFuture<V> implements ListenableFuture<V>, IKeepAll {

    private static class ContinuationFuture<V, R> extends FluentFuture<V> {

        private final FutureSupplier<R, V> futureSupplier;
        private final ContinuationOption continuationOption;

        ContinuationFuture(FutureSupplier<R, V> futureSupplier, ContinuationOption continuationOption) {
            this.futureSupplier = futureSupplier;
            this.continuationOption = continuationOption;
        }

        @Override
        @SuppressWarnings("unchecked")
        void initialize(ListenableFuture<?> parent) {
            Expect.isTrue(parent instanceof FluentFuture, "parent");

            FluentFuture<R> fluentFuture = (FluentFuture<R>)parent;
            synchronized (resetEvent) {
                super.initialize(futureSupplier.apply(fluentFuture));
            }
        }

        ContinuationOption getContinuationOption() {
            return continuationOption;
        }

    }

    private static final AtomicReferenceFieldUpdater<FluentFuture, Throwable> throwableUpdater =
        AtomicReferenceFieldUpdater.newUpdater(FluentFuture.class, Throwable.class, "throwable");

    private static final AtomicReferenceFieldUpdater<FluentFuture, Throwable[]> precedingThrowablesUpdater =
        AtomicReferenceFieldUpdater.newUpdater(FluentFuture.class, Throwable[].class, "precedingThrowables");

    final ResetEvent resetEvent = new ResetEvent();

    private ListenableFuture<V> nestedFuture;
    private List<ContinuationFuture<?, V>> continuations;
    private List<Pair<Runnable, Executor>> onDoneRunnables;
    private List<Pair<Runnable, Executor>> deferredListeners;
    private boolean cancelled;
    private boolean mayInterruptIfRunning;
    private volatile Throwable throwable;
    private volatile Throwable[] precedingThrowables;

    synchronized ListenableFuture<V> getNestedFuture() {
        if (nestedFuture == null) {
            throw new IllegalStateException("The future is not yet materialized.");
        }

        return nestedFuture;
    }

    @SuppressWarnings("unchecked")
    synchronized void initialize(ListenableFuture<?> future) {
        this.nestedFuture = (ListenableFuture<V>)future;
        this.resetEvent.set();
        future.addListener(this::onNestedFutureCompleted, MoreExecutors.directExecutor());

        if (onDoneRunnables != null) {
            for (Pair<Runnable, Executor> pair : onDoneRunnables) {
                future.addListener(pair.getKey(), pair.getValue());
            }
        }

        if (deferredListeners != null) {
            for (Pair<Runnable, Executor> pair : deferredListeners) {
                future.addListener(pair.getKey(), pair.getValue());
            }
        }
    }

    public static <V> FluentFuture<V> fromResult(V result) {
        return from(Futures.immediateFuture(result));
    }

    public static <V> FluentFuture<V> fromThrowable(Throwable throwable) {
        return from(Futures.immediateFailedFuture(throwable));
    }

    public static <V> FluentFuture<V> fromCancelled() {
        return from(Futures.immediateCancelledFuture());
    }

    /** Creates a FluentFuture by wrapping a ListenableFuture instance. */
    public static <V> FluentFuture<V> from(ListenableFuture<V> future) {
        if (future instanceof FluentFuture) {
            return (FluentFuture<V>)future;
        }

        FluentFuture<V> fluentFuture = new FluentFuture<>();
        fluentFuture.initialize(future);
        return fluentFuture;
    }

    /**
     * Creates a FluentFuture by wrapping multiple ListenableFuture instances. The returned FluentFuture completes when
     * all of the wrapped futures have completed.
     */
    public static FluentFuture<ListenableFuture<?>[]> from(
            ListenableFuture<?> firstFuture, ListenableFuture<?>... moreFutures) {
        if (moreFutures.length == 0) {
            throw new IllegalArgumentException("future");
        }

        var completionFuture =
            new AbstractFuture<ListenableFuture<?>[]>() {
                private AtomicInteger completedCount = new AtomicInteger(0);

                void signalComplete() {
                    if (completedCount.incrementAndGet() == moreFutures.length + 1) {
                        var res = new ListenableFuture<?>[moreFutures.length + 1];
                        res[0] = firstFuture;
                        System.arraycopy(moreFutures, 0, res, 1, moreFutures.length - 1);
                        set(res);
                    }
                }
            };

        firstFuture.addListener(completionFuture::signalComplete, MoreExecutors.directExecutor());

        for (var future : moreFutures) {
            future.addListener(completionFuture::signalComplete, MoreExecutors.directExecutor());
        }

        return from(completionFuture);
    }

    @Override
    public synchronized void addListener(Runnable runnable, Executor executor) {
        if (nestedFuture != null) {
            nestedFuture.addListener(runnable, executor);
        } else {
            if (deferredListeners == null) {
                deferredListeners = new ArrayList<>();
            }

            deferredListeners.add(new Pair<>(runnable, executor));
        }
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (nestedFuture == null) {
            cancelled = true;
            this.mayInterruptIfRunning = mayInterruptIfRunning;
            resetEvent.set();

            if (continuations != null) {
                for (ContinuationFuture<?, V> continuation : continuations) {
                    continuation.setPrecedingThrowables(precedingThrowables);

                    ContinuationOption option = continuation.getContinuationOption();
                    if (option.continueOnCancelled()) {
                        continuation.initialize(this);
                    } else {
                        continuation.cancel(mayInterruptIfRunning);
                    }
                }
            }

            if (onDoneRunnables != null) {
                for (Pair<Runnable, Executor> pair : onDoneRunnables) {
                    pair.getValue().execute(pair.getKey());
                }
            }

            if (deferredListeners != null) {
                for (Pair<Runnable, Executor> pair : deferredListeners) {
                    pair.getValue().execute(pair.getKey());
                }
            }

            return true;
        } else {
            return nestedFuture.cancel(mayInterruptIfRunning);
        }
    }

    @Override
    public synchronized boolean isCancelled() {
        return nestedFuture != null ? nestedFuture.isCancelled() : cancelled;
    }

    @Override
    public synchronized boolean isDone() {
        return isCancelled() || (nestedFuture != null && nestedFuture.isDone());
    }

    public synchronized boolean isSuccess() {
        return isDone() && !isCancelled() && throwable == null;
    }

    public synchronized boolean isFailed() {
        return isDone() && !isCancelled() && throwable != null;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Throwable[] getPrecedingThrowables() {
        return precedingThrowables;
    }

    void setPrecedingThrowables(Throwable[] throwables) {
        if (!precedingThrowablesUpdater.compareAndSet(this, null, throwables)) {
            throw new IllegalStateException("Preceding throwables have already been set.");
        }
    }

    /**
     * Waits until the future has completed and a result is available. If used in conjunction with Dispatcher, deadlock
     * situations resulting from waiting on the JavaFX application thread will be detected and resolved by throwing an
     * exception.
     */
    @Override
    public synchronized V get() throws ExecutionException {
        boolean fxAppThread = Platform.isFxApplicationThread();
        if (fxAppThread && Dispatcher.deadlockAvoidanceFlag.getAndSet(true)) {
            throw new RuntimeException(
                "A deadlock was detected. Avoid calling "
                    + FluentFuture.class.getSimpleName()
                    + "::get() from the JavaFX application thread.");
        }

        try {
            if (nestedFuture == null) {
                resetEvent.await();
                if (nestedFuture == null) {
                    throw new CancellationException();
                }
            }

            return nestedFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (fxAppThread) {
                Dispatcher.deadlockAvoidanceFlag.set(false);
            }
        }
    }

    /**
     * Waits until the future has completed and a result is available, or until a timeout has elapsed. If used in
     * conjunction with Dispatcher, deadlock situations resulting from waiting on the JavaFX application thread will be
     * detected and resolved by throwing an exception.
     */
    @Override
    public synchronized V get(long l, TimeUnit timeUnit) throws ExecutionException, TimeoutException {
        boolean fxAppThread = Platform.isFxApplicationThread();
        if (fxAppThread && Dispatcher.deadlockAvoidanceFlag.getAndSet(true)) {
            throw new RuntimeException(
                "A deadlock was detected. Avoid calling "
                    + FluentFuture.class.getSimpleName()
                    + "::get() from the JavaFX application thread.");
        }

        try {
            if (nestedFuture == null) {
                resetEvent.await();
                if (nestedFuture == null) {
                    throw new CancellationException();
                }
            }

            return nestedFuture.get(l, timeUnit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (fxAppThread) {
                Dispatcher.deadlockAvoidanceFlag.set(false);
            }
        }
    }

    /**
     * Occurs when the future completed without any exceptions. The continuation method will be executed on the same
     * thread the nested ListenableFuture was executed on.
     */
    public FluentFuture<V> onSuccess(Consumer<V> consumer) {
        return onSuccess(consumer, MoreExecutors.directExecutor());
    }

    /** Occurs when the future completed without any exceptions. */
    public FluentFuture<V> onSuccess(Consumer<V> consumer, Executor executor) {
        return onDoneImpl(
            () -> {
                if (isCancelled()) {
                    return;
                }

                try {
                    V value = Futures.getDone(nestedFuture);
                    executor.execute(() -> consumer.accept(value));
                } catch (ExecutionException e) {
                }
            },
            executor);
    }

    /**
     * Occurs when the future completed by throwing an exception. The continuation method will be executed on the same
     * thread the nested ListenableFuture was executed on.
     */
    public FluentFuture<V> onFailure(Consumer<ExecutionException> consumer) {
        return onFailure(consumer, MoreExecutors.directExecutor());
    }

    /** Occurs when the future completed by throwing an exception. */
    public FluentFuture<V> onFailure(Consumer<ExecutionException> consumer, Executor executor) {
        return onDoneImpl(
            () -> {
                if (isCancelled()) {
                    return;
                }

                try {
                    Futures.getDone(nestedFuture);
                } catch (ExecutionException e) {
                    executor.execute(() -> consumer.accept(e));
                }
            },
            executor);
    }

    /**
     * Occurs when the future completed either successfully, by throwing an exception, or by being cancelled. The
     * continuation method will be executed on the same thread the nested ListenableFuture was executed on.
     */
    public FluentFuture<V> onDone(Consumer<FluentFuture<V>> consumer) {
        return onDone(consumer, MoreExecutors.directExecutor());
    }

    /** Occurs when the future completed either successfully, by throwing an exception, or by being cancelled. */
    public FluentFuture<V> onDone(Consumer<FluentFuture<V>> consumer, Executor executor) {
        return onDoneImpl(() -> executor.execute(() -> consumer.accept(this)), executor);
    }

    @SuppressWarnings("unchecked")
    public <U> V unwrap() {
        return (V)unwrap((FluentFuture<? extends FluentFuture<U>>)this);
    }

    private <U> FluentFuture<U> unwrap(FluentFuture<? extends FluentFuture<U>> wrappedFuture) {
        var proxyFuture = SettableFuture.<U>create();
        onDoneImpl(
            () -> {
                try {
                    Consumer<FluentFuture<U>> consumer =
                        future -> {
                            try {
                                proxyFuture.set(Futures.getDone(future));
                            } catch (ExecutionException e) {
                                proxyFuture.setException(e.getCause());
                            }
                        };

                    Futures.getDone(wrappedFuture).onDone(consumer);
                } catch (ExecutionException e) {
                    proxyFuture.setException(e.getCause());
                }
            },
            MoreExecutors.directExecutor());

        return from(proxyFuture);
    }

    /** Continues this operation with another operation. */
    public <R> FluentFuture<R> continueWith(FutureSupplier<V, R> futureSupplier) {
        return continueWith(futureSupplier, ContinuationOption.ALWAYS);
    }

    /** Continues this operation with another operation. */
    public synchronized <R> FluentFuture<R> continueWith(
            FutureSupplier<V, R> futureSupplier, ContinuationOption continuationOption) {
        Expect.notNull(futureSupplier, "futureSupplier", continuationOption, "continuationOption");

        if (continuations == null) {
            continuations = new ArrayList<>();
        }

        ContinuationFuture<R, V> future = new ContinuationFuture<>(futureSupplier, continuationOption);
        if (isCancelled() && !continuationOption.continueOnCancelled()) {
            return future;
        }

        if (!isDone()) {
            continuations.add(future);
        } else {
            if (continuationOption.continueOnSuccess() && throwable == null
                    || continuationOption.continueOnFailure() && throwable != null
                    || continuationOption.continueOnCancelled() && isCancelled()) {
                future.initialize(this);
            } else {
                future.cancel(mayInterruptIfRunning);
            }
        }

        return future;
    }

    private synchronized FluentFuture<V> onDoneImpl(Runnable runnable, Executor executor) {
        Expect.notNull(runnable, "runnable", executor, "executor");

        if (isCancelled()) {
            executor.execute(runnable);
        } else if (nestedFuture == null) {
            if (onDoneRunnables == null) {
                onDoneRunnables = new ArrayList<>();
            }

            onDoneRunnables.add(new Pair<>(runnable, executor));
        } else if (nestedFuture.isDone()) {
            executor.execute(runnable);
        } else {
            nestedFuture.addListener(runnable, executor);
        }

        return this;
    }

    private synchronized void onNestedFutureCompleted() {
        if (nestedFuture.isCancelled()) {
            if (continuations != null) {
                for (ContinuationFuture<?, V> continuation : continuations) {
                    continuation.setPrecedingThrowables(precedingThrowables);

                    if (continuation.getContinuationOption().continueOnCancelled()) {
                        continuation.initialize(this);
                    } else {
                        continuation.cancel(mayInterruptIfRunning);
                    }
                }
            }
        } else {
            try {
                nestedFuture.get();
            } catch (ExecutionException e) {
                throwableUpdater.set(this, e.getCause());
            } catch (InterruptedException e) {
                throwableUpdater.set(this, e);
            } finally {
                if (throwable != null) {
                    if (precedingThrowables == null) {
                        precedingThrowablesUpdater.set(this, new Throwable[1]);
                    } else {
                        precedingThrowablesUpdater.getAndUpdate(this, p -> Arrays.copyOf(p, p.length + 1));
                    }

                    precedingThrowables[precedingThrowables.length - 1] = throwable;
                }

                if (continuations != null) {
                    for (ContinuationFuture<?, V> continuation : continuations) {
                        continuation.setPrecedingThrowables(precedingThrowables);

                        ContinuationOption option = continuation.getContinuationOption();
                        if (option.continueOnSuccess() && throwable == null
                                || option.continueOnFailure() && throwable != null
                                || option.continueOnCancelled() && isCancelled()) {
                            continuation.initialize(this);
                        } else {
                            continuation.cancel(mayInterruptIfRunning);
                        }
                    }
                }
            }
        }
    }

}
