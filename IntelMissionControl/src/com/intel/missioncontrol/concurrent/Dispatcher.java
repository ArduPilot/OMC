/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatcher can be used in conjunction with {@link FluentFuture} to offload work from the UI thread to a background
 * thread.
 *
 * <p>In this example, the doHeavyWorkAsync() method defers work to a background thread, and returns a ListenableFuture
 * that we can use to be notified when the operation has finished:
 *
 * <pre>{@code
 * class HeavyWorker {
 *     ListenableFuture<int> doHeavyWorkAsync() {
 *         return Dispatcher.post(() -> {
 *             int result = longRunningOperation();
 *             return result;
 *         }
 *     }
 *
 *     public static void main() {
 *         FluentFuture.from(doHeavyWorkAsync())
 *             .onFailure(exception -> System.err.println(exception))
 *             .onSuccess(result -> System.out.println("The result is: " + result));
 *     }
 * }
 *
 * }</pre>
 *
 * <p>The FluentFuture class is a wrapper around an existing ListenableFuture instance and provides an easier-to-use
 * interface. When used with Dispatcher, it also avoids deadlocking the JavaFX application thread.
 *
 * <p>If the operation that has been deferred to a background thread needs to execute code on the JavaFX application
 * thread, it can do so via Dispatcher::runOnUI(). This method will block the background thread until the work item has
 * been processed by the JavaFX application thread. This is particularly useful when UI elements need to be accessed
 * from the background thread.
 *
 * <p>Note of caution: If the background thread uses Dispatcher::runOnUI() to execute code on the JavaFX application
 * thread, the ListenableFuture must not be waited on via ListenableFuture::get(). To prevent deadlocking the JavaFX
 * application thread, a RuntimeException will be thrown in this situation.
 */
public class Dispatcher {

    private static Logger LOGGER = LoggerFactory.getLogger(Dispatcher.class);

    private static class RunNowData<T> {
        T result;
        Exception exception;
    }

    static AtomicBoolean deadlockAvoidanceFlag = new AtomicBoolean();

    private static int threadCount;

    private static final ListeningExecutorService BACKGROUND =
        MoreExecutors.listeningDecorator(
            java.util.concurrent.Executors.newCachedThreadPool(
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("Background execution thread " + threadCount++);
                    thread.setDaemon(true);
                    return thread;
                }));

    private static final ListeningScheduledExecutorService BACKGROUND_SCHEDULED =
        MoreExecutors.listeningDecorator(
            java.util.concurrent.Executors.newScheduledThreadPool(
                2,
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("Background execution thread " + threadCount++);
                    thread.setDaemon(true);
                    return thread;
                }));

    private static final ListeningExecutorService DIRECT = MoreExecutors.newDirectExecutorService();

    public static ListeningExecutorService getBackgroundExecutorService() {
        return BACKGROUND;
    }

    /** Executes the specified Callable on the current thread. */
    public static <T> ListenableFuture<T> run(Callable<T> callable) {
        return DIRECT.submit(callable);
    }

    /** Executes the specified Runnable on the current thread. */
    public static ListenableFuture<?> run(Runnable runnable) {
        return DIRECT.submit(runnable);
    }

    /** Defers execution of the specified Callable to a background thread and returns immediately. */
    public static <T> FluentFuture<T> post(Callable<T> callable) {
        return FluentFuture.from(BACKGROUND.submit(callable));
    }

    /** Defers execution of the specified Callable to a background thread and returns immediately. */
    public static <T> FluentFuture<T> post(Callable<T> callable, CancellationToken cancellationToken) {
        var future = FluentFuture.from(BACKGROUND.submit(callable));
        cancellationToken.addListener(() -> future.cancel(false));
        return future;
    }

    /** Defers execution of the specified Callable to a background thread and returns immediately. */
    public static <T> FluentFuture<T> post(CancellableCallable<T> callable, CancellationToken cancellationToken) {
        var future = FluentFuture.from(BACKGROUND.submit(() -> callable.call(cancellationToken)));
        cancellationToken.addListener(() -> future.cancel(false));
        return future;
    }

    /** Defers execution of the specified Runnable to a background thread and returns immediately. */
    @SuppressWarnings("unchecked")
    public static FluentFuture<Void> post(Runnable runnable) {
        return (FluentFuture<Void>)FluentFuture.from(BACKGROUND.submit(runnable));
    }

    /** Defers execution of the specified Runnable to a background thread and returns immediately. */
    @SuppressWarnings("unchecked")
    public static FluentFuture<Void> post(Runnable runnable, CancellationToken cancellationToken) {
        var future = (FluentFuture<Void>)FluentFuture.from(BACKGROUND.submit(runnable));
        cancellationToken.addListener(() -> future.cancel(false));
        return future;
    }

    /** Defers execution of the specified Runnable to a background thread and returns immediately. */
    @SuppressWarnings("unchecked")
    public static FluentFuture<Void> post(CancellableRunnable runnable, CancellationToken cancellationToken) {
        var future = (FluentFuture<Void>)FluentFuture.from(BACKGROUND.submit(() -> runnable.run(cancellationToken)));
        cancellationToken.addListener(() -> future.cancel(false));
        return future;
    }

    /** Defers execution of the specified Callable to a background thread and returns immediately. */
    public static <T> FluentScheduledFuture<T> schedule(Callable<T> callable, Duration delay) {
        return FluentScheduledFuture.from(
            BACKGROUND_SCHEDULED.schedule(callable, (int)delay.toMillis(), TimeUnit.MILLISECONDS));
    }

    /** Defers execution of the specified Callable to a background thread and returns immediately. */
    public static <T> FluentScheduledFuture<T> schedule(
            Callable<T> callable, Duration delay, CancellationToken cancellationToken) {
        var future =
            FluentScheduledFuture.from(
                BACKGROUND_SCHEDULED.schedule(callable, (int)delay.toMillis(), TimeUnit.MILLISECONDS));
        cancellationToken.addListener(() -> future.cancel(false));
        return future;
    }

    /** Defers execution of the specified Callable to a background thread and returns immediately. */
    public static <T> FluentScheduledFuture<T> schedule(
            CancellableCallable<T> callable, Duration delay, CancellationToken cancellationToken) {
        var future =
            FluentScheduledFuture.from(
                BACKGROUND_SCHEDULED.schedule(
                    () -> callable.call(cancellationToken), (int)delay.toMillis(), TimeUnit.MILLISECONDS));
        cancellationToken.addListener(() -> future.cancel(false));
        return future;
    }

    /** Defers execution of the specified Runnable to a background thread and returns immediately. */
    @SuppressWarnings("unchecked")
    public static FluentScheduledFuture<Void> schedule(Runnable runnable, Duration delay) {
        return (FluentScheduledFuture<Void>)
            FluentScheduledFuture.from(
                BACKGROUND_SCHEDULED.schedule(runnable, (int)delay.toMillis(), TimeUnit.MILLISECONDS));
    }

    /** Defers execution of the specified Runnable to a background thread and returns immediately. */
    @SuppressWarnings("unchecked")
    public static FluentScheduledFuture<Void> schedule(
            Runnable runnable, Duration delay, CancellationToken cancellationToken) {
        var future =
            (FluentScheduledFuture<Void>)
                FluentScheduledFuture.from(
                    BACKGROUND_SCHEDULED.schedule(runnable, (int)delay.toMillis(), TimeUnit.MILLISECONDS));
        cancellationToken.addListener(() -> future.cancel(false));
        return future;
    }

    /** Defers execution of the specified Runnable to a background thread and returns immediately. */
    @SuppressWarnings("unchecked")
    public static FluentScheduledFuture<Void> schedule(
            CancellableRunnable runnable, Duration delay, CancellationToken cancellationToken) {
        var future =
            (FluentScheduledFuture<Void>)
                FluentScheduledFuture.from(
                    BACKGROUND_SCHEDULED.schedule(
                        () -> runnable.run(cancellationToken), (int)delay.toMillis(), TimeUnit.MILLISECONDS));
        cancellationToken.addListener(() -> future.cancel(false));
        return future;
    }

    /** Defers execution of the specified Callable to a background thread and returns immediately. */
    @SuppressWarnings("unchecked")
    public static <Void> FluentScheduledFuture<Void> schedule(Runnable runnable, Duration delay, Duration period) {
        return (FluentScheduledFuture<Void>)
            FluentScheduledFuture.from(
                BACKGROUND_SCHEDULED.scheduleWithFixedDelay(
                    runnable, (int)delay.toMillis(), (int)period.toMillis(), TimeUnit.MILLISECONDS));
    }

    /** Defers execution of the specified Callable to a background thread and returns immediately. */
    @SuppressWarnings("unchecked")
    public static <Void> FluentScheduledFuture<Void> schedule(
            Runnable runnable, Duration delay, Duration period, CancellationToken cancellationToken) {
        var future =
            (FluentScheduledFuture<Void>)
                FluentScheduledFuture.from(
                    BACKGROUND_SCHEDULED.scheduleWithFixedDelay(
                        runnable, (int)delay.toMillis(), (int)period.toMillis(), TimeUnit.MILLISECONDS));
        cancellationToken.addListener(() -> future.cancel(false));
        return future;
    }

    /** Defers execution of the specified Callable to a background thread and returns immediately. */
    @SuppressWarnings("unchecked")
    public static <Void> FluentScheduledFuture<Void> schedule(
            CancellableRunnable runnable, Duration delay, Duration period, CancellationToken cancellationToken) {
        var future =
            (FluentScheduledFuture<Void>)
                FluentScheduledFuture.from(
                    BACKGROUND_SCHEDULED.scheduleWithFixedDelay(
                        () -> runnable.run(cancellationToken),
                        (int)delay.toMillis(),
                        (int)period.toMillis(),
                        TimeUnit.MILLISECONDS));
        cancellationToken.addListener(() -> future.cancel(false));
        return future;
    }

    /**
     * Defers execution of the specified Runnable to the UI thread, or executes the Runnable directly if invoked from
     * the UI thread.
     */
    public static FluentFuture<Void> dispatchToUI(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            try {
                runnable.run();
                return FluentFuture.fromResult(null);
            } catch (Throwable e) {
                LOGGER.debug("Exception in async execution:", e);
                return FluentFuture.fromThrowable(e);
            }
        } else {
            return postToUI(runnable);
        }
    }

    /** Defers execution of the specified Runnable to the UI thread and returns immediately. */
    public static FluentFuture<Void> postToUI(Runnable runnable) {
        var future = SettableFuture.<Void>create();
        Platform.runLater(
            () -> {
                try {
                    runnable.run();
                    future.set(null);
                } catch (Throwable e) {
                    LOGGER.debug("Exception in async execution:", e);
                    future.setException(e);
                }
            });

        return FluentFuture.from(future);
    }

    /** Defers execution of the specified Callable to the UI thread and returns immediately. */
    public static <T> FluentFuture<T> postToUI(Callable<T> runnable) {
        var future = SettableFuture.<T>create();
        Platform.runLater(
            () -> {
                try {
                    T result = runnable.call();
                    future.set(result);
                } catch (Throwable e) {
                    LOGGER.debug("Exception in async execution:", e);
                    future.setException(e);
                }
            });

        return FluentFuture.from(future);
    }

    /** Defers execution of the specified Runnable to the UI thread and returns immediately. */
    public static FluentScheduledFuture<Void> scheduleOnUI(Runnable runnable, Duration delay) {
        return schedule(() -> Platform.runLater(runnable), delay);
    }

    /** Defers execution of the specified Runnable to the UI thread and returns immediately. */
    public static FluentScheduledFuture<Void> scheduleOnUI(Runnable runnable, Duration delay, Duration period) {
        return schedule(() -> Platform.runLater(runnable), delay, period);
    }

    /** Defers execution of the specified Runnable to the UI thread and returns immediately. */
    public static FluentScheduledFuture<Void> scheduleOnUI(
            Runnable runnable, Duration delay, CancellationToken cancellationToken) {
        return schedule(() -> Platform.runLater(runnable), delay, cancellationToken);
    }

    /** Defers execution of the specified Runnable to the UI thread and returns immediately. */
    public static FluentScheduledFuture<Void> scheduleOnUI(
            Runnable runnable, Duration delay, Duration period, CancellationToken cancellationToken) {
        return schedule(() -> Platform.runLater(runnable), delay, period, cancellationToken);
    }

    /** Defers execution of the specified Runnable to the UI thread and returns immediately. */
    public static FluentScheduledFuture<Void> scheduleOnUI(
            CancellableRunnable runnable, Duration delay, CancellationToken cancellationToken) {
        return schedule(() -> Platform.runLater(() -> runnable.run(cancellationToken)), delay, cancellationToken);
    }

    /** Defers execution of the specified Runnable to the UI thread and returns immediately. */
    public static FluentScheduledFuture<Void> scheduleOnUI(
            CancellableRunnable runnable, Duration delay, Duration period, CancellationToken cancellationToken) {
        return schedule(() -> Platform.runLater(() -> runnable.run(cancellationToken)), delay, cancellationToken);
    }

    /** Executes the specified Runnable to the UI thread and waits for the operation to finish. */
    public static void runOnUI(final Runnable runnable) {
        boolean fxAppThread = Platform.isFxApplicationThread();
        if (fxAppThread) {
            runnable.run();
            return;
        }

        final ResetEvent resetEvent = new ResetEvent();
        final RunNowData<Void> data = new RunNowData<>();

        Platform.runLater(
            () -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    data.exception = e;
                }

                resetEvent.set();
            });

        // this codes doesent work, if two different threads are in parallel waiting for the UI which is valid due to
        // the Recomputer framework
        /*if (deadlockAvoidanceFlag.getAndSet(true)) {
            throw new RuntimeException(
                "A deadlock was detected. Avoid calling "
                    + FluentFuture.class.getSimpleName()
                    + "::get() from the JavaFX application thread.");
        }*/

        try {
            resetEvent.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // deadlockAvoidanceFlag.set(false);
        }

        if (data.exception != null) {
            if (data.exception instanceof RuntimeException) {
                throw (RuntimeException)data.exception;
            } else {
                throw new RuntimeException(data.exception);
            }
        }
    }

    /** Executes the specified Callable to the UI thread and waits for the operation to finish. */
    public static <T> T runOnUI(final Callable<T> runnable) {
        boolean fxAppThread = Platform.isFxApplicationThread();
        if (fxAppThread) {
            try {
                return runnable.call();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        final ResetEvent resetEvent = new ResetEvent();
        final RunNowData<T> data = new RunNowData<>();

        Platform.runLater(
            () -> {
                try {
                    data.result = runnable.call();
                } catch (Exception e) {
                    data.exception = e;
                }

                resetEvent.set();
            });

        // this codes doesent work, if two different threads are in parallel waiting for the UI which is valid due to
        // the Recomputer framework
        /*if (deadlockAvoidanceFlag.getAndSet(true)) {
            throw new RuntimeException(
                "A deadlock was detected. Avoid calling "
                    + FluentFuture.class.getSimpleName()
                    + "::get() from the JavaFX application thread.");
        }*/

        try {
            resetEvent.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // deadlockAvoidanceFlag.set(false);
        }

        if (data.exception != null) {
            if (data.exception instanceof RuntimeException) {
                throw (RuntimeException)data.exception;
            } else {
                throw new RuntimeException(data.exception);
            }
        }

        return data.result;
    }

    static class Timer {

        private final Duration delay;
        private final Consumer<Timer> action;
        private final Timeline timeline;
        private int serial = 0;

        Timer(Duration delay, Consumer<Timer> action) {
            this.delay = delay;
            this.action = action;
            this.timeline = new Timeline();
            timeline.getKeyFrames().add(new KeyFrame(delay));
            timeline.setCycleCount(1);
        }

        public void start() {
            stop();

            final int expectedSerial = this.serial;
            timeline.getKeyFrames()
                .set(
                    0,
                    new KeyFrame(
                        delay,
                        event -> {
                            if (expectedSerial == serial) {
                                action.accept(this);
                            }
                        }));

            timeline.play();
        }

        public void stop() {
            timeline.stop();
            serial++;
        }

    }

}
