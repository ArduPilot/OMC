/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import java.util.concurrent.CountDownLatch;

public class AwaitableTestBase {

    private CountDownLatch latch;

    public void await(int awaitedSignals, Runnable runnable) {
        latch = new CountDownLatch(awaitedSignals);
        runnable.run();

        try {
            latch.await();
        } catch (InterruptedException e) {
        }
    }

    public void signal() {
        if (latch != null) {
            latch.countDown();
        }
    }

}
