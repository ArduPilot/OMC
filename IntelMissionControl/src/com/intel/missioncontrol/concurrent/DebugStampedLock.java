/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import com.intel.missioncontrol.EnvironmentOptions;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;

public class DebugStampedLock extends StampedLock {

    private static class LockInfo {
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
                    + (info.write ? "while a write lock is being held." : "that was already acquired."));
        }

        info.locked = true;
        info.write = false;

        try {
            long stamp = super.tryReadLock(EnvironmentOptions.DEADLOCK_DETECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            if (stamp == 0) {
                String token = Integer.toHexString(random.nextInt(Integer.MAX_VALUE)).toUpperCase();
                synchronized (DebugStampedLock.class) {
                    if (timeoutHandler != null) {
                        timeoutHandler.accept(token);
                    }
                }

                throw new IllegalMonitorStateException(
                    "Potential deadlock detected: failed to acquire a read lock within "
                        + EnvironmentOptions.DEADLOCK_DETECTION_TIMEOUT
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
        currentLockInfo.locked = false;
        super.unlockRead(stamp);
    }

    @Override
    public long writeLock() {
        LockInfo info = this.info.get();
        if (info.locked) {
            throw new IllegalMonitorStateException(
                "An illegal attempt was made to acquire a write lock "
                    + (info.write ? "that was already acquired" : "while a read lock is being held."));
        }

        info.locked = true;
        info.write = true;

        try {
            long stamp = super.tryWriteLock(EnvironmentOptions.DEADLOCK_DETECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            if (stamp == 0) {
                String token = Integer.toHexString(random.nextInt(Integer.MAX_VALUE)).toUpperCase();
                synchronized (DebugStampedLock.class) {
                    if (timeoutHandler != null) {
                        timeoutHandler.accept(token);
                    }
                }

                throw new IllegalMonitorStateException(
                    "Potential deadlock detected: failed to acquire a write lock within "
                        + EnvironmentOptions.DEADLOCK_DETECTION_TIMEOUT
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
