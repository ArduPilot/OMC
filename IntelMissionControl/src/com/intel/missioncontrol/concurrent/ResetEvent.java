/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * This waiting primitive can be used to wait for a signal. If the signal is set, it allows any number of threads to
 * pass until the signal is reset again.
 */
public class ResetEvent {

    private static final class Sync extends AbstractQueuedSynchronizer {
        @Override
        public int tryAcquireShared(int acquires) {
            return getState() == 0 ? 1 : -1;
        }

        @Override
        public boolean tryReleaseShared(int releases) {
            while (true) {
                int state = getState();
                if (state == 0) {
                    return false;
                }

                int next = state - 1;
                if (compareAndSetState(state, next)) {
                    return next == 0;
                }
            }
        }

        public void reset() {
            setState(1);
        }
    }

    private final Sync sync = new Sync();

    public ResetEvent() {
        sync.reset();
    }

    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    public void reset() {
        sync.reset();
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    public void set() {
        sync.releaseShared(1);
    }

}
