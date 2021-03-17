/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import org.asyncfx.AsyncFX;

public class DebugStampedLock extends StampedLock {

    private static class LockInfo {
        Thread owner;
        boolean locked;
        boolean write;
    }

    private static Consumer<String> timeoutHandler;

    public static void setTimeoutHandler(Consumer<String> handler) {
        synchronized (DebugStampedLock.class) {
            timeoutHandler = handler;
        }
    }

    private final ThreadLocal<LockInfo> info = ThreadLocal.withInitial(LockInfo::new);
    private final Random random = new Random();
    private LockInfo currentLockInfo;

    @Override
    public long readLock() {
        LockInfo info = this.info.get();
        if (info.locked) {
            throw new IllegalMonitorStateException(
                "An illegal attempt was made to acquire a read lock "
                    + (info.write ? "while a write lock is being held" : "that was already acquired")
                    + " [currentThread = "
                    + Thread.currentThread().getName()
                    + "].");
        }

        info.owner = Thread.currentThread();
        info.locked = true;
        info.write = false;

        try {
            long stamp = super.tryReadLock(AsyncFX.getDeadlockDetectionTimeoutMillis(), TimeUnit.MILLISECONDS);
            if (stamp == 0) {
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

            currentLockInfo = info;
            return stamp;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unlockRead(long stamp) {
        if (currentLockInfo.owner != Thread.currentThread()) {
            throw new IllegalMonitorStateException(
                "An illegal attempt was made by '"
                    + Thread.currentThread().getName()
                    + "' to release a "
                    + (currentLockInfo.write ? "write" : "read")
                    + " lock that was acquired by '"
                    + currentLockInfo.owner.getName()
                    + "'.");
        }

        currentLockInfo.locked = false;
        super.unlockRead(stamp);
    }

    @Override
    public long writeLock() {
        LockInfo info = this.info.get();
        if (info.locked) {
            throw new IllegalMonitorStateException(
                "An illegal attempt was made to acquire a write lock "
                    + (info.write ? "that was already acquired" : "while a read lock is being held")
                    + " [currentThread = "
                    + Thread.currentThread().getName()
                    + "].");
        }

        info.owner = Thread.currentThread();
        info.locked = true;
        info.write = true;

        try {
            long stamp = super.tryWriteLock(AsyncFX.getDeadlockDetectionTimeoutMillis(), TimeUnit.MILLISECONDS);
            if (stamp == 0) {
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

            currentLockInfo = info;
            return stamp;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unlockWrite(long stamp) {
        currentLockInfo.locked = false;
        super.unlockWrite(stamp);
    }

}
