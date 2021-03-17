/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import static org.asyncfx.concurrent.FutureFlags.ALREADY_EXECUTED;

import com.google.common.util.concurrent.MoreExecutors;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import javafx.application.Platform;
import org.asyncfx.AsyncFX;
import org.asyncfx.concurrent.Future.RunnableWithProgress;
import org.asyncfx.concurrent.Future.SupplierWithProgress;
import org.jetbrains.annotations.NotNull;

class PlatformFutureFactory implements Executor {

    static PlatformFutureFactory getInstance() {
        return INSTANCE;
    }

    private static final PlatformFutureFactory INSTANCE = new PlatformFutureFactory();

    @Override
    public void execute(@NotNull Runnable command) {
        Platform.runLater(
            () -> {
                long startTime = System.nanoTime();
                command.run();
                AsyncFX.Accessor.trackPlatformSubmit(System.nanoTime() - startTime);
            });
    }

    Future<Void> newRunFuture(Runnable runnable) {
        return runImpl(runnable, Duration.ZERO, Duration.ZERO, CancellationSource.DEFAULT);
    }

    Future<Void> newRunFuture(Runnable runnable, Duration delay) {
        return runImpl(runnable, delay, Duration.ZERO, CancellationSource.DEFAULT);
    }

    Future<Void> newRunFuture(Runnable runnable, CancellationSource cancellationSource) {
        return runImpl(runnable, Duration.ZERO, Duration.ZERO, cancellationSource);
    }

    Future<Void> newRunFuture(Runnable runnable, Duration delay, CancellationSource cancellationSource) {
        return runImpl(runnable, delay, Duration.ZERO, cancellationSource);
    }

    Future<Void> newRunFuture(Runnable runnable, Duration delay, Duration period) {
        return runImpl(runnable, delay, period, CancellationSource.DEFAULT);
    }

    Future<Void> newRunFuture(
            Runnable runnable, Duration delay, Duration period, CancellationSource cancellationSource) {
        return runImpl(runnable, delay, period, cancellationSource);
    }

    Future<Void> newRunFuture(RunnableWithProgress runnable) {
        return runImpl(runnable, Duration.ZERO, Duration.ZERO, CancellationSource.DEFAULT);
    }

    Future<Void> newRunFuture(RunnableWithProgress runnable, Duration delay) {
        return runImpl(runnable, delay, Duration.ZERO, CancellationSource.DEFAULT);
    }

    Future<Void> newRunFuture(RunnableWithProgress runnable, CancellationSource cancellationSource) {
        return runImpl(runnable, Duration.ZERO, Duration.ZERO, cancellationSource);
    }

    Future<Void> newRunFuture(RunnableWithProgress runnable, Duration delay, CancellationSource cancellationSource) {
        return runImpl(runnable, delay, Duration.ZERO, cancellationSource);
    }

    Future<Void> newRunFuture(RunnableWithProgress runnable, Duration delay, Duration period) {
        return runImpl(runnable, delay, period, CancellationSource.DEFAULT);
    }

    Future<Void> newRunFuture(
            RunnableWithProgress runnable, Duration delay, Duration period, CancellationSource cancellationSource) {
        return runImpl(runnable, delay, period, cancellationSource);
    }

    <V> Future<V> newGetFuture(Supplier<V> supplier) {
        return getImpl(supplier, Duration.ZERO, CancellationSource.DEFAULT);
    }

    <V> Future<V> newGetFuture(Supplier<V> supplier, Duration delay) {
        return getImpl(supplier, delay, CancellationSource.DEFAULT);
    }

    <V> Future<V> newGetFuture(Supplier<V> supplier, CancellationSource cancellationSource) {
        return getImpl(supplier, Duration.ZERO, cancellationSource);
    }

    <V> Future<V> newGetFuture(Supplier<V> supplier, Duration delay, CancellationSource cancellationSource) {
        return getImpl(supplier, delay, cancellationSource);
    }

    <V> Future<V> newGetFuture(SupplierWithProgress<V> supplier) {
        return getImpl(supplier, Duration.ZERO, CancellationSource.DEFAULT);
    }

    <V> Future<V> newGetFuture(SupplierWithProgress<V> supplier, Duration delay) {
        return getImpl(supplier, delay, CancellationSource.DEFAULT);
    }

    <V> Future<V> newGetFuture(SupplierWithProgress<V> supplier, CancellationSource cancellationSource) {
        return getImpl(supplier, Duration.ZERO, cancellationSource);
    }

    <V> Future<V> newGetFuture(
            SupplierWithProgress<V> supplier, Duration delay, CancellationSource cancellationSource) {
        return getImpl(supplier, delay, cancellationSource);
    }

    private Future<Void> runImpl(
            Runnable runnable, Duration delay, Duration period, CancellationSource cancellationSource) {
        PlatformFuture<Void> future = new PlatformFuture<>();
        cancellationSource.registerFuture(future);

        if (delay.isZero()) {
            Platform.runLater(
                () -> {
                    long startTime = System.nanoTime();

                    try {
                        runnable.run();

                        if (future.getProgressInfo().isCancellationRequested()) {
                            future.completeWithCancellation(null);
                        } else {
                            future.completeWithResult(null);
                        }
                    } catch (AssertionError assertionError) {
                        future.completeWithException(assertionError);
                        throw assertionError;
                    } catch (Throwable throwable) {
                        future.completeWithException(throwable);
                    }

                    long duration = System.nanoTime() - startTime;
                    AsyncFX.Accessor.trackPlatformSubmit(duration);
                });
        } else {
            Runnable runLaterRunnable =
                () -> {
                    future.setFlag(ALREADY_EXECUTED);

                    Platform.runLater(
                        () -> {
                            long startTime = System.nanoTime();

                            try {
                                runnable.run();

                                if (future.getProgressInfo().isCancellationRequested()) {
                                    future.completeWithCancellation(null);
                                } else {
                                    future.completeWithResult(null);
                                }
                            } catch (AssertionError assertionError) {
                                future.completeWithException(assertionError);
                                throw assertionError;
                            } catch (Throwable throwable) {
                                future.completeWithException(throwable);
                            }

                            long duration = System.nanoTime() - startTime;
                            AsyncFX.Accessor.trackPlatformSubmit(duration);
                        });
                };

            Future<Void> scheduledFuture =
                period.isZero() || period.isNegative()
                    ? DefaultFutureFactory.getInstance()
                        .newRunFuture(runLaterRunnable, delay, CancellationSource.DEFAULT)
                    : DefaultFutureFactory.getInstance()
                        .newRunFuture(runLaterRunnable, delay, period, CancellationSource.DEFAULT);

            scheduledFuture.addListener(
                () -> {
                    if (!future.hasFlag(ALREADY_EXECUTED)) {
                        Platform.runLater(() -> future.completeWithCancellation(null));
                    }
                },
                MoreExecutors.directExecutor());

            future.setCancellationHandler(scheduledFuture::cancel);
        }

        return future;
    }

    private Future<Void> runImpl(
            RunnableWithProgress runnable, Duration delay, Duration period, CancellationSource cancellationSource) {
        PlatformFuture<Void> future = new PlatformFuture<>();
        cancellationSource.registerFuture(future);

        if (delay.isZero()) {
            Platform.runLater(
                () -> {
                    long startTime = System.nanoTime();

                    try {
                        runnable.run(future.getProgressInfo());

                        if (future.getProgressInfo().isCancellationRequested()) {
                            future.completeWithCancellation(null);
                        } else {
                            future.completeWithResult(null);
                        }
                    } catch (AssertionError assertionError) {
                        future.completeWithException(assertionError);
                        throw assertionError;
                    } catch (Throwable throwable) {
                        future.completeWithException(throwable);
                    }

                    long duration = System.nanoTime() - startTime;
                    AsyncFX.Accessor.trackPlatformSubmit(duration);
                });
        } else {
            Runnable runLaterRunnable =
                () -> {
                    future.setFlag(ALREADY_EXECUTED);

                    Platform.runLater(
                        () -> {
                            long startTime = System.nanoTime();

                            try {
                                runnable.run(future.getProgressInfo());

                                if (future.getProgressInfo().isCancellationRequested()) {
                                    future.completeWithCancellation(null);
                                } else {
                                    future.completeWithResult(null);
                                }
                            } catch (AssertionError assertionError) {
                                future.completeWithException(assertionError);
                                throw assertionError;
                            } catch (Throwable throwable) {
                                future.completeWithException(throwable);
                            }

                            long duration = System.nanoTime() - startTime;
                            AsyncFX.Accessor.trackPlatformSubmit(duration);
                        });
                };

            Future<Void> scheduledFuture =
                period.isZero() || period.isNegative()
                    ? DefaultFutureFactory.getInstance()
                        .newRunFuture(runLaterRunnable, delay, CancellationSource.DEFAULT)
                    : DefaultFutureFactory.getInstance()
                        .newRunFuture(runLaterRunnable, delay, period, CancellationSource.DEFAULT);

            scheduledFuture.addListener(
                () -> {
                    if (!future.hasFlag(ALREADY_EXECUTED)) {
                        Platform.runLater(() -> future.completeWithCancellation(null));
                    }
                },
                MoreExecutors.directExecutor());

            future.setCancellationHandler(scheduledFuture::cancel);
        }

        return future;
    }

    private <V> Future<V> getImpl(Supplier<V> supplier, Duration delay, CancellationSource cancellationSource) {
        PlatformFuture<V> future = new PlatformFuture<>();
        cancellationSource.registerFuture(future);

        if (delay.isZero()) {
            Platform.runLater(
                () -> {
                    long startTime = System.nanoTime();

                    try {
                        V value = supplier.get();

                        if (future.getProgressInfo().isCancellationRequested()) {
                            future.completeWithCancellation(null);
                        } else {
                            future.completeWithResult(value);
                        }
                    } catch (AssertionError assertionError) {
                        future.completeWithException(assertionError);
                        throw assertionError;
                    } catch (Throwable throwable) {
                        future.completeWithException(throwable);
                    }

                    long duration = System.nanoTime() - startTime;
                    AsyncFX.Accessor.trackPlatformSubmit(duration);
                });
        } else {
            Future<Void> scheduledFuture =
                DefaultFutureFactory.getInstance()
                    .newRunFuture(
                        () -> {
                            future.setFlag(ALREADY_EXECUTED);

                            Platform.runLater(
                                () -> {
                                    long startTime = System.nanoTime();

                                    try {
                                        V value = supplier.get();

                                        if (future.getProgressInfo().isCancellationRequested()) {
                                            future.completeWithCancellation(null);
                                        } else {
                                            future.completeWithResult(value);
                                        }
                                    } catch (AssertionError assertionError) {
                                        future.completeWithException(assertionError);
                                        throw assertionError;
                                    } catch (Throwable throwable) {
                                        future.completeWithException(throwable);
                                    }

                                    long duration = System.nanoTime() - startTime;
                                    AsyncFX.Accessor.trackPlatformSubmit(duration);
                                });
                        },
                        delay,
                        CancellationSource.DEFAULT);

            scheduledFuture.addListener(
                () -> {
                    if (!future.hasFlag(ALREADY_EXECUTED)) {
                        Platform.runLater(() -> future.completeWithCancellation(null));
                    }
                },
                MoreExecutors.directExecutor());

            future.setCancellationHandler(scheduledFuture::cancel);
        }

        return future;
    }

    private <V> Future<V> getImpl(
            SupplierWithProgress<V> supplier, Duration delay, CancellationSource cancellationSource) {
        PlatformFuture<V> future = new PlatformFuture<>();
        cancellationSource.registerFuture(future);

        if (delay.isZero()) {
            Platform.runLater(
                () -> {
                    long startTime = System.nanoTime();

                    try {
                        V value = supplier.get(future.getProgressInfo());

                        if (future.getProgressInfo().isCancellationRequested()) {
                            future.completeWithCancellation(null);
                        } else {
                            future.completeWithResult(value);
                        }
                    } catch (AssertionError assertionError) {
                        future.completeWithException(assertionError);
                        throw assertionError;
                    } catch (Throwable throwable) {
                        future.completeWithException(throwable);
                    }

                    long duration = System.nanoTime() - startTime;
                    AsyncFX.Accessor.trackPlatformSubmit(duration);
                });
        } else {
            Future<Void> scheduledFuture =
                DefaultFutureFactory.getInstance()
                    .newRunFuture(
                        () -> {
                            future.setFlag(ALREADY_EXECUTED);

                            Platform.runLater(
                                () -> {
                                    long startTime = System.nanoTime();

                                    try {
                                        V value = supplier.get(future.getProgressInfo());

                                        if (future.getProgressInfo().isCancellationRequested()) {
                                            future.completeWithCancellation(null);
                                        } else {
                                            future.completeWithResult(value);
                                        }
                                    } catch (AssertionError assertionError) {
                                        future.completeWithException(assertionError);
                                        throw assertionError;
                                    } catch (Throwable throwable) {
                                        future.completeWithException(throwable);
                                    }

                                    long duration = System.nanoTime() - startTime;
                                    AsyncFX.Accessor.trackPlatformSubmit(duration);
                                });
                        },
                        delay,
                        CancellationSource.DEFAULT);

            scheduledFuture.addListener(
                () -> {
                    if (!future.hasFlag(ALREADY_EXECUTED)) {
                        Platform.runLater(() -> future.completeWithCancellation(null));
                    }
                },
                MoreExecutors.directExecutor());

            future.setCancellationHandler(scheduledFuture::cancel);
        }

        return future;
    }

}
