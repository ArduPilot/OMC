/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.application.Platform;
import org.asyncfx.AsyncFX;
import org.asyncfx.Awaiter;
import org.asyncfx.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FutureTest extends TestBase {

    @BeforeAll
    static void init() {
        loadClass(PlatformDispatcher.class);
        loadClass(BackgroundDispatcher.class);
        loadClass(ContinuationFuture.class);
        loadClass(SuccessfulFuture.class);
        loadClass(FailedFuture.class);
        loadClass(CancelledFuture.class);
        loadClass(RunnableFuture.class);
        loadClass(CallableFuture.class);
        loadClass(FutureCompletionSource.class);
        loadClass(Futures.class);
        loadClass(FutureExecutorService.class);
        loadClass(DefaultFutureFactory.class);
        loadClass(PlatformFutureFactory.class);
        loadClass(ReentrantStampedLock.class);
    }

    @Nested
    class WhenContinuations {
        @Test
        void whenSucceeded_Is_Called() {
            var awaiter = new Awaiter();

            for (int i = 0; i < 500; ++i) {
                Dispatcher.background()
                    .getLaterAsync(() -> 1)
                    .whenFailed(ex -> awaiter.fail())
                    .whenCancelled(awaiter::fail)
                    .whenSucceeded(
                        res -> {
                            awaiter.assertEquals(1, (int)res);
                            awaiter.signal();
                        });
            }

            awaiter.await(500);
        }

        @Test
        void whenFailed_Is_Called() {
            var awaiter = new Awaiter();

            for (int i = 0; i < 500; ++i) {
                Dispatcher.background()
                    .getLaterAsync(
                        () -> {
                            throw new RuntimeException("foo");
                        })
                    .whenSucceeded(res -> awaiter.fail())
                    .whenCancelled(awaiter::fail)
                    .whenFailed(
                        ex -> {
                            awaiter.assertEquals("foo", ex.getCause().getMessage());
                            awaiter.signal();
                        });
            }

            awaiter.await(500);
        }

        @Test
        void whenCancelled_Is_Called_After_Future_Has_Completed() {
            var awaiter = new Awaiter();

            var future =
                Dispatcher.background()
                    .runLaterAsync(
                        () -> {
                            storeInt("flag", 1);
                            sleep(1000);
                            storeInt("flag", 2);
                        });

            future.whenFailed(ex -> awaiter.fail());
            future.whenSucceeded(res -> awaiter.fail());
            future.whenCancelled(
                () -> {
                    awaiter.assertEquals(2, loadInt("flag"));
                    awaiter.signal();
                });

            future.cancel(false);

            awaiter.await(1);
        }

        @Test
        void whenCancelled_Is_Called_Immediately_When_Interrupting() {
            var awaiter = new Awaiter();

            var future =
                Dispatcher.background()
                    .runLaterAsync(
                        () -> {
                            storeInt("flag", 1);

                            try {
                                Thread.sleep(2000);
                                storeInt("flag", 2);
                            } catch (InterruptedException ex) {
                                storeInt("flag", 3);
                            }
                        });

            future.whenFailed(ex -> awaiter.fail());
            future.whenSucceeded(res -> awaiter.fail());
            future.whenCancelled(
                () -> {
                    awaiter.assertEquals(3, loadInt("flag"));
                    awaiter.signal();
                });

            sleep(100);
            future.cancel(true);

            awaiter.await(1);
        }

        @Test
        void whenCancelled_Is_Immediately_Called_When_Using_External_Future() {
            var awaiter = new Awaiter();

            ListeningExecutorService executorService =
                MoreExecutors.listeningDecorator(java.util.concurrent.Executors.newSingleThreadExecutor());

            var externalFuture =
                executorService.submit(
                    () -> {
                        sleep(2000);
                        storeBoolean("flag", true);
                    });

            var future =
                Futures.fromListenableFuture(externalFuture)
                    .whenFailed(ex -> awaiter.fail())
                    .whenSucceeded(res -> awaiter.fail())
                    .whenCancelled(
                        () -> {
                            awaiter.assertFalse(loadBoolean("flag"));
                            awaiter.signal();
                        });

            sleep(100);
            future.cancel(false);

            awaiter.await(1);
        }

        @Test
        void whenCancelled_Is_Immediately_Called_When_External_Future_Is_Cancelled_Externally() {
            var awaiter = new Awaiter();

            ListeningExecutorService executorService =
                MoreExecutors.listeningDecorator(java.util.concurrent.Executors.newSingleThreadExecutor());

            var externalFuture =
                executorService.submit(
                    () -> {
                        sleep(2000);
                        storeBoolean("flag", true);
                    });

            Futures.fromListenableFuture(externalFuture)
                .whenFailed(ex -> awaiter.fail())
                .whenSucceeded(res -> awaiter.fail())
                .whenCancelled(
                    () -> {
                        awaiter.assertFalse(loadBoolean("flag"));
                        awaiter.signal();
                    });

            sleep(100);
            externalFuture.cancel(false);

            awaiter.await(1);
        }
    }

    @Nested
    class ThenContinuations {
        @SuppressWarnings("Convert2MethodRef")
        @Test
        void thenRun_Elides_FutureExecutorService_Submit() {
            var awaiter = new Awaiter();

            // reset the counter
            AsyncFX.getAsyncSubmitCount();
            AsyncFX.getElidedAsyncSubmitCount();

            // Note: In each of the scenarios, we have to sleep(50) to make sure we don't get an immediately completed
            // future. If we received an immediately completed future, then the next thenRun would be submitted to FES.

            // Scenario 1:
            long millis = System.currentTimeMillis();
            Dispatcher.background()
                .runLaterAsync(() -> sleep(50)) // submitted to FES
                .thenRun(() -> sleep(50)) // runs synchronously
                .thenRun(() -> sleep(50)) // runs synchronously
                .whenDone(f -> awaiter.signal());

            var elapsed = System.currentTimeMillis() - millis;
            Assertions.assertTrue(elapsed < 50, "Elapsed time: " + elapsed);
            awaiter.await(1);
            Assertions.assertEquals(1, AsyncFX.getAsyncSubmitCount());

            // Scenario 2:
            millis = System.currentTimeMillis();
            Futures.successful(0) // runs synchronously
                .thenRun(() -> sleep(50)) // submitted to FES
                .thenRun(() -> sleep(50)) // runs synchronously
                .whenDone(f -> awaiter.signal());

            elapsed = System.currentTimeMillis() - millis;
            Assertions.assertTrue(elapsed < 50, "elapsed time: " + elapsed);
            awaiter.await(1);
            Assertions.assertEquals(1, AsyncFX.getAsyncSubmitCount());

            // Scenario 3:
            millis = System.currentTimeMillis();
            Dispatcher.background()
                .runLaterAsync(() -> sleep(50)) // submitted to FES
                .thenRunAsync(() -> Dispatcher.background().runLaterAsync(() -> sleep(50))) // runs synchronously
                .thenRunAsync(() -> Dispatcher.background().runLaterAsync(() -> sleep(50))) // runs synchronously
                .whenDone(f -> awaiter.signal());

            elapsed = System.currentTimeMillis() - millis;
            Assertions.assertTrue(elapsed < 50, "elapsed time: " + elapsed);
            awaiter.await(1);
            Assertions.assertEquals(1, AsyncFX.getAsyncSubmitCount());

            // Scenario 4:
            millis = System.currentTimeMillis();
            Futures.successful(0) // runs synchronously
                .thenRunAsync(() -> Dispatcher.background().runLaterAsync(() -> sleep(50))) // submitted to FES
                .thenRunAsync(() -> Dispatcher.background().runLaterAsync(() -> sleep(50))) // runs synchronously
                .whenDone(f -> awaiter.signal());

            elapsed = System.currentTimeMillis() - millis;
            Assertions.assertTrue(elapsed < 50, "elapsed time: " + elapsed);
            awaiter.await(1);
            Assertions.assertEquals(1, AsyncFX.getAsyncSubmitCount());

            // Scenario 5:
            millis = System.currentTimeMillis();
            Future<Integer> f0 = Futures.successful(0); // runs synchronously
            f0.thenRunAsync(
                    () -> {
                        sleep(50);
                        return Futures.successful();
                    }) // submitted to FES
                .thenRunAsync(() -> Futures.successful()) // runs synchronously
                .whenDone(f -> awaiter.signal());

            elapsed = System.currentTimeMillis() - millis;
            Assertions.assertTrue(elapsed < 50, "elapsed time: " + elapsed);
            awaiter.await(1);
            Assertions.assertEquals(1, AsyncFX.getAsyncSubmitCount());
        }

        @Test
        void Immediately_Completed_Future_Submits_Continuation_To_FutureExecutorService() {
            var awaiter = new Awaiter();

            // reset the counter
            AsyncFX.getAsyncSubmitCount();
            AsyncFX.getElidedAsyncSubmitCount();

            long millis = System.currentTimeMillis();
            Futures.successful() // immediately successful
                .thenRun(() -> sleep(50)) // submitted to FES
                .thenRun(() -> sleep(50)) // runs synchronously
                .whenDone(f -> awaiter.signal());

            var elapsed = System.currentTimeMillis() - millis;
            Assertions.assertTrue(elapsed < 50, "elapsed time: " + elapsed);
            awaiter.await(1);
            Assertions.assertEquals(1, AsyncFX.getAsyncSubmitCount());
        }

        @Test
        void thenRun_Does_Not_Execute_When_Previous_Future_Has_Failed() {
            var awaiter = new Awaiter();

            Futures.failed(new RuntimeException())
                .thenRun((Runnable)awaiter::fail)
                .whenDone(
                    f -> {
                        if (f.isFailed()) {
                            awaiter.signal();
                        } else {
                            awaiter.fail();
                        }
                    });

            awaiter.await(1);
        }

        @Test
        void thenRunExceptional_Executes_When_Previous_Future_Has_Failed() {
            var awaiter = new Awaiter();
            AtomicInteger count = new AtomicInteger();

            Futures.failed(new RuntimeException())
                .thenFinallyRun(ex -> count.incrementAndGet())
                .whenDone(
                    f -> {
                        if (count.get() == 1) {
                            awaiter.signal();
                        } else {
                            awaiter.fail();
                        }
                    });

            awaiter.await(1);
        }

        @Test
        void thenApply_Forwards_Return_Value_Of_Previous_Future() {
            var awaiter = new Awaiter();

            var future =
                Dispatcher.background()
                    .getLaterAsync(() -> 1)
                    .thenApply(i -> i + 1)
                    .thenApply(i -> i + 1)
                    .whenSucceeded(f -> awaiter.signal());

            awaiter.await(1);
            Assertions.assertEquals(3, (int)future.getUnchecked());
        }

        @Test
        void thenGetAsync_Runs_On_UI_Thread() {
            var awaiter = new Awaiter();

            for (int i = 0; i < 50; ++i) {
                Dispatcher.background()
                    .runLaterAsync(() -> awaiter.assertTrue(!Platform.isFxApplicationThread()))
                    .thenGetAsync(
                        () ->
                            Dispatcher.platform()
                                .getLaterAsync(
                                    () -> {
                                        awaiter.assertTrue(Platform.isFxApplicationThread());
                                        return 2;
                                    }))
                    .thenAccept(
                        value -> {
                            awaiter.assertTrue(!Platform.isFxApplicationThread());
                            awaiter.assertEquals(2, (int)value);
                        })
                    .whenDone(awaiter::signal);
            }

            awaiter.await(50);
        }

        @Test
        void thenFinallyRun_Continues_When_Preceding_Future_Has_Failed() {
            var awaiter = new Awaiter();

            Dispatcher.background()
                .runLaterAsync(
                    () -> {
                        sleep(100);
                        throw new RuntimeException();
                    })
                .thenFinallyRun(awaiter::fail, ex -> awaiter.signal()) // runs the failure case
                .thenRun(awaiter::signal) // runs always
                .whenDone(awaiter::signal);

            awaiter.await(3);
        }

        @Test
        void thenFinallyRunAsync_Continues_When_Preceding_Future_Has_Failed() {
            var awaiter = new Awaiter();

            Dispatcher.background()
                .runLaterAsync(
                    () -> {
                        sleep(100);
                        throw new RuntimeException();
                    })
                .thenFinallyRunAsync(
                    () -> {
                        awaiter.fail();
                        return Futures.cancelled();
                    },
                    ex -> {
                        awaiter.signal();
                        return Dispatcher.background().runLaterAsync(() -> {}, Duration.ofMillis(100));
                    }) // runs the failure case
                .thenRun(awaiter::signal) // runs always
                .whenDone(awaiter::signal);

            awaiter.await(3);
        }

        @Test
        void Preceding_Futures_Can_Be_GCed() {
            int directExecs = DefaultFutureFactory.getInstance().getMaxDirectExecutions();
            DefaultFutureFactory.getInstance().setMaxDirectExecutions(0);

            var awaiter = new Awaiter();

            var future0 = new WeakReference<>(Dispatcher.background().runLaterAsync(() -> sleep(100)));
            var future1 = new WeakReference<>(future0.get().thenRun(() -> sleep(100)));
            var future2 = new WeakReference<>(future1.get().thenRun(() -> sleep(100)));
            var future3 = new WeakReference<>(future2.get().thenRun(() -> sleep(100)));
            var future4 = new WeakReference<>(future3.get().thenRun(() -> sleep(100)));
            var future5 =
                future4.get()
                    .thenRun(
                        () -> {
                            System.gc();
                            awaiter.waitUntil(() -> future0.get() == null);
                            awaiter.waitUntil(() -> future1.get() == null);
                            awaiter.waitUntil(() -> future2.get() == null);
                            awaiter.waitUntil(() -> future3.get() == null);
                            awaiter.signal();
                        });

            awaiter.await(1);

            DefaultFutureFactory.getInstance().setMaxDirectExecutions(directExecs);
        }
    }

    @Nested
    class Cancellation {
        @Test
        void Future_Is_Not_Cancelled_Cooperatively() {
            var awaiter = new Awaiter();
            var count = new AtomicInteger();

            Future<Void> future =
                Dispatcher.background()
                    .runLaterAsync(
                        () -> {
                            // This method always runs to completion
                            count.addAndGet(1);
                            sleep(250);
                            count.addAndGet(10);
                        })
                    .thenRun((Runnable)awaiter::fail)
                    .whenDone(
                        () -> {
                            // This is executed because the previous thenRun future was cancelled
                            awaiter.assertEquals(11, count.get());
                            awaiter.signal();
                        });

            sleep(50);
            future.cancel(false);
            awaiter.await(1);
        }

        @Test
        void Future_Is_Cancelled_By_Interruption() {
            var awaiter = new Awaiter();
            var count = new AtomicInteger();

            Future<Void> future =
                Dispatcher.background()
                    .runLaterAsync(
                        cancellationToken -> {
                            while (!cancellationToken.isCancellationRequested()) {
                                sleep(100);
                            }

                            count.set(1);
                        })
                    .thenRun((Runnable)awaiter::fail)
                    .whenFailed(
                        f -> {
                            // This is executed because the previous thenRun future failed by throwing a
                            // RuntimeException(InterruptedException)
                            awaiter.assertEquals(0, count.get());
                            awaiter.signal();
                        });

            sleep(50);
            future.cancel(true);
            awaiter.await(1);
        }

        @Test
        void Future_Is_Cancelled_Cooperatively_By_Calling_Cancel() {
            var awaiter = new Awaiter();

            var future =
                Dispatcher.background()
                    .runLaterAsync(
                        cancellationToken -> {
                            int i = 0;
                            for (; i < 20; ++i) {
                                if (cancellationToken.isCancellationRequested()) {
                                    storeBoolean("flag", true);
                                    return;
                                }

                                sleep(50);
                            }
                        })
                    .whenFailed(ex -> awaiter.fail())
                    .whenSucceeded(res -> awaiter.fail())
                    .whenCancelled(
                        () -> {
                            awaiter.assertTrue(loadBoolean("flag"));
                            awaiter.signal();
                        });

            sleep(50);
            future.cancel(false);

            awaiter.await(1);
        }

        @Test
        void Future_Is_Cancelled_Cooperatively_By_CancellationSource() {
            var awaiter = new Awaiter();
            var cts = new CancellationSource();

            Dispatcher.background()
                .runLaterAsync(
                    cancellationToken -> {
                        int i = 0;
                        for (; i < 20; ++i) {
                            if (cancellationToken.isCancellationRequested()) {
                                storeBoolean("flag", true);
                                return;
                            }

                            sleep(50);
                        }
                    },
                    cts)
                .whenFailed(ex -> awaiter.fail())
                .whenSucceeded(res -> awaiter.fail())
                .whenCancelled(
                    () -> {
                        awaiter.assertTrue(loadBoolean("flag"));
                        awaiter.signal();
                    });

            sleep(50);
            cts.cancel();

            awaiter.await(1);
        }

        @Test
        void Cancel_Propagates_To_Predecessor() {
            // test it one way
            AtomicInteger count = new AtomicInteger();
            var future =
                Dispatcher.background()
                    .runLaterAsync(
                        () -> {
                            // doesn't run
                            count.addAndGet(999);
                        },
                        Duration.ofMillis(1000))
                    .whenCancelled(
                        () -> {
                            // runs
                            count.addAndGet(1);
                        })
                    .thenRun(
                        ex -> {
                            // doesn't run
                            count.addAndGet(9999);
                        })
                    .whenCancelled(
                        () -> {
                            // runs
                            count.addAndGet(10);
                        });

            Assertions.assertEquals(0, count.get());

            future.cancel(false);
            sleep(250);
            Assertions.assertEquals(11, count.get());

            sleep(250);
            Assertions.assertEquals(11, count.get());

            // test it another way
            var awaiter = new Awaiter();
            var future0 =
                Dispatcher.background()
                    .runLaterAsync(
                        cancellationToken -> {
                            while (!cancellationToken.isCancellationRequested()) {
                                sleep(10);
                            }

                            awaiter.signal();
                        });
            future0.whenCancelled(awaiter::signal);
            future0.whenFailed(ex -> awaiter.fail());
            future0.whenSucceeded(res -> awaiter.fail());

            var future1 =
                future0.thenRun(
                    () -> {
                        throw new RuntimeException("Never thrown");
                    });
            future1.whenCancelled(awaiter::signal);
            future1.whenFailed(ex -> awaiter.fail());
            future1.whenSucceeded(res -> awaiter.fail());

            sleep(50);
            future1.cancel(false);

            awaiter.await(3);
        }

        @Test
        void Cancellation_Tokens_Work_With_Continuations() {
            AtomicInteger count = new AtomicInteger();

            var cts = new CancellationSource();

            Dispatcher.background()
                .runLaterAsync(
                    () -> {
                        // not run because of cancellation
                        count.addAndGet(999);
                    },
                    Duration.ofMillis(100),
                    cts)
                .whenDone(
                    f -> {
                        // this should be called even if cancelled:
                        count.addAndGet(1);
                    })
                .thenRun(
                    f -> {
                        // not run because of cancellation
                        count.addAndGet(9999);
                    })
                .whenDone(
                    f -> {
                        // this should be called even if cancelled:
                        count.addAndGet(10);
                    });

            // cancel immediately
            cts.cancel();

            sleep(200);

            Assertions.assertEquals(11, count.get());
        }

        @Test
        void Async_Continuation_Is_Cancelled_By_Calling_Cancel() {
            var awaiter = new Awaiter();

            Dispatcher.background()
                .runLaterAsync(() -> sleep(50))
                .thenRunAsync(
                    () ->
                        Dispatcher.background()
                            .runLaterAsync(
                                cancellationToken -> {
                                    while (!cancellationToken.isCancellationRequested()) {
                                        sleep(20);
                                    }
                                }))
                .whenDone(awaiter::signal)
                .cancel(false);

            awaiter.await(1, Duration.ofSeconds(1));
        }

        @Test
        void CancellationToken_Works_If_Submit_Is_Elided() {
            var awaiter = new Awaiter();

            AsyncFX.getAsyncSubmitCount();

            var future =
                Dispatcher.background()
                    .runLaterAsync(() -> sleep(100)) // submitted to FES
                    .thenRunAsync(
                        () -> Dispatcher.background().runLaterAsync(() -> sleep(50))) // executed synchronously
                    .thenRunAsync( // executed synchronously
                        () ->
                            // Submit to FES is elided, and the CT of the continuation is re-used for the new future:
                            Dispatcher.background()
                                .runLaterAsync(
                                    cancellationToken -> {
                                        awaiter.assertEquals(1, AsyncFX.getAsyncSubmitCount());

                                        while (!cancellationToken.isCancellationRequested()) {
                                            sleep(20);
                                        }

                                        awaiter.signal();
                                    }))
                    .whenCancelled(awaiter::signal);

            // Need to wait here for a moment, until we're actually running the second continuation.
            sleep(250);

            future.cancel(false);
            awaiter.await(2, Duration.ofSeconds(1));
        }

        private Future<Integer> recursiveDelayLoopAsync(int i, int iMax, Runnable runnable, Duration delay) {
            if (i >= iMax) {
                return Futures.successful(i);
            }

            // recursive call after delay
            return Dispatcher.background()
                .runLaterAsync(runnable, delay)
                .thenGetAsync(() -> recursiveDelayLoopAsync(i + 1, iMax, runnable, delay));
        }

        @Test
        void Recursive_Futures_Can_Be_Cancelled() {
            var awaiter = new Awaiter();
            var counter = new AtomicInteger();
            var future =
                recursiveDelayLoopAsync(0, 50, awaiter::signal, Duration.ofMillis(100))
                    .whenDone(
                        ff -> {
                            if (ff.isSuccess()) {
                                counter.set(ff.getUnchecked() + 10);
                            }

                            if (ff.isCancelled()) {
                                counter.set(-1);
                            }
                        });

            // should take 2 * 500ms
            awaiter.await(2, Duration.ofMillis(2000));

            future.cancel(false);
            sleep(100);

            Assertions.assertEquals(-1, counter.get());
            assertTrue(future.isCancelled());
        }
    }

    @Nested
    class ExceptionHandling {
        @Test
        void Failing_Continuations_Aggregate_Exceptions() {
            var awaiter = new Awaiter();

            var firstFuture =
                Dispatcher.background()
                    .getLaterAsync(
                        () -> {
                            throw new RuntimeException("First exception");
                        },
                        Duration.ofMillis(100));

            var secondFuture =
                firstFuture.thenFinallyRunAsync(exception -> Futures.failed(new RuntimeException("Second exception")));

            var thirdFuture =
                secondFuture.thenFinallyRunAsync(
                    exception ->
                        Dispatcher.background()
                            .getLaterAsync(
                                () -> {
                                    throw new RuntimeException("Third exception");
                                },
                                Duration.ofMillis(100)));

            thirdFuture.whenDone(
                f -> {
                    awaiter.assertEquals(3, f.getException().getThrowables().length);

                    var throwables = f.getException().getThrowables();
                    awaiter.assertEquals("Third exception", throwables[0].getMessage());
                    awaiter.assertEquals("Second exception", throwables[1].getMessage());
                    awaiter.assertEquals("First exception", throwables[2].getMessage());

                    awaiter.signal();
                });

            awaiter.await(1);
        }

        @Test
        void Immediately_Failed_Continuations_Aggregate_Exceptions() {
            var awaiter = new Awaiter();

            var firstFuture = Futures.failed(new RuntimeException("First exception"));

            var secondFuture =
                firstFuture.thenFinallyRunAsync(ex -> Futures.failed(new RuntimeException("Second exception")));

            var thirdFuture =
                secondFuture.thenFinallyRunAsync(ex -> Futures.failed(new RuntimeException("Third exception")));

            thirdFuture.whenDone(
                f -> {
                    awaiter.assertEquals(3, f.getException().getThrowables().length);

                    var throwables = f.getException().getThrowables();
                    awaiter.assertEquals("Third exception", throwables[0].getMessage());
                    awaiter.assertEquals("Second exception", throwables[1].getMessage());
                    awaiter.assertEquals("First exception", throwables[2].getMessage());

                    awaiter.signal();
                });

            awaiter.await(1);
        }

        @Test
        void Failed_Future_Forwards_Exception() {
            var awaiter = new Awaiter();

            // test with scheduled future
            var firstFuture =
                Dispatcher.background()
                    .getLaterAsync(
                        () -> {
                            throw new RuntimeException("First exception");
                        },
                        Duration.ofMillis(150));
            var secondFuture =
                firstFuture.thenRun(
                    () -> {
                        throw new RuntimeException("Never executed");
                    });
            var thirdFuture =
                secondFuture.thenRun(
                    () -> {
                        throw new RuntimeException("Never executed");
                    });
            thirdFuture.whenDone(
                f -> {
                    awaiter.assertTrue(f.isFailed());
                    awaiter.assertEquals(1, f.getException().getThrowables().length);
                    awaiter.assertEquals("First exception", f.getException().getThrowables()[0].getMessage());
                    awaiter.signal();
                });

            // test with immediate future
            firstFuture = Futures.failed(new RuntimeException("First exception"));
            secondFuture =
                firstFuture.thenRun(
                    () -> {
                        throw new RuntimeException("Never executed");
                    });
            thirdFuture =
                secondFuture.thenRun(
                    () -> {
                        throw new RuntimeException("Never executed");
                    });
            thirdFuture.whenDone(
                f -> {
                    awaiter.assertTrue(f.isFailed());
                    awaiter.assertEquals(1, f.getException().getThrowables().length);
                    awaiter.assertEquals("First exception", f.getException().getThrowables()[0].getMessage());
                    awaiter.signal();
                });

            awaiter.await(2);
        }
    }

    @Nested
    class PlatformFuture {
        @Test
        void whenSucceeded_Is_Called_On_UI_Thread() {
            var awaiter = new Awaiter();

            Dispatcher.platform()
                .runLaterAsync(
                    () -> {
                        awaiter.assertTrue(Platform.isFxApplicationThread());
                        awaiter.signal();
                    })
                .whenSucceeded(
                    () -> {
                        awaiter.assertTrue(Platform.isFxApplicationThread());
                        awaiter.signal();
                    });

            awaiter.await(2);
        }

        @Test
        void run_Is_Not_Cancelled_Cooperatively() {
            var awaiter = new Awaiter();
            AtomicInteger count = new AtomicInteger();

            Future<Void> future =
                Dispatcher.platform()
                    .runLaterAsync(
                        () -> {
                            awaiter.assertTrue(Platform.isFxApplicationThread());
                            count.addAndGet(1);
                            sleep(250);
                            count.addAndGet(10);
                        })
                    .thenRun((Runnable)awaiter::fail)
                    .whenDone(
                        () -> {
                            awaiter.assertTrue(!Platform.isFxApplicationThread());
                            awaiter.assertEquals(11, count.get());
                            awaiter.signal();
                        });

            // mayInterruptIfRunning=true has no effect here, since platform futures don't support interruption
            future.cancel(true);
            awaiter.await(1);
        }

        @Test
        void run_Is_Cancelled_Cooperatively() {
            var awaiter = new Awaiter();
            AtomicInteger count = new AtomicInteger();

            Future<Void> future =
                Dispatcher.platform()
                    .runLaterAsync(
                        cancellationToken -> {
                            awaiter.assertTrue(Platform.isFxApplicationThread());

                            count.addAndGet(1);
                            for (int i = 0; i < 50 && !cancellationToken.isCancellationRequested(); ++i) {
                                sleep(10);
                            }

                            if (cancellationToken.isCancellationRequested()) {
                                count.addAndGet(10);
                            }
                        })
                    .thenRun((Runnable)awaiter::fail)
                    .whenDone(
                        () -> {
                            awaiter.assertTrue(!Platform.isFxApplicationThread());
                            awaiter.signal();
                        });

            sleep(50);
            future.cancel(false);

            awaiter.await(1);
            Assertions.assertEquals(11, count.get());
        }

        @Test
        void run_Is_Cancelled_Cooperatively_By_CancellationSource() {
            var awaiter = new Awaiter();
            var cancellationSource = new CancellationSource();
            AtomicInteger count = new AtomicInteger();

            Dispatcher.platform()
                .runLaterAsync(
                    cancellationToken -> {
                        awaiter.assertTrue(Platform.isFxApplicationThread());

                        count.addAndGet(1);
                        for (int i = 0; i < 50 && !cancellationToken.isCancellationRequested(); ++i) {
                            sleep(10);
                        }

                        if (cancellationToken.isCancellationRequested()) {
                            count.addAndGet(10);
                        }
                    },
                    cancellationSource)
                .thenRun((Runnable)awaiter::fail)
                .whenDone(
                    () -> {
                        awaiter.assertTrue(!Platform.isFxApplicationThread());
                        awaiter.signal();
                    });

            sleep(50);
            cancellationSource.cancel(false);

            awaiter.await(1);
            Assertions.assertEquals(11, count.get());
        }

        @Test
        void Delayed_run_Is_Cancelled_Immediately() {
            var awaiter = new Awaiter();

            Future<Void> future =
                Dispatcher.platform()
                    .runLaterAsync((Runnable)Assertions::fail, Duration.ofMillis(1000))
                    .whenDone(
                        () -> {
                            Assertions.assertTrue(Platform.isFxApplicationThread());
                            awaiter.signal();
                        });

            future.cancel(false);
            awaiter.await(1);
        }
    }

    @Test
    void Future_Is_Periodically_Executed() {
        var awaiter = new Awaiter();
        var counter = new AtomicInteger();
        Future<Void> future =
            Dispatcher.background()
                .runLaterAsync(awaiter::signal, Duration.ofMillis(50), Duration.ofMillis(50))
                .whenDone(
                    () -> {
                        counter.incrementAndGet();
                        awaiter.signal();
                    });

        awaiter.await(10, Duration.ofMillis(1000));
        future.cancel(false);
        awaiter.await(1, Duration.ofMillis(500));
        assertTrue(future.isCancelled());
        Assertions.assertEquals(1, counter.get());
    }

    @Test
    void whenAll_Future_Completes_When_All_Inner_Futures_Have_Completed() {
        var awaiter = new Awaiter();
        var counter = new AtomicInteger();

        var future =
            Futures.whenAll(
                Dispatcher.background().getLaterAsync(counter::incrementAndGet),
                Dispatcher.background().getLaterAsync(counter::incrementAndGet, Duration.ofMillis(100)),
                Dispatcher.background().getLaterAsync(counter::incrementAndGet));

        future.whenSucceeded(
            f -> {
                awaiter.assertEquals(3, counter.get());
                awaiter.signal();
            });

        awaiter.await(1, Duration.ofMillis(250));
    }

    @Test
    void whenAll_Future_Cancels_All_Inner_Futures() {
        var awaiter = new Awaiter();
        var counter = new AtomicInteger();

        var future =
            Futures.whenAll(
                Dispatcher.background()
                    .getLaterAsync(counter::incrementAndGet, Duration.ofMillis(100))
                    .whenDone(
                        f -> {
                            awaiter.assertTrue(f.isCancelled());
                            awaiter.signal();
                        }),
                Dispatcher.background()
                    .getLaterAsync(counter::incrementAndGet, Duration.ofMillis(125))
                    .whenDone(
                        f -> {
                            awaiter.assertTrue(f.isCancelled());
                            awaiter.signal();
                        }),
                Dispatcher.background()
                    .getLaterAsync(counter::incrementAndGet, Duration.ofMillis(150))
                    .whenDone(
                        f -> {
                            awaiter.assertTrue(f.isCancelled());
                            awaiter.signal();
                        }));

        future.whenDone(
            f -> {
                awaiter.assertEquals(0, counter.get());
                awaiter.signal();
            });

        future.cancel(false);

        awaiter.await(4, Duration.ofMillis(1000));
    }

    @Test
    void Futures_Report_Progress() {
        var awaiter = new Awaiter();

        Future<Void> future =
            Dispatcher.background()
                .runLaterAsync(
                    cancellationToken -> {
                        for (int i = 0; i < 10; ++i) {
                            cancellationToken.setProgress((double)i / 9);
                            sleep(10);
                        }
                    },
                    Duration.ofMillis(150))
                .thenRun(
                    cancellationToken -> {
                        for (int i = 0; i < 10; ++i) {
                            cancellationToken.setProgress((double)i / 9);
                            sleep(10);
                        }
                    })
                .thenRun(
                    cancellationToken -> {
                        for (int i = 0; i < 10; ++i) {
                            cancellationToken.setProgress((double)i / 9);
                            sleep(10);
                        }
                    });

        double[] lastProgress = new double[1];
        lastProgress[0] = -1;

        future.addListener(
            progress -> {
                awaiter.assertTrue(progress > lastProgress[0]);
                lastProgress[0] = progress;
                awaiter.signal();
            });

        awaiter.await(9 * 3 + 1);

        Assertions.assertEquals(1, lastProgress[0]);
    }

}
