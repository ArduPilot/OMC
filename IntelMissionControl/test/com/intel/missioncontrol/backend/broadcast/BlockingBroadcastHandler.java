/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.backend.broadcast;

import eu.mavinci.core.plane.management.CAirport;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class BlockingBroadcastHandler implements CAirport.BroadcastHandler {
    private final CountDownLatch connectionEstablishedEvent = new CountDownLatch(1);
    private final CountDownLatch connectionClosedEvent = new CountDownLatch(1);
    private final CyclicBarrier messageConsumedEvent = new CyclicBarrier(2);

    void waitForConnectionEstablished() throws InterruptedException {
        connectionEstablishedEvent.await();
    }

    @Override
    public void onConnectionEstablished() {
        connectionEstablishedEvent.countDown();
    }

    void waitForConnectionClosed() throws InterruptedException {
        connectionClosedEvent.await();
    }

    @Override
    public void onConnectionClosed() {
        connectionClosedEvent.countDown();
    }

    void waitForMessageConsumed() throws Exception {
        messageConsumedEvent.await();
    }

    @Override
    public void onMessageConsumed() {
        try {
            messageConsumedEvent.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }
}
