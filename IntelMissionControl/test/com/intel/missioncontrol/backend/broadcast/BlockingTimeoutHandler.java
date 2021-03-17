/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.backend.broadcast;

import eu.mavinci.core.plane.management.BackendState;
import eu.mavinci.core.plane.management.CAirport;
import java.time.Clock;
import java.util.concurrent.CountDownLatch;

public class BlockingTimeoutHandler implements CAirport.TimeoutHandler {
    static final int TEST_BACKEND_TIMEOUT = 100;

    private final CountDownLatch timeoutCheckedEvent = new CountDownLatch(1);
    private final CountDownLatch timeoutStoppedEvent = new CountDownLatch(1);
    private final Clock clock = Clock.systemDefaultZone();

    @Override
    public void timeoutSleep() {}

    void waitForTimeoutCheckFinished() throws InterruptedException {
        timeoutCheckedEvent.await();
    }

    @Override
    public void onTimeoutCheckFinished() {
        timeoutCheckedEvent.countDown();
    }

    void waitForTimeoutStop() throws InterruptedException {
        timeoutStoppedEvent.await();
    }

    @Override
    public void onTimeoutCheckStopped() {
        timeoutStoppedEvent.countDown();
    }

    @Override
    public boolean isTimeout(BackendState backendState) {
        return clock.millis() - backendState.getLastUpdate().getTime() > TEST_BACKEND_TIMEOUT;
    }
}
