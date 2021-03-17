/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Adds default implementations for various {@link Future} methods that are common to most future classes. */
abstract class AbstractFuture<V> implements Future<V> {

    int getRank() {
        return 1;
    }

    abstract ProgressInfo getProgressInfo();

    @MaybeUnsynchronized
    void notifyProgressListeners(double localProgress) {
        int rank = getRank();
        notifyProgressListeners(localProgress + rank - 1, rank);
    }

    @MaybeUnsynchronized
    void notifyProgressListeners(double cumulativeProgress, int rank) {
        getProgressInfo().notifyListeners(cumulativeProgress, getRank());
    }

    @Override
    public void addProgressListener(ProgressListener listener) {
        getProgressInfo().addListener(listener);
    }

    @Override
    public final V getUnchecked() {
        try {
            return get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final V getUnchecked(long l, TimeUnit timeUnit) {
        try {
            return get(l, timeUnit);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean cancel() {
        return cancel(false);
    }

    @Override
    public Future<Void> thenRun(Runnable runnable) {
        return thenRunAsync(
            () -> {
                try {
                    runnable.run();
                    return DefaultFutureFactory.getInstance().successful(null);
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable throwable) {
                    return DefaultFutureFactory.getInstance().failed(throwable);
                }
            });
    }

    @Override
    public Future<Void> thenRun(RunnableWithProgress runnable) {
        return thenRunAsync(
            cancellationToken -> {
                try {
                    runnable.run(cancellationToken);
                    return DefaultFutureFactory.getInstance().successful(null);
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable throwable) {
                    return DefaultFutureFactory.getInstance().failed(throwable);
                }
            });
    }

    @Override
    public Future<Void> thenFinallyRun(FinallyRunnable runnable) {
        return thenFinallyRunAsync(
            exception -> {
                try {
                    runnable.run(exception);
                    return DefaultFutureFactory.getInstance().successful(null);
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public Future<Void> thenFinallyRun(FinallyRunnableWithProgress runnable) {
        return thenFinallyRunAsync(
            (exception, cancellationToken) -> {
                try {
                    runnable.run(exception, cancellationToken);
                    return DefaultFutureFactory.getInstance().successful(null);
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public Future<Void> thenFinallyRun(Runnable success, Consumer<AggregateException> failure) {
        return thenFinallyRunAsync(
            exception -> {
                try {
                    if (exception == null) {
                        success.run();
                    } else {
                        failure.accept(exception);
                    }

                    return DefaultFutureFactory.getInstance().successful(null);
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public Future<Void> thenFinallyRun(RunnableWithProgress success, ConsumerWithProgress<AggregateException> failure) {
        return thenFinallyRunAsync(
            (exception, cancellationToken) -> {
                try {
                    if (exception == null) {
                        success.run(cancellationToken);
                    } else {
                        failure.accept(exception, cancellationToken);
                    }

                    return DefaultFutureFactory.getInstance().successful(null);
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public <R> Future<R> thenGet(Supplier<R> supplier) {
        return thenGetAsync(
            () -> {
                try {
                    return DefaultFutureFactory.getInstance().successful(supplier.get());
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable throwable) {
                    return DefaultFutureFactory.getInstance().failed(throwable);
                }
            });
    }

    @Override
    public <R> Future<R> thenGet(SupplierWithProgress<R> supplier) {
        return thenGetAsync(
            cancellationToken -> {
                try {
                    return DefaultFutureFactory.getInstance().successful(supplier.get(cancellationToken));
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable throwable) {
                    return DefaultFutureFactory.getInstance().failed(throwable);
                }
            });
    }

    @Override
    public <R> Future<R> thenFinallyGet(FinallySupplier<R> supplier) {
        return thenFinallyGetAsync(
            exception -> {
                try {
                    return DefaultFutureFactory.getInstance().successful(supplier.get(exception));
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public <R> Future<R> thenFinallyGet(FinallySupplierWithProgress<R> supplier) {
        return thenFinallyGetAsync(
            (exception, cancellationToken) -> {
                try {
                    return DefaultFutureFactory.getInstance().successful(supplier.get(exception, cancellationToken));
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public <R> Future<R> thenFinallyGet(Supplier<R> success, Function<AggregateException, R> failure) {
        return thenFinallyGetAsync(
            exception -> {
                try {
                    if (exception == null) {
                        return DefaultFutureFactory.getInstance().successful(success.get());
                    } else {
                        return DefaultFutureFactory.getInstance().successful(failure.apply(exception));
                    }
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public <R> Future<R> thenFinallyGet(
            SupplierWithProgress<R> success, FunctionWithProgress<AggregateException, R> failure) {
        return thenFinallyGetAsync(
            (exception, cancellationToken) -> {
                try {
                    if (exception == null) {
                        return DefaultFutureFactory.getInstance().successful(success.get(cancellationToken));
                    } else {
                        return DefaultFutureFactory.getInstance()
                            .successful(failure.apply(exception, cancellationToken));
                    }
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public Future<Void> thenAccept(Consumer<V> consumer) {
        return thenAcceptAsync(
            value -> {
                try {
                    consumer.accept(value);
                    return DefaultFutureFactory.getInstance().successful(null);
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable throwable) {
                    return DefaultFutureFactory.getInstance().failed(throwable);
                }
            });
    }

    @Override
    public Future<Void> thenAccept(ConsumerWithProgress<V> consumer) {
        return thenAcceptAsync(
            (value, cancellationToken) -> {
                try {
                    consumer.accept(value, cancellationToken);
                    return DefaultFutureFactory.getInstance().successful(null);
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable throwable) {
                    return DefaultFutureFactory.getInstance().failed(throwable);
                }
            });
    }

    @Override
    public Future<Void> thenFinallyAccept(FinallyConsumer<V> consumer) {
        return thenFinallyAcceptAsync(
            (value, exception) -> {
                try {
                    consumer.accept(value, exception);
                    return DefaultFutureFactory.getInstance().successful(null);
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public Future<Void> thenFinallyAccept(FinallyConsumerWithProgress<V> consumer) {
        return thenFinallyAcceptAsync(
            (value, exception, cancellationToken) -> {
                try {
                    consumer.accept(value, exception, cancellationToken);
                    return DefaultFutureFactory.getInstance().successful(null);
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public Future<Void> thenFinallyAccept(Consumer<V> success, Consumer<AggregateException> failure) {
        return thenFinallyAcceptAsync(
            (value, exception) -> {
                try {
                    if (exception == null) {
                        success.accept(value);
                    } else {
                        failure.accept(exception);
                    }

                    return DefaultFutureFactory.getInstance().successful(null);
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public Future<Void> thenFinallyAccept(
            ConsumerWithProgress<V> success, ConsumerWithProgress<AggregateException> failure) {
        return thenFinallyAcceptAsync(
            (value, exception, cancellationToken) -> {
                try {
                    if (exception == null) {
                        success.accept(value, cancellationToken);
                    } else {
                        failure.accept(exception, cancellationToken);
                    }

                    return DefaultFutureFactory.getInstance().successful(null);
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public <R> Future<R> thenApply(Function<V, R> function) {
        return thenApplyAsync(
            value -> {
                try {
                    return DefaultFutureFactory.getInstance().successful(function.apply(value));
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable throwable) {
                    return DefaultFutureFactory.getInstance().failed(throwable);
                }
            });
    }

    @Override
    public <R> Future<R> thenApply(FunctionWithProgress<V, R> function) {
        return thenApplyAsync(
            (value, cancellationToken) -> {
                try {
                    return DefaultFutureFactory.getInstance().successful(function.apply(value, cancellationToken));
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable throwable) {
                    return DefaultFutureFactory.getInstance().failed(throwable);
                }
            });
    }

    @Override
    public <R> Future<R> thenFinallyApply(FinallyFunction<V, R> function) {
        return thenFinallyApplyAsync(
            (value, exception) -> {
                try {
                    return DefaultFutureFactory.getInstance().successful(function.apply(value, exception));
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public <R> Future<R> thenFinallyApply(FinallyFunctionWithProgress<V, R> function) {
        return thenFinallyApplyAsync(
            (value, exception, cancellationToken) -> {
                try {
                    return DefaultFutureFactory.getInstance()
                        .successful(function.apply(value, exception, cancellationToken));
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public <R> Future<R> thenFinallyApply(Function<V, R> success, Function<AggregateException, R> failure) {
        return thenFinallyApplyAsync(
            (value, exception) -> {
                try {
                    if (exception == null) {
                        return DefaultFutureFactory.getInstance().successful(success.apply(value));
                    } else {
                        return DefaultFutureFactory.getInstance().successful(failure.apply(exception));
                    }
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public <R> Future<R> thenFinallyApply(
            FunctionWithProgress<V, R> success, FunctionWithProgress<AggregateException, R> failure) {
        return thenFinallyApplyAsync(
            (value, exception, cancellationToken) -> {
                try {
                    if (exception == null) {
                        return DefaultFutureFactory.getInstance().successful(success.apply(value, cancellationToken));
                    } else {
                        return DefaultFutureFactory.getInstance()
                            .successful(failure.apply(exception, cancellationToken));
                    }
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public Future<Void> thenFinallyRunAsync(FutureRunnable success, FutureConsumer<AggregateException> failure) {
        return thenFinallyRunAsync(
            exception -> {
                try {
                    if (exception == null) {
                        return success.run();
                    } else {
                        return failure.accept(exception);
                    }
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public Future<Void> thenFinallyRunAsync(
            FutureRunnableWithProgress success, FutureConsumerWithProgress<AggregateException> failure) {
        return thenFinallyRunAsync(
            (exception, cancellationToken) -> {
                try {
                    if (exception == null) {
                        return success.run(cancellationToken);
                    } else {
                        return failure.accept(exception, cancellationToken);
                    }
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public <R> Future<R> thenFinallyGetAsync(FutureSupplier<R> success, FutureFunction<AggregateException, R> failure) {
        return thenFinallyGetAsync(
            exception -> {
                try {
                    if (exception == null) {
                        return success.get();
                    } else {
                        return failure.apply(exception);
                    }
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public <R> Future<R> thenFinallyGetAsync(
            FutureSupplierWithProgress<R> success, FutureFunctionWithProgress<AggregateException, R> failure) {
        return thenFinallyGetAsync(
            (exception, cancellationToken) -> {
                try {
                    if (exception == null) {
                        return success.get(cancellationToken);
                    } else {
                        return failure.apply(exception, cancellationToken);
                    }
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public Future<Void> thenFinallyAcceptAsync(FutureConsumer<V> success, FutureConsumer<AggregateException> failure) {
        return thenFinallyAcceptAsync(
            (value, exception) -> {
                try {
                    if (exception == null) {
                        return success.accept(value);
                    } else {
                        return failure.accept(exception);
                    }
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public Future<Void> thenFinallyAcceptAsync(
            FutureConsumerWithProgress<V> success, FutureConsumerWithProgress<AggregateException> failure) {
        return thenFinallyAcceptAsync(
            (value, exception, cancellationToken) -> {
                try {
                    if (exception == null) {
                        return success.accept(value, cancellationToken);
                    } else {
                        return failure.accept(exception, cancellationToken);
                    }
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public <R> Future<R> thenFinallyApplyAsync(
            FutureFunction<V, R> success, FutureFunction<AggregateException, R> failure) {
        return thenFinallyApplyAsync(
            (value, exception) -> {
                try {
                    if (exception == null) {
                        return success.apply(value);
                    } else {
                        return failure.apply(exception);
                    }
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public <R> Future<R> thenFinallyApplyAsync(
            FutureFunctionWithProgress<V, R> success, FutureFunctionWithProgress<AggregateException, R> failure) {
        return thenFinallyApplyAsync(
            (value, exception, cancellationToken) -> {
                try {
                    if (exception == null) {
                        return success.apply(value, cancellationToken);
                    } else {
                        return failure.apply(exception, cancellationToken);
                    }
                } catch (AssertionError assertionError) {
                    throw assertionError;
                } catch (Throwable t) {
                    return DefaultFutureFactory.getInstance().failed(t);
                }
            });
    }

    @Override
    public Future<V> whenDone(Runnable runnable) {
        return whenDone(runnable, MoreExecutors.directExecutor());
    }

    @Override
    public Future<V> whenDone(Consumer<Future<V>> consumer) {
        return whenDone(consumer, MoreExecutors.directExecutor());
    }

    @Override
    public Future<V> whenSucceeded(Runnable runnable) {
        return whenSucceeded(runnable, MoreExecutors.directExecutor());
    }

    @Override
    public Future<V> whenSucceeded(Consumer<V> consumer) {
        return whenSucceeded(consumer, MoreExecutors.directExecutor());
    }

    @Override
    public Future<V> whenFailed(Consumer<AggregateException> consumer) {
        return whenFailed(consumer, MoreExecutors.directExecutor());
    }

    @Override
    public Future<V> whenFailed(Runnable runnable) {
        return whenFailed(runnable, MoreExecutors.directExecutor());
    }

    @Override
    public Future<V> whenCancelled(Runnable runnable) {
        return whenCancelled(runnable, MoreExecutors.directExecutor());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Future<T> cast() {
        return thenFinallyApplyAsync(
            (value, exception) -> {
                if (exception != null) {
                    return Futures.failed(exception);
                }

                return Futures.successful((T)value);
            });
    }

}
