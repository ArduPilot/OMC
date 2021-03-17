/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import static org.asyncfx.concurrent.FutureFlags.CANCELLED;
import static org.asyncfx.concurrent.FutureFlags.DONE;
import static org.asyncfx.concurrent.FutureFlags.FAILED;
import static org.asyncfx.concurrent.FutureFlags.RUNNING;
import static org.asyncfx.concurrent.FutureFlags.SUCCEEDED;

import com.google.common.util.concurrent.MoreExecutors;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

/**
 * This class provides base functionality for all non-trivial futures, such as completing the future and adding
 * continuations.
 */
class CompletableFuture<V> extends AbstractFuture<V> {

    interface FutureProvider<V, R> {
        Future<R> provide(Future<V> predecessor, ProgressInfo progressInfo);
    }

    private static <T> Future<T> verifyNotNull(Future<T> future) {
        if (future == null) {
            throw new NullPointerException(
                "Async continuations must return a " + Future.class.getSimpleName() + " instance.");
        }

        return future;
    }

    private static class FutureRunnableProvider<T> implements FutureProvider<T, Void> {
        private final FutureRunnable futureRunnable;

        FutureRunnableProvider(FutureRunnable futureRunnable) {
            this.futureRunnable = futureRunnable;
        }

        @Override
        public Future<Void> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            return verifyNotNull(futureRunnable.run());
        }
    }

    private static class FutureFinallyRunnableProvider<T> implements FutureProvider<T, Void> {
        private final FutureFinallyRunnable futureRunnable;

        FutureFinallyRunnableProvider(FutureFinallyRunnable futureRunnable) {
            this.futureRunnable = futureRunnable;
        }

        @Override
        public Future<Void> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            return verifyNotNull(futureRunnable.run(predecessor.getException()));
        }
    }

    private static class FutureRunnableWithProgressProvider<T> implements FutureProvider<T, Void> {
        private final FutureRunnableWithProgress futureRunnable;

        FutureRunnableWithProgressProvider(FutureRunnableWithProgress futureRunnable) {
            this.futureRunnable = futureRunnable;
        }

        @Override
        public Future<Void> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            return verifyNotNull(futureRunnable.run(progressInfo));
        }
    }

    private static class FutureFinallyRunnableWithProgressProvider<T> implements FutureProvider<T, Void> {
        private final FutureFinallyRunnableWithProgress futureRunnable;

        FutureFinallyRunnableWithProgressProvider(FutureFinallyRunnableWithProgress futureRunnable) {
            this.futureRunnable = futureRunnable;
        }

        @Override
        public Future<Void> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            return verifyNotNull(futureRunnable.run(predecessor.getException(), progressInfo));
        }
    }

    private static class FutureConsumerProvider<T> implements FutureProvider<T, Void> {
        private final FutureConsumer<T> futureConsumer;

        FutureConsumerProvider(FutureConsumer<T> futureConsumer) {
            this.futureConsumer = futureConsumer;
        }

        @Override
        public Future<Void> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            return verifyNotNull(futureConsumer.accept(predecessor.getUnchecked()));
        }
    }

    private static class FutureFinallyConsumerProvider<T> implements FutureProvider<T, Void> {
        private final FutureFinallyConsumer<T> futureConsumer;

        FutureFinallyConsumerProvider(FutureFinallyConsumer<T> futureConsumer) {
            this.futureConsumer = futureConsumer;
        }

        @Override
        public Future<Void> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            AggregateException exception = predecessor.getException();
            return exception != null
                ? verifyNotNull(futureConsumer.accept(null, exception))
                : verifyNotNull(futureConsumer.accept(predecessor.getUnchecked(), null));
        }
    }

    private static class FutureConsumerWithProgressProvider<T> implements FutureProvider<T, Void> {
        private final FutureConsumerWithProgress<T> futureConsumer;

        FutureConsumerWithProgressProvider(FutureConsumerWithProgress<T> futureConsumer) {
            this.futureConsumer = futureConsumer;
        }

        @Override
        public Future<Void> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            return verifyNotNull(futureConsumer.accept(predecessor.getUnchecked(), progressInfo));
        }
    }

    private static class FutureFinallyConsumerWithProgressProvider<T> implements FutureProvider<T, Void> {
        private final FutureFinallyConsumerWithProgress<T> futureConsumer;

        FutureFinallyConsumerWithProgressProvider(FutureFinallyConsumerWithProgress<T> futureConsumer) {
            this.futureConsumer = futureConsumer;
        }

        @Override
        public Future<Void> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            AggregateException exception = predecessor.getException();
            return exception != null
                ? verifyNotNull(futureConsumer.accept(null, exception, progressInfo))
                : verifyNotNull(futureConsumer.accept(predecessor.getUnchecked(), null, progressInfo));
        }
    }

    private static class FutureSupplierProvider<T, R> implements FutureProvider<T, R> {
        private final FutureSupplier<R> futureSupplier;

        FutureSupplierProvider(FutureSupplier<R> futureSupplier) {
            this.futureSupplier = futureSupplier;
        }

        @Override
        public Future<R> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            return verifyNotNull(futureSupplier.get());
        }
    }

    private static class FutureFinallySupplierProvider<T, R> implements FutureProvider<T, R> {
        private final FutureFinallySupplier<R> futureSupplier;

        FutureFinallySupplierProvider(FutureFinallySupplier<R> futureSupplier) {
            this.futureSupplier = futureSupplier;
        }

        @Override
        public Future<R> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            return verifyNotNull(futureSupplier.get(predecessor.getException()));
        }
    }

    private static class FutureSupplierWithProgressProvider<T, R> implements FutureProvider<T, R> {
        private final FutureSupplierWithProgress<R> futureSupplier;

        FutureSupplierWithProgressProvider(FutureSupplierWithProgress<R> futureSupplier) {
            this.futureSupplier = futureSupplier;
        }

        @Override
        public Future<R> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            return verifyNotNull(futureSupplier.get(progressInfo));
        }
    }

    private static class FutureFinallySupplierWithProgressProvider<T, R> implements FutureProvider<T, R> {
        private final FutureFinallySupplierWithProgress<R> futureSupplier;

        FutureFinallySupplierWithProgressProvider(FutureFinallySupplierWithProgress<R> futureSupplier) {
            this.futureSupplier = futureSupplier;
        }

        @Override
        public Future<R> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            return verifyNotNull(futureSupplier.get(predecessor.getException(), progressInfo));
        }
    }

    private static class FutureFunctionProvider<T, R> implements FutureProvider<T, R> {
        private final FutureFunction<T, R> futureFunction;

        FutureFunctionProvider(FutureFunction<T, R> futureFunction) {
            this.futureFunction = futureFunction;
        }

        @Override
        public Future<R> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            return verifyNotNull(futureFunction.apply(predecessor.getUnchecked()));
        }
    }

    private static class FutureFinallyFunctionProvider<T, R> implements FutureProvider<T, R> {
        private final FutureFinallyFunction<T, R> futureFunction;

        FutureFinallyFunctionProvider(FutureFinallyFunction<T, R> futureFunction) {
            this.futureFunction = futureFunction;
        }

        @Override
        public Future<R> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            AggregateException exception = predecessor.getException();
            return exception != null
                ? verifyNotNull(futureFunction.apply(null, exception))
                : verifyNotNull(futureFunction.apply(predecessor.getUnchecked(), null));
        }
    }

    private static class FutureFunctionWithProgressProvider<T, R> implements FutureProvider<T, R> {
        private final FutureFunctionWithProgress<T, R> futureFunction;

        FutureFunctionWithProgressProvider(FutureFunctionWithProgress<T, R> futureFunction) {
            this.futureFunction = futureFunction;
        }

        @Override
        public Future<R> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            return verifyNotNull(futureFunction.apply(predecessor.getUnchecked(), progressInfo));
        }
    }

    private static class FutureFinallyFunctionWithProgressProvider<T, R> implements FutureProvider<T, R> {
        private final FutureFinallyFunctionWithProgress<T, R> futureFunction;

        FutureFinallyFunctionWithProgressProvider(FutureFinallyFunctionWithProgress<T, R> futureFunction) {
            this.futureFunction = futureFunction;
        }

        @Override
        public Future<R> provide(Future<T> predecessor, ProgressInfo progressInfo) {
            AggregateException exception = predecessor.getException();
            return exception != null
                ? verifyNotNull(futureFunction.apply(null, exception, progressInfo))
                : verifyNotNull(futureFunction.apply(predecessor.getUnchecked(), null, progressInfo));
        }
    }

    private interface CompletionAction<V> {
        void run(Future<V> future, ContinuationOption condition);
    }

    private static class RunnableCompletionAction<V> implements CompletionAction<V> {
        private final Runnable runnable;
        private final Executor executor;
        private final ContinuationOption continuationOption;

        RunnableCompletionAction(Runnable runnable, Executor executor, ContinuationOption continuationOption) {
            this.runnable = runnable;
            this.executor = executor;
            this.continuationOption = continuationOption;
        }

        @Override
        public void run(Future<V> future, ContinuationOption continuationOption) {
            if (this.continuationOption.continuesWhen(continuationOption)) {
                executor.execute(runnable);
            }
        }
    }

    private static class ResultConsumerCompletionAction<V> implements CompletionAction<V> {
        private final Consumer<V> consumer;
        private final Executor executor;
        private final ContinuationOption continuationOption;

        ResultConsumerCompletionAction(Consumer<V> consumer, Executor executor, ContinuationOption continuationOption) {
            this.consumer = consumer;
            this.executor = executor;
            this.continuationOption = continuationOption;
        }

        @Override
        public void run(Future<V> future, ContinuationOption condition) {
            if (this.continuationOption.continuesWhen(condition)) {
                executor.execute(() -> consumer.accept(future.getUnchecked()));
            }
        }
    }

    private static class ExceptionConsumerCompletionAction<V> implements CompletionAction<V> {
        private final Consumer<AggregateException> consumer;
        private final Executor executor;
        private final ContinuationOption continuationOption;

        ExceptionConsumerCompletionAction(
                Consumer<AggregateException> consumer, Executor executor, ContinuationOption continuationOption) {
            this.consumer = consumer;
            this.executor = executor;
            this.continuationOption = continuationOption;
        }

        @Override
        public void run(Future<V> future, ContinuationOption condition) {
            if (this.continuationOption.continuesWhen(condition)) {
                executor.execute(() -> consumer.accept(future.getException()));
            }
        }
    }

    private static class FutureConsumerCompletionAction<V> implements CompletionAction<V> {
        private final Consumer<Future<V>> consumer;
        private final Executor executor;
        private final ContinuationOption continuationOption;

        FutureConsumerCompletionAction(
                Consumer<Future<V>> consumer, Executor executor, ContinuationOption continuationOption) {
            this.consumer = consumer;
            this.executor = executor;
            this.continuationOption = continuationOption;
        }

        @Override
        public void run(Future<V> future, ContinuationOption condition) {
            if (this.continuationOption.continuesWhen(condition)) {
                executor.execute(() -> consumer.accept(future));
            }
        }
    }

    private final ProgressInfo progressInfo = new ProgressInfo(this);
    private List<ContinuationFuture> continuations;
    private List<CompletionAction<V>> completionActions;
    private ContinuationFuture singleContinuation;
    private CompletionAction<V> singleCompletionAction;
    private AggregateException exception;
    private int flags;
    private V value;

    @Override
    public void addListener(@NotNull Runnable runnable, @NotNull Executor executor) {
        whenDone(runnable, executor);
    }

    @Override
    public boolean isSuccess() {
        return hasFlag(SUCCEEDED);
    }

    @Override
    public boolean isFailed() {
        return hasFlag(FAILED);
    }

    @Override
    public boolean isCancelled() {
        return hasFlag(CANCELLED);
    }

    @Override
    public boolean isDone() {
        return hasFlag(DONE);
    }

    @Override
    public V get() throws ExecutionException {
        int flag = WaitHelper.waitAndGet(this, () -> hasFlagUnsafe(DONE), () -> flags);

        if ((flag & SUCCEEDED) != 0) {
            return value;
        }

        if ((flag & FAILED) != 0) {
            throw new ExecutionException(exception);
        }

        throw new CancellationException();
    }

    @Override
    public V get(long timeout, @NotNull TimeUnit unit) throws ExecutionException, TimeoutException {
        int flag =
            WaitHelper.wait(this, () -> hasFlagUnsafe(DONE), () -> flags, Duration.ofNanos(unit.toNanos(timeout)));

        if ((flag & SUCCEEDED) != 0) {
            return value;
        }

        if ((flag & FAILED) != 0) {
            throw new ExecutionException(exception);
        }

        if ((flag & CANCELLED) != 0) {
            throw new CancellationException();
        }

        throw new TimeoutException();
    }

    @Override
    public AggregateException getException() {
        return exception;
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (hasFlagUnsafe(DONE)) {
            return false;
        }

        if (progressInfo.cancel()) {
            return cancellationRequested(mayInterruptIfRunning);
        }

        return false;
    }

    @Synchronized
    boolean cancellationRequested(boolean mayInterruptIfRunning) {
        return true;
    }

    @Override
    public Future<Void> thenRunAsync(FutureRunnable runnable) {
        return continueWith(new FutureRunnableProvider<>(runnable), ContinuationOption.SUCCESS);
    }

    @Override
    public Future<Void> thenRunAsync(FutureRunnableWithProgress runnable) {
        return continueWith(new FutureRunnableWithProgressProvider<>(runnable), ContinuationOption.SUCCESS);
    }

    @Override
    public Future<Void> thenFinallyRunAsync(FutureFinallyRunnable runnable) {
        return continueWith(new FutureFinallyRunnableProvider<>(runnable), ContinuationOption.NOT_CANCELLED);
    }

    @Override
    public Future<Void> thenFinallyRunAsync(FutureFinallyRunnableWithProgress runnable) {
        return continueWith(
            new FutureFinallyRunnableWithProgressProvider<>(runnable), ContinuationOption.NOT_CANCELLED);
    }

    @Override
    public <R> Future<R> thenGetAsync(FutureSupplier<R> supplier) {
        return continueWith(new FutureSupplierProvider<>(supplier), ContinuationOption.SUCCESS);
    }

    @Override
    public <R> Future<R> thenGetAsync(FutureSupplierWithProgress<R> supplier) {
        return continueWith(new FutureSupplierWithProgressProvider<>(supplier), ContinuationOption.SUCCESS);
    }

    @Override
    public <R> Future<R> thenFinallyGetAsync(FutureFinallySupplier<R> supplier) {
        return continueWith(new FutureFinallySupplierProvider<>(supplier), ContinuationOption.NOT_CANCELLED);
    }

    @Override
    public <R> Future<R> thenFinallyGetAsync(FutureFinallySupplierWithProgress<R> supplier) {
        return continueWith(
            new FutureFinallySupplierWithProgressProvider<>(supplier), ContinuationOption.NOT_CANCELLED);
    }

    @Override
    public Future<Void> thenAcceptAsync(FutureConsumer<V> consumer) {
        return continueWith(new FutureConsumerProvider<>(consumer), ContinuationOption.SUCCESS);
    }

    @Override
    public Future<Void> thenAcceptAsync(FutureConsumerWithProgress<V> consumer) {
        return continueWith(new FutureConsumerWithProgressProvider<>(consumer), ContinuationOption.SUCCESS);
    }

    @Override
    public Future<Void> thenFinallyAcceptAsync(FutureFinallyConsumer<V> consumer) {
        return continueWith(new FutureFinallyConsumerProvider<>(consumer), ContinuationOption.NOT_CANCELLED);
    }

    @Override
    public Future<Void> thenFinallyAcceptAsync(FutureFinallyConsumerWithProgress<V> consumer) {
        return continueWith(
            new FutureFinallyConsumerWithProgressProvider<>(consumer), ContinuationOption.NOT_CANCELLED);
    }

    @Override
    public <R> Future<R> thenApplyAsync(FutureFunction<V, R> function) {
        return continueWith(new FutureFunctionProvider<>(function), ContinuationOption.SUCCESS);
    }

    @Override
    public <R> Future<R> thenApplyAsync(FutureFunctionWithProgress<V, R> function) {
        return continueWith(new FutureFunctionWithProgressProvider<>(function), ContinuationOption.SUCCESS);
    }

    @Override
    public <R> Future<R> thenFinallyApplyAsync(FutureFinallyFunction<V, R> function) {
        return continueWith(new FutureFinallyFunctionProvider<>(function), ContinuationOption.NOT_CANCELLED);
    }

    @Override
    public <R> Future<R> thenFinallyApplyAsync(FutureFinallyFunctionWithProgress<V, R> function) {
        return continueWith(
            new FutureFinallyFunctionWithProgressProvider<>(function), ContinuationOption.NOT_CANCELLED);
    }

    @Override
    public Future<V> whenDone(Runnable runnable, Executor executor) {
        addCompletionAction(new RunnableCompletionAction<>(runnable, executor, ContinuationOption.ALWAYS));
        return this;
    }

    @Override
    public Future<V> whenDone(Consumer<Future<V>> consumer, Executor executor) {
        addCompletionAction(new FutureConsumerCompletionAction<>(consumer, executor, ContinuationOption.ALWAYS));
        return this;
    }

    @Override
    public Future<V> whenSucceeded(Runnable runnable, Executor executor) {
        addCompletionAction(new RunnableCompletionAction<>(runnable, executor, ContinuationOption.SUCCESS));
        return this;
    }

    @Override
    public Future<V> whenSucceeded(Consumer<V> consumer, Executor executor) {
        addCompletionAction(new ResultConsumerCompletionAction<>(consumer, executor, ContinuationOption.SUCCESS));
        return this;
    }

    @Override
    public Future<V> whenFailed(Runnable runnable, Executor executor) {
        addCompletionAction(new RunnableCompletionAction<>(runnable, executor, ContinuationOption.FAILURE));
        return this;
    }

    @Override
    public Future<V> whenFailed(Consumer<AggregateException> consumer, Executor executor) {
        addCompletionAction(new ExceptionConsumerCompletionAction<>(consumer, executor, ContinuationOption.FAILURE));
        return this;
    }

    @Override
    public Future<V> whenCancelled(Runnable runnable, Executor executor) {
        addCompletionAction(new RunnableCompletionAction<>(runnable, executor, ContinuationOption.CANCELLED));
        return this;
    }

    @Override
    ProgressInfo getProgressInfo() {
        return progressInfo;
    }

    @Override
    @MaybeUnsynchronized
    void notifyProgressListeners(double cumulativeProgress, int rank) {
        progressInfo.notifyListeners(cumulativeProgress, rank);

        synchronized (progressInfo) {
            if (singleContinuation != null) {
                singleContinuation.notifyProgressListeners(cumulativeProgress, rank + 1);
            } else if (continuations != null) {
                for (ContinuationFuture continuation : continuations) {
                    continuation.notifyProgressListeners(cumulativeProgress, rank + 1);
                }
            }
        }
    }

    @MaybeUnsynchronized
    void completeWithResult(V value) {
        synchronized (this) {
            if (hasFlagUnsafe(DONE)) {
                return;
            }

            this.value = value;
            clearFlagUnsafe(RUNNING);
            setFlagUnsafe(SUCCEEDED);
            notifyAll();
        }

        runContinuations(ContinuationOption.SUCCESS);
    }

    @MaybeUnsynchronized
    void completeWithException(Throwable throwable) {
        ContinuationOption continuationOption;

        synchronized (this) {
            if (hasFlagUnsafe(DONE)) {
                return;
            }

            clearFlagUnsafe(RUNNING);

            if (throwable instanceof CancellationException) {
                continuationOption = ContinuationOption.CANCELLED;
                setFlagUnsafe(CANCELLED);
            } else {
                this.exception =
                    throwable instanceof AggregateException
                        ? (AggregateException)throwable
                        : new AggregateException(throwable);
                continuationOption = ContinuationOption.FAILURE;
                setFlagUnsafe(FAILED);
            }

            notifyAll();
        }

        runContinuations(continuationOption);
    }

    @MaybeUnsynchronized
    void completeWithCancellation(AggregateException exception) {
        synchronized (this) {
            if (hasFlagUnsafe(DONE)) {
                return;
            }

            this.exception = exception;
            clearFlagUnsafe(RUNNING);
            setFlagUnsafe(CANCELLED);
            notifyAll();
        }

        runContinuations(ContinuationOption.CANCELLED);
    }

    @Synchronized
    boolean cancelContinuations(boolean mayInterruptIfRunning) {
        synchronized (progressInfo) {
            if (singleContinuation != null) {
                return singleContinuation.cancel(mayInterruptIfRunning);
            } else if (continuations != null) {
                boolean result = false;
                for (ContinuationFuture continuation : continuations) {
                    result |= continuation.cancel(mayInterruptIfRunning);
                }

                return result;
            }
        }

        return false;
    }

    @MaybeUnsynchronized
    void runContinuations(ContinuationOption continuationOption) {
        progressInfo.setProgress(1);

        // We don't need to protect the fields accessed by this method because they can't change by the time we are
        // here (we're in SUCCESS, FAILURE or CANCELLED state).
        if (singleCompletionAction != null) {
            singleCompletionAction.run(this, continuationOption);
        } else if (completionActions != null) {
            for (CompletionAction<V> completionAction : completionActions) {
                try {
                    completionAction.run(this, continuationOption);
                } catch (Throwable throwable) {
                    Thread currentThread = Thread.currentThread();
                    if (currentThread instanceof FutureExecutorService.ExecutorThread) {
                        ((FutureExecutorService.ExecutorThread)currentThread).uncaughtException(throwable);
                    } else {
                        throw throwable;
                    }
                }
            }
        }

        DefaultFutureFactory factory = DefaultFutureFactory.getInstance();

        if (singleContinuation != null) {
            if (factory.canExecuteDirectly()) {
                singleContinuation.runIfContinued(continuationOption, MoreExecutors.directExecutor());
            } else {
                singleContinuation.runIfContinued(continuationOption, factory);
            }
        } else if (continuations != null) {
            if (factory.canExecuteDirectly()) {
                for (ContinuationFuture continuation : continuations) {
                    continuation.runIfContinued(continuationOption, MoreExecutors.directExecutor());
                }
            } else {
                for (ContinuationFuture continuation : continuations) {
                    continuation.runIfContinued(continuationOption, factory);
                }
            }
        }
    }

    private synchronized void addCompletionAction(CompletionAction<V> completionAction) {
        if (hasFlagUnsafe(SUCCEEDED)) {
            completionAction.run(this, ContinuationOption.SUCCESS);
        } else if (hasFlagUnsafe(FAILED)) {
            completionAction.run(this, ContinuationOption.FAILURE);
        } else if (hasFlagUnsafe(CANCELLED)) {
            completionAction.run(this, ContinuationOption.CANCELLED);
        } else {
            if (completionActions != null) {
                completionActions.add(completionAction);
            } else if (singleCompletionAction == null) {
                singleCompletionAction = completionAction;
            } else {
                completionActions = new ArrayList<>(3);
                completionActions.add(singleCompletionAction);
                completionActions.add(completionAction);
                singleCompletionAction = null;
            }
        }
    }

    private synchronized <R> Future<R> continueWith(
            FutureProvider<V, R> futureProvider, ContinuationOption continuationOption) {
        DefaultFutureFactory factory = DefaultFutureFactory.getInstance();

        if (hasFlagUnsafe(CANCELLED) && continuationOption.continueOnCancelled()
                || hasFlagUnsafe(SUCCEEDED) && continuationOption.continueOnSuccess()
                || hasFlagUnsafe(FAILED) && continuationOption.continueOnFailure()) {
            ContinuationFuture<V, R> continuation =
                new ContinuationFuture<>(this, futureProvider, ContinuationOption.ALWAYS);
            if (factory.canExecuteDirectly()) {
                continuation.runIfContinued(ContinuationOption.ALWAYS, MoreExecutors.directExecutor());
            } else {
                continuation.runIfContinued(ContinuationOption.ALWAYS, factory);
            }

            return continuation;
        }

        if (hasFlagUnsafe(CANCELLED)) {
            return factory.cancelled();
        }

        if (hasFlagUnsafe(SUCCEEDED)) {
            return factory.cancelled();
        }

        if (hasFlagUnsafe(FAILED)) {
            return factory.failed(exception);
        }

        ContinuationFuture<V, R> future = new ContinuationFuture<>(this, futureProvider, continuationOption);

        synchronized (progressInfo) {
            if (continuations != null) {
                continuations.add(future);
            } else if (singleContinuation == null) {
                singleContinuation = future;
            } else {
                continuations = new ArrayList<>(3);
                continuations.add(singleContinuation);
                continuations.add(future);
                singleContinuation = null;
            }
        }

        return future;
    }

    @Synchronized
    boolean hasFlagUnsafe(int flag) {
        return (flags & flag) != 0;
    }

    @Synchronized
    void setFlagUnsafe(int flag) {
        flags = flags | flag;
    }

    @Synchronized
    void clearFlagUnsafe(int flag) {
        flags = flags & ~flag;
    }

    synchronized boolean hasFlag(int flag) {
        return hasFlagUnsafe(flag);
    }

    synchronized void setFlag(int flag) {
        setFlagUnsafe(flag);
    }

    synchronized void clearFlag(int flag) {
        clearFlagUnsafe(flag);
    }

    @Override
    public String toString() {
        String flags;
        if (hasFlag(SUCCEEDED)) {
            flags = "SUCCEEDED";
        } else if (hasFlag(FAILED)) {
            flags = "FAILED";
        } else if (hasFlag(CANCELLED)) {
            flags = "CANCELLED";
        } else if (hasFlag(RUNNING)) {
            flags = "RUNNING";
        } else {
            flags = "PENDING";
        }

        return super.toString() + "[" + flags + "]";
    }

}
