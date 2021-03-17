/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import org.asyncfx.AsyncFX;

public class DebugReentrantReadWriteLock extends ReentrantReadWriteLock {

    private static Consumer<String> timeoutHandler;

    public static void setTimeoutHandler(Consumer<String> handler) {
        synchronized (DebugStampedLock.class) {
            timeoutHandler = handler;
        }
    }

    private static class DebugReadLock extends ReadLock {
        private final Random random = new Random();

        DebugReadLock(ReentrantReadWriteLock lock) {
            super(lock);
        }

        @Override
        public void lock() {
            try {
                boolean succeeded =
                    super.tryLock()
                        || super.tryLock(AsyncFX.getDeadlockDetectionTimeoutMillis(), TimeUnit.MILLISECONDS);

                if (!succeeded) {
                    String token = Integer.toHexString(random.nextInt(Integer.MAX_VALUE)).toUpperCase();
                    synchronized (DebugStampedLock.class) {
                        if (timeoutHandler != null) {
                            timeoutHandler.accept(token);
                        }
                    }

                    throw new IllegalMonitorStateException(
                        "Potential deadlock detected: failed to acquire a read lock within "
                            + AsyncFX.getDeadlockDetectionTimeoutMillis()
                            + " ms ["
                            + token
                            + "].");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class DebugWriteLock extends WriteLock {
        private final Random random = new Random();

        DebugWriteLock(ReentrantReadWriteLock lock) {
            super(lock);
        }

        @Override
        public void lock() {
            try {
                boolean succeeded =
                    super.tryLock()
                        || super.tryLock(AsyncFX.getDeadlockDetectionTimeoutMillis(), TimeUnit.MILLISECONDS);

                if (!succeeded) {
                    String token = Integer.toHexString(random.nextInt(Integer.MAX_VALUE)).toUpperCase();
                    synchronized (DebugStampedLock.class) {
                        if (timeoutHandler != null) {
                            timeoutHandler.accept(token);
                        }
                    }

                    throw new IllegalMonitorStateException(
                        "Potential deadlock detected: failed to acquire a write lock within "
                            + AsyncFX.getDeadlockDetectionTimeoutMillis()
                            + " ms ["
                            + token
                            + "].");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final DebugReadLock readerLock;
    private final DebugWriteLock writerLock;

    public DebugReentrantReadWriteLock() {
        readerLock = new DebugReadLock(this);
        writerLock = new DebugWriteLock(this);
    }

    @Override
    public WriteLock writeLock() {
        return writerLock;
    }

    @Override
    public ReadLock readLock() {
        return readerLock;
    }

}
