/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/** This waiting primitive can be used to wait for an event. */
public class WaitHandle {

    static final class Sync extends AbstractQueuedSynchronizer {
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

    final Sync sync = new Sync();

    WaitHandle() {
        sync.reset();
    }

    public void awaitUnchecked() {
        try {
            await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean awaitUnchecked(long timeout, TimeUnit unit) {
        try {
            return await(timeout, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

}
