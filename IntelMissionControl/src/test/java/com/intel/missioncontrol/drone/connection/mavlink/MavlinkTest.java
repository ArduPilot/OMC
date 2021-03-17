/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import com.intel.missioncontrol.TestBase;
import com.intel.missioncontrol.Waiter;
import java.time.Duration;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.FutureCompletionSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MavlinkTest extends TestBase {

    @BeforeAll
    static void init() {
    }

    @Test
    void refreshableTimeout_Timeout_Works() {
        var waiter = new Waiter(true);

        var refreshableTimeout = new RefreshableTimeout(waiter::signal, Duration.ofMillis(200), null);

        refreshableTimeout.refreshOrStart();
        Dispatcher.background().runLaterAsync((Runnable)waiter::fail, Duration.ofMillis(500));

        waiter.await(1);
    }

    @Test
    void refreshableTimeout_Refresh_Works() {
        var waiter = new Waiter(true);

        var refreshableTimeout = new RefreshableTimeout(waiter::fail, Duration.ofMillis(200), null);

        refreshableTimeout.refreshOrStart();
        Dispatcher.background()
            .runLaterAsync(refreshableTimeout::refreshOrStart, Duration.ofMillis(100))
            .thenRunAsync(
                () -> Dispatcher.background().runLaterAsync(refreshableTimeout::refreshOrStart, Duration.ofMillis(100)))
            .thenRunAsync(
                () -> Dispatcher.background().runLaterAsync(refreshableTimeout::refreshOrStart, Duration.ofMillis(100)))
            .thenRunAsync(
                () -> Dispatcher.background().runLaterAsync(refreshableTimeout::refreshOrStart, Duration.ofMillis(100)))
            .thenRunAsync(
                () -> Dispatcher.background().runLaterAsync(refreshableTimeout::refreshOrStart, Duration.ofMillis(100)))
            .thenRun(waiter::signal);

        waiter.await(1);
    }

    @Test
    void runLaterAsync_Can_Be_Cancelled() {
        var waiter = new Waiter(true);
        var cs = new CancellationSource();
        Dispatcher.background().runLaterAsync((Runnable)waiter::fail, Duration.ofMillis(200), cs);

        Dispatcher.background()
            .runLaterAsync((Runnable)cs::cancel, Duration.ofMillis(100))
            .thenRunAsync(() -> Dispatcher.background().runLaterAsync(waiter::signal, Duration.ofMillis(400)));

        waiter.await(1);
    }

    @Test
    void refreshableTimeout_Cancellation_Works() {
        var waiter = new Waiter(true);

        var cs = new CancellationSource();

        var refreshableTimeout = new RefreshableTimeout(waiter::fail, Duration.ofMillis(200), cs);

        refreshableTimeout.refreshOrStart();
        Dispatcher.background()
            .runLaterAsync(refreshableTimeout::refreshOrStart, Duration.ofMillis(100))
            .thenRunAsync(
                () -> Dispatcher.background().runLaterAsync(refreshableTimeout::refreshOrStart, Duration.ofMillis(100)))
            .thenRunAsync(
                () -> Dispatcher.background().runLaterAsync(refreshableTimeout::refreshOrStart, Duration.ofMillis(100)))
            .thenRunAsync(
                () -> Dispatcher.background().runLaterAsync(refreshableTimeout::refreshOrStart, Duration.ofMillis(100)))
            .thenRunAsync(
                () -> Dispatcher.background().runLaterAsync(refreshableTimeout::refreshOrStart, Duration.ofMillis(100)))
            .thenRun((Runnable)cs::cancel)
            .thenRunAsync(() -> Dispatcher.background().runLaterAsync(waiter::signal, Duration.ofMillis(400)));

        waiter.await(1);
    }

    @Test
    void repeatSequentiallyAsync_Works() {
        var waiter = new Waiter(true);

        int count = 5;

        FutureHelper.repeatSequentiallyAsync(
                rep -> Dispatcher.background().getLaterAsync(() -> rep, Duration.ofMillis(50)), count)
            .whenSucceeded(
                list -> {
                    waiter.assertTrue(list.size() == count);
                    for (int i = 0; i < count; i++) {
                        waiter.assertTrue(list.get(i) == i);
                    }

                    waiter.signal();
                });

        waiter.await(1);
    }

    @Test
    void repeatSequentiallyAsync_Can_Be_Cancelled() {
        var waiter = new Waiter(true);

        int count = 2;

        var f =
            FutureHelper.repeatSequentiallyAsync(
                    rep -> Dispatcher.background().getLaterAsync(() -> rep, Duration.ofMillis(100)), count)
                .whenSucceeded(list -> waiter.fail());

        Dispatcher.background()
            .runLaterAsync((Runnable)f::cancel, Duration.ofMillis(100))
            .thenRunAsync(() -> Dispatcher.background().runLaterAsync(waiter::signal, Duration.ofMillis(400)));

        waiter.await(1);
    }

    @Test
    void FutureCompletionSource_whenDone_Triggers() {
        var waiter = new Waiter(true);

        var fcs = new FutureCompletionSource<Integer>();

        fcs.getFuture().whenDone(waiter::signal);

        fcs.setResult(1);
        fcs.setException(new RuntimeException("test exception"));

        waiter.await(1);

        waiter.assertTrue(fcs.getFuture().isSuccess());
    }

    @Test
    void FutureCompletionSource_whenCancelled_Triggers() {
        var waiter = new Waiter(true);

        var cs = new CancellationSource();
        var fcs = new FutureCompletionSource<Integer>(cs);

        fcs.getFuture().whenCancelled(waiter::signal);

        cs.cancel();

        waiter.await(1);

        waiter.assertTrue(fcs.getFuture().isCancelled());
    }
}
