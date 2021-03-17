/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

/**
 * {@link Future} represents the result of an asynchronous operation.
 *
 * <p>Futures can be daisy-chained by calling one of the following continuation methods:
 *
 * <ul>
 *   <li>thenRun (no argument, no return value)
 *   <li>thenGet (no argument, returns a value)
 *   <li>thenAccept (accepts an argument, no return value)
 *   <li>thenApply (accepts an argument, returns a value)
 * </ul>
 *
 * <p>All continuation methods are also available in the form of thenFinally[Run|Get|Accept|Apply], which indicates that
 * the continuation is also invoked when the future has failed by throwing an exception. thenFinally continuations come
 * in two versions each:
 *
 * <ul>
 *   <li>thenFinallyRun(FinallyRunnable successOrFailure)<br>
 *       This version handles the success and failure cases in a single functional interface. The functional interface
 *       supplies two parameters: the value (in case of success) and the stored exception (in case of failure). If the
 *       exception parameter is non-null, this indicates that the future has failed; if it is null, it indicates that
 *       the future has successfully completed.
 *   <li>thenFinallyRun(Runnable success, Consumer<AggregateException> failure)<br>
 *       This version handles the success and failure case in separate functional interfaces. Only one of the two
 *       supplied functional interfaces is called, depending on whether the future has successfully completed or failed
 *       by throwing an exception.
 * </ul>
 *
 * <p>All continuations methods are also available with the suffix -Async, which indicates that a future value is
 * returned by the continuation. This can be used to continue a future with another future. A common use case is to
 * continue a future with an async method (i.e. a method that returns a future value):
 *
 * <pre><blockquote>
 *     void showCurrentWeatherReport() {
 *         getCurrentLocationAsync()
 *             .thenApplyAsync(location -> getWeatherReportAsync(location))
 *             .whenSucceeded(report -> { System.out.println(report); });
 *     }
 *
 *     Future<Location> getCurrentLocationAsync() {
 *         return Future.get(() -> { request location from GeoIP service });
 *     }
 *
 *     Future<WeatherReport> getWeatherReportAsync(Location location) {
 *         return Future.get(() -> { request weather report for location });
 *     }
 * </blockquote></pre>
 *
 * <p>Futures can choose to support cooperative cancellation by checking the state of the {@link ProgressInfo} that is
 * passed to the functional interface that is executed by the future:
 *
 * <pre><blockquote>
 *     var future = Dispatcher.background().getLaterAsync(progressInfo -> {
 *         // long-running operation
 *         while (!progressInfo.isCancellationRequested()) {
 *             // ...
 *         }
 *     };
 * c
 *     // Cancels the operation, which the future observes by checking the progressInfo.
 *     future.cancel();
 * </blockquote></pre>
 *
 * Futures can be created with a custom {@link CancellationSource}, in which case all futures that use the same
 * cancellation source can be cancelled at once by calling {@link CancellationSource#cancel()}.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Future<V> extends ListenableFuture<V> {

    /**
     * Adds a progress listener that will be called whenever the progress of this future of any of its predecessors
     * changes. The reported progress will reflect the combined progress of the entire chain of futures that preceded
     * this future.
     */
    void addListener(ProgressListener listener);

    /** Returns whether this future has completed successfully. */
    boolean isSuccess();

    /** Returns whether this future has completed by throwing an exception. */
    boolean isFailed();

    /** Returns the stored exception of this future. */
    @Nullable
    AggregateException getException();

    /**
     * Gets the value of this future.
     *
     * @throws CancellationException if the future was cancelled
     * @throws RuntimeException if the future completed by throwing an exception
     */
    V getUnchecked();

    /**
     * Gets the value of this future.
     *
     * @throws CancellationException if the future was cancelled
     * @throws RuntimeException if the future completed by throwing an exception, or if the the timeout elapsed
     */
    V getUnchecked(long l, TimeUnit timeUnit);

    /** Equivalent to calling {@code Future.cancel(false)}. */
    boolean cancel();

    /**
     * Returns a new future that, when this future is completed successfully, is executed by calling the given {@link
     * Runnable}.
     */
    Future<Void> thenRun(Runnable runnable);

    /**
     * Returns a new future that, when this future is completed successfully, is executed by calling the given {@link
     * RunnableWithProgress}. A cancellation token will be supplied as an argument.
     */
    Future<Void> thenRun(RunnableWithProgress runnable);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling the given {@link FinallyRunnable}. This future's stored exception will be supplied as an argument.
     */
    Future<Void> thenFinallyRun(FinallyRunnable runnable);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling the given {@link FinallyRunnableWithProgress}. This future's stored exception and a cancellation token
     * will be supplied as an argument.
     */
    Future<Void> thenFinallyRun(FinallyRunnableWithProgress runnable);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling {@code success} or {@code failure}. This future's stored exception will be supplied to {@code failure}.
     */
    Future<Void> thenFinallyRun(Runnable success, Consumer<AggregateException> failure);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling {@code success} or {@code failure}. This future's stored exception will be supplied to {@code failure}. A
     * cancellation token will be supplied to both interfaces.
     */
    Future<Void> thenFinallyRun(RunnableWithProgress success, ConsumerWithProgress<AggregateException> failure);

    /**
     * Returns a new future that, when this future is completed successfully, is executed by calling the given {@link
     * Supplier}.
     */
    <R> Future<R> thenGet(Supplier<R> supplier);

    /**
     * Returns a new future that, when this future is completed successfully, is executed by calling the given {@link
     * SupplierWithProgress}. A cancellation token will be supplied as an argument.
     */
    <R> Future<R> thenGet(SupplierWithProgress<R> supplier);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling the given {@link FinallySupplier}. This future's stored exception will be supplied as an argument.
     */
    <R> Future<R> thenFinallyGet(FinallySupplier<R> supplier);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling the given {@link FinallySupplierWithProgress}. This future's stored exception and a cancellation token
     * will be supplied as an argument.
     */
    <R> Future<R> thenFinallyGet(FinallySupplierWithProgress<R> supplier);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling {@code success} or {@code failure}. This future's stored exception will be supplied to {@code failure}.
     */
    <R> Future<R> thenFinallyGet(Supplier<R> success, Function<AggregateException, R> failure);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling {@code success} or {@code failure}. This future's stored exception will be supplied to {@code failure}. A
     * cancellation token will be supplied to both interfaces.
     */
    <R> Future<R> thenFinallyGet(SupplierWithProgress<R> success, FunctionWithProgress<AggregateException, R> failure);

    /**
     * Returns a new future that, when this future is completed successfully, is executed by calling the given {@link
     * Consumer}. This future's result will be supplied as an argument.
     */
    Future<Void> thenAccept(Consumer<V> consumer);

    /**
     * Returns a new future that, when this future is completed successfully, is executed by calling the given {@link
     * ConsumerWithProgress}. This future's result and a cancellation token will be supplied as an argument.
     */
    Future<Void> thenAccept(ConsumerWithProgress<V> consumer);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling the given {@link FinallyConsumer}. This future's result and its stored exception will be supplied as an
     * argument.
     */
    Future<Void> thenFinallyAccept(FinallyConsumer<V> consumer);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling the given {@link FinallyConsumerWithProgress}. This future's result, its stored exception and a
     * cancellation token will be supplied as an argument.
     */
    Future<Void> thenFinallyAccept(FinallyConsumerWithProgress<V> consumer);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling {@code success} or {@code failure}. The result of this future will be supplied to {@code success}, while
     * its stored exception will be supplied to {@code failure}.
     */
    Future<Void> thenFinallyAccept(Consumer<V> success, Consumer<AggregateException> failure);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling {@code success} or {@code failure}. The result of this future will be supplied to {@code success}, while
     * its stored exception will be supplied to {@code failure}. A cancellation token will be supplied to both
     * interfaces.
     */
    Future<Void> thenFinallyAccept(ConsumerWithProgress<V> success, ConsumerWithProgress<AggregateException> failure);

    /**
     * Returns a new future that, when this future is completed successfully, is executed by calling the given {@link
     * Function}. This future's result will be supplied as an argument.
     */
    <R> Future<R> thenApply(Function<V, R> function);

    /**
     * Returns a new future that, when this future is completed successfully, is executed by calling the given {@link
     * FunctionWithProgress}. This future's result and a cancellation token will be supplied as an argument.
     */
    <R> Future<R> thenApply(FunctionWithProgress<V, R> function);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling the given {@link FinallyFunction}. This future's result and its stored exception will be supplied as an
     * argument.
     */
    <R> Future<R> thenFinallyApply(FinallyFunction<V, R> function);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling the given {@link FinallyFunctionWithProgress}. This future's result, its stored exception and a
     * cancellation token will be supplied as an argument.
     */
    <R> Future<R> thenFinallyApply(FinallyFunctionWithProgress<V, R> function);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling {@code success} or {@code failure}. The result of this future will be supplied to {@code success}, while
     * its stored exception will be supplied to {@code failure}.
     */
    <R> Future<R> thenFinallyApply(Function<V, R> success, Function<AggregateException, R> failure);

    /**
     * Returns a new future that, when this future is completed successfully or by throwing an exception, is executed by
     * calling {@code success} or {@code failure}. The result of this future will be supplied to {@code success}, while
     * its stored exception will be supplied to {@code failure}. A cancellation token will be supplied to both
     * interfaces.
     */
    <R> Future<R> thenFinallyApply(
            FunctionWithProgress<V, R> success, FunctionWithProgress<AggregateException, R> failure);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureRunnable}.
     */
    Future<Void> thenRunAsync(FutureRunnable runnable);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureRunnableWithProgress}. A cancellation token will be supplied as an argument.
     */
    Future<Void> thenRunAsync(FutureRunnableWithProgress runnable);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureFinallyRunnable}. This future's stored exception will be supplied as an argument.
     */
    Future<Void> thenFinallyRunAsync(FutureFinallyRunnable runnable);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureFinallyRunnableWithProgress}. This future's stored exception and a cancellation token will be
     * supplied as an argument.
     */
    Future<Void> thenFinallyRunAsync(FutureFinallyRunnableWithProgress runnable);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@code success}. When this future is completed by throwing an exception, it corresponds to the future returned by
     * {@code failure}.
     */
    Future<Void> thenFinallyRunAsync(FutureRunnable success, FutureConsumer<AggregateException> failure);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@code success}. When this future is completed by throwing an exception, it corresponds to the future returned by
     * {@code failure}. A cancellation token is supplied as an argument.
     */
    Future<Void> thenFinallyRunAsync(
            FutureRunnableWithProgress success, FutureConsumerWithProgress<AggregateException> failure);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureSupplier}.
     */
    <R> Future<R> thenGetAsync(FutureSupplier<R> supplier);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureSupplierWithProgress}. A cancellation token will be supplied as an argument.
     */
    <R> Future<R> thenGetAsync(FutureSupplierWithProgress<R> supplier);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureFinallySupplier}. This future's stored exception will be supplied as an argument.
     */
    <R> Future<R> thenFinallyGetAsync(FutureFinallySupplier<R> supplier);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureFinallySupplierWithProgress}. This future's stored exception and a cancellation token will be
     * supplied as an argument.
     */
    <R> Future<R> thenFinallyGetAsync(FutureFinallySupplierWithProgress<R> supplier);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@code success}. When this future is completed by throwing an exception, it corresponds to the future returned by
     * {@code failure}.
     */
    <R> Future<R> thenFinallyGetAsync(FutureSupplier<R> success, FutureFunction<AggregateException, R> failure);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@code success}. When this future is completed by throwing an exception, it corresponds to the future returned by
     * {@code failure}. A cancellation token is supplied as an argument.
     */
    <R> Future<R> thenFinallyGetAsync(
            FutureSupplierWithProgress<R> success, FutureFunctionWithProgress<AggregateException, R> failure);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureConsumer}. This future's result will be supplied as an argument.
     */
    Future<Void> thenAcceptAsync(FutureConsumer<V> consumer);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureConsumerWithProgress}. This future's result and a cancellation token will be supplied as an
     * argument.
     */
    Future<Void> thenAcceptAsync(FutureConsumerWithProgress<V> consumer);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureFinallyConsumer}. This future's result and its stored exception will be supplied as an argument.
     */
    Future<Void> thenFinallyAcceptAsync(FutureFinallyConsumer<V> consumer);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureFinallyConsumerWithProgress}. This future's result, its stored exception and a cancellation token
     * will be supplied as an argument.
     */
    Future<Void> thenFinallyAcceptAsync(FutureFinallyConsumerWithProgress<V> consumer);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@code success}. When this future is completed by throwing an exception, it corresponds to the future returned by
     * {@code failure}.
     */
    Future<Void> thenFinallyAcceptAsync(FutureConsumer<V> success, FutureConsumer<AggregateException> failure);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@code success}. When this future is completed by throwing an exception, it corresponds to the future returned by
     * {@code failure}. A cancellation token is supplied as an argument.
     */
    Future<Void> thenFinallyAcceptAsync(
            FutureConsumerWithProgress<V> success, FutureConsumerWithProgress<AggregateException> failure);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureFunction}. This future's result will be supplied as an argument.
     */
    <R> Future<R> thenApplyAsync(FutureFunction<V, R> function);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureFunctionWithProgress}. This future's result and a cancellation token will be supplied as an
     * argument.
     */
    <R> Future<R> thenApplyAsync(FutureFunctionWithProgress<V, R> function);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureFinallyFunction}. This future's result and its stored exception will be supplied as an argument.
     */
    <R> Future<R> thenFinallyApplyAsync(FutureFinallyFunction<V, R> function);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@link FutureFinallyFunctionWithProgress}. This future's result, its stored exception and a cancellation token
     * will be supplied as an argument.
     */
    <R> Future<R> thenFinallyApplyAsync(FutureFinallyFunctionWithProgress<V, R> function);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@code success}. When this future is completed by throwing an exception, it corresponds to the future returned by
     * {@code failure}.
     */
    <R> Future<R> thenFinallyApplyAsync(FutureFunction<V, R> success, FutureFunction<AggregateException, R> failure);

    /**
     * Returns a new future that, when this future is completed successfully, corresponds to the future returned by
     * {@code success}. When this future is completed by throwing an exception, it corresponds to the future returned by
     * {@code failure}. A cancellation token is supplied as an argument.
     */
    <R> Future<R> thenFinallyApplyAsync(
            FutureFunctionWithProgress<V, R> success, FutureFunctionWithProgress<AggregateException, R> failure);

    /**
     * Executes the supplied {@link Runnable} when this future is completed successfully. This method returns the
     * current future instance, it does not return a new future.
     */
    Future<V> whenSucceeded(Runnable runnable);

    /**
     * Executes the supplied {@link Runnable} with a custom executor when this future is completed successfully. This
     * method returns the current future instance, it does not return a new future.
     */
    Future<V> whenSucceeded(Runnable runnable, Executor executor);

    /**
     * Executes the supplied {@link Consumer} when this future is completed successfully. This future's result will be
     * supplied as an argument. This method returns the current future instance, it does not return a new future.
     */
    Future<V> whenSucceeded(Consumer<V> consumer);

    /**
     * Executes the supplied {@link Consumer} with a custom executor when this future is completed successfully. This
     * future's result will be supplied as an argument. This method returns the current future instance, it does not
     * return a new future.
     */
    Future<V> whenSucceeded(Consumer<V> consumer, Executor executor);

    /**
     * Executes the supplied {@link Runnable} when this future is completed by throwing an exception. This method
     * returns the current future instance, it does not return a new future.
     */
    Future<V> whenFailed(Runnable runnable);

    /**
     * Executes the supplied {@link Runnable} with a custom executor when this future is completed by throwing an
     * exception. This method returns the current future instance, it does not return a new future.
     */
    Future<V> whenFailed(Runnable runnable, Executor executor);

    /**
     * Executes the supplied {@link Consumer} when this future is completed by throwing an exception. This future's
     * stored exception will be supplied as an argument. This method returns the current future instance, it does not
     * return a new future.
     */
    Future<V> whenFailed(Consumer<AggregateException> consumer);

    /**
     * Executes the supplied {@link Consumer} with a custom executor when this future is completed by throwing an
     * exception. This future's stored exception will be supplied as an argument. This method returns the current future
     * instance, it does not return a new future.
     */
    Future<V> whenFailed(Consumer<AggregateException> consumer, Executor executor);

    /**
     * Executes the supplied {@link Runnable} when this future is cancelled. This method returns the current future
     * instance, it does not return a new future.
     */
    Future<V> whenCancelled(Runnable runnable);

    /**
     * Executes the supplied {@link Runnable} with a custom executor when this future is cancelled. This method returns
     * the current future instance, it does not return a new future.
     */
    Future<V> whenCancelled(Runnable runnable, Executor executor);

    /**
     * Executes the supplied {@link Runnable} when this future is completed successfully or by throwing an exception, or
     * when this future is cancelled. This method returns the current future instance, it does not return a new future.
     */
    Future<V> whenDone(Runnable runnable);

    /**
     * Executes the supplied {@link Runnable} with a custom executor when this future is completed successfully or by
     * throwing an exception, or when this future is cancelled. This method returns the current future instance, it does
     * not return a new future.
     */
    Future<V> whenDone(Runnable runnable, Executor executor);

    /**
     * Executes the supplied {@link Runnable} when this future is completed successfully or by throwing an exception, or
     * when this future is cancelled. This future is supplied as an argument. This method returns the current future
     * instance, it does not return a new future.
     */
    Future<V> whenDone(Consumer<Future<V>> consumer);

    /**
     * Executes the supplied {@link Runnable} with a custom executor when this future is completed successfully or by
     * throwing an exception, or when this future is cancelled. This future is supplied as an argument. This method
     * returns the current future instance, it does not return a new future.
     */
    Future<V> whenDone(Consumer<Future<V>> consumer, Executor executor);

    /**
     * Returns a new future that, when this future is completed successfully, represents the value that was obtained by
     * casting this future's value to the specified type T.
     */
    <T> Future<T> cast();

    interface FutureRunnable {
        Future<Void> run();
    }

    interface FutureFinallyRunnable {
        Future<Void> run(@Nullable AggregateException exception);
    }

    interface FutureRunnableWithProgress {
        Future<Void> run(ProgressInfo progressInfo);
    }

    interface FutureFinallyRunnableWithProgress {
        Future<Void> run(@Nullable AggregateException exception, ProgressInfo progressInfo);
    }

    interface FutureConsumer<T> {
        Future<Void> accept(T value);
    }

    interface FutureFinallyConsumer<T> {
        Future<Void> accept(@Nullable T value, @Nullable AggregateException exception);
    }

    interface FutureConsumerWithProgress<T> {
        Future<Void> accept(T value, ProgressInfo progressInfo);
    }

    interface FutureFinallyConsumerWithProgress<T> {
        Future<Void> accept(@Nullable T value, @Nullable AggregateException exception, ProgressInfo progressInfo);
    }

    interface FutureSupplier<T> {
        Future<T> get();
    }

    interface FutureFinallySupplier<T> {
        Future<T> get(@Nullable AggregateException exception);
    }

    interface FutureSupplierWithProgress<T> {
        Future<T> get(ProgressInfo progressInfo);
    }

    interface FutureFinallySupplierWithProgress<T> {
        Future<T> get(@Nullable AggregateException exception, ProgressInfo progressInfo);
    }

    interface FutureFunction<T, R> {
        Future<R> apply(T value);
    }

    interface FutureFinallyFunction<T, R> {
        Future<R> apply(@Nullable T value, @Nullable AggregateException exception);
    }

    interface FutureFunctionWithProgress<T, R> {
        Future<R> apply(T value, ProgressInfo progressInfo);
    }

    interface FutureFinallyFunctionWithProgress<T, R> {
        Future<R> apply(@Nullable T value, @Nullable AggregateException exception, ProgressInfo progressInfo);
    }

    interface FinallyRunnable {
        void run(@Nullable AggregateException exception);
    }

    interface RunnableWithProgress {
        void run(ProgressInfo progressInfo);
    }

    interface FinallyRunnableWithProgress {
        void run(@Nullable AggregateException exception, ProgressInfo progressInfo);
    }

    interface FinallySupplier<T> {
        T get(@Nullable AggregateException exception);
    }

    interface SupplierWithProgress<T> {
        T get(ProgressInfo progressInfo);
    }

    interface FinallySupplierWithProgress<T> {
        T get(@Nullable AggregateException exception, ProgressInfo progressInfo);
    }

    interface FinallyConsumer<T> {
        void accept(@Nullable T value, @Nullable AggregateException exception);
    }

    interface ConsumerWithProgress<T> {
        void accept(T value, ProgressInfo progressInfo);
    }

    interface FinallyConsumerWithProgress<T> {
        void accept(@Nullable T value, @Nullable AggregateException exception, ProgressInfo progressInfo);
    }

    interface FinallyFunction<T, R> {
        R apply(@Nullable T value, @Nullable AggregateException exception);
    }

    interface FunctionWithProgress<T, R> {
        R apply(T value, ProgressInfo progressInfo);
    }

    interface FinallyFunctionWithProgress<T, R> {
        R apply(@Nullable T value, @Nullable AggregateException exception, ProgressInfo progressInfo);
    }

}
