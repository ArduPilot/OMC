/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import org.asyncfx.PublishSource;

@PublishSource(module = "openjdk-locks", licenses = "openjdk-locks")
@SuppressWarnings({"WeakerAccess", "unused"})
public class ReentrantStampedLock {

    public static final long REENTRANT_STAMP = Long.MAX_VALUE;

    private final StampedLock lock = new StampedLock();
    private volatile Thread owner;

    public void changeOwner(Thread newOwner) {
        owner = newOwner;
    }

    public boolean isWriteLockedByCurrentThread() {
        return isWriteLockedByThread(Thread.currentThread());
    }

    private boolean isWriteLockedByThread(Thread thread) {
        return owner == thread;
    }

    public long tryOptimisticRead() {
        return lock.tryOptimisticRead();
    }

    public boolean validate(long stamp) {
        return lock.validate(stamp);
    }

    /**
     * Acquires a non-exclusive read lock and blocks until it becomes available. If a write lock has already been
     * acquired by the current thread, returns the special value {@link ReentrantStampedLock#REENTRANT_STAMP}.
     */
    public long readLock() {
        if (isWriteLockedByCurrentThread()) {
            return REENTRANT_STAMP;
        }

        return lock.readLock();
    }

    /**
     * Acquires a non-exclusive read lock and blocks until it becomes available, or until the thread is interrupted. If
     * a write lock has already been acquired by the current thread, returns the special value {@link
     * ReentrantStampedLock#REENTRANT_STAMP}.
     */
    public long readLockInterruptibly() throws InterruptedException {
        if (isWriteLockedByCurrentThread()) {
            return REENTRANT_STAMP;
        }

        return lock.readLockInterruptibly();
    }

    /**
     * Tries to acquire a non-exclusive read lock. Returns the special value 0 if the lock is not available, or {@link
     * ReentrantStampedLock#REENTRANT_STAMP} if an exclusive lock has already been acquired by the current thread.
     */
    public long tryReadLock() {
        if (isWriteLockedByCurrentThread()) {
            return REENTRANT_STAMP;
        }

        return lock.tryReadLock();
    }

    /**
     * Tries to acquire a non-exclusive read lock. Returns the special value 0 if the lock is not available or the
     * request timed out, or {@link ReentrantStampedLock#REENTRANT_STAMP} if an exclusive lock has already been acquired
     * by the current thread.
     */
    public long tryReadLock(long time, TimeUnit unit) throws InterruptedException {
        if (isWriteLockedByCurrentThread()) {
            return REENTRANT_STAMP;
        }

        return lock.tryReadLock(time, unit);
    }

    /**
     * Releases the lock if the value matches the specified stamp. For the special value {@link
     * ReentrantStampedLock#REENTRANT_STAMP}, this is a no-op.
     */
    public void unlockRead(long stamp) {
        if (stamp != REENTRANT_STAMP) {
            lock.unlockRead(stamp);
        }
    }

    /**
     * Acquires an exclusive write lock and blocks until it becomes available. The current thread becomes the owner of
     * the lock. If a write lock has already been acquired by the current thread, returns the special value {@link
     * ReentrantStampedLock#REENTRANT_STAMP}.
     */
    public long writeLock() {
        Thread currentOwner = owner;
        Thread currentThread = Thread.currentThread();
        if (isWriteLockedByThread(currentThread)) {
            return REENTRANT_STAMP;
        }

        long stamp = lock.writeLock();
        owner = currentThread;
        return stamp;
    }

    /**
     * Acquires an exclusive write lock and blocks until it becomes available, or until the thread is interrupted. The
     * current thread becomes the owner of the lock. If a write lock has already been acquired by the current thread,
     * returns the special value {@link ReentrantStampedLock#REENTRANT_STAMP}.
     */
    public long writeLockInterruptibly() throws InterruptedException {
        Thread currentThread = Thread.currentThread();
        if (isWriteLockedByThread(currentThread)) {
            return REENTRANT_STAMP;
        }

        long stamp = lock.writeLockInterruptibly();
        owner = currentThread;
        return stamp;
    }

    /**
     * Tries to acquire an exclusive write lock. Returns the special value 0 if the lock is not available, or {@link
     * ReentrantStampedLock#REENTRANT_STAMP} if an exclusive lock has already been acquired by the current thread. If
     * the lock was successfully acquired, the current thread becomes the owner of the lock.
     */
    public long tryWriteLock() {
        Thread currentThread = Thread.currentThread();
        if (isWriteLockedByThread(currentThread)) {
            return REENTRANT_STAMP;
        }

        long stamp = lock.tryWriteLock();
        if (stamp != 0) {
            owner = currentThread;
        }

        return stamp;
    }

    /**
     * Tries to acquire an exclusive write lock. Returns the special value 0 if the lock is not available or the request
     * timed out, or {@link ReentrantStampedLock#REENTRANT_STAMP} if an exclusive lock has already been acquired by the
     * current thread. If the lock was successfully acquired, the current thread becomes the owner of the lock.
     */
    public long tryWriteLock(long time, TimeUnit unit) throws InterruptedException {
        Thread currentThread = Thread.currentThread();
        if (isWriteLockedByThread(currentThread)) {
            return REENTRANT_STAMP;
        }

        long stamp = lock.tryWriteLock(time, unit);
        if (stamp != 0) {
            owner = currentThread;
        }

        return stamp;
    }

    /**
     * Releases the lock if the value matches the specified stamp. For the special value {@link
     * ReentrantStampedLock#REENTRANT_STAMP}, this is a no-op.
     */
    public void unlockWrite(long stamp) {
        if (stamp != REENTRANT_STAMP) {
            owner = null;
            lock.unlockWrite(stamp);
        }
    }

    public String toString() {
        return lock.toString() + ", owner=" + (owner != null ? owner : "<null>");
    }

    public static boolean isReadLockStamp(long stamp) {
        return stamp != REENTRANT_STAMP && StampedLock.isReadLockStamp(stamp);
    }

    public static boolean isWriteLockStamp(long stamp) {
        return stamp != REENTRANT_STAMP && StampedLock.isWriteLockStamp(stamp);
    }

    public static boolean isOptimisticReadStamp(long stamp) {
        return stamp != REENTRANT_STAMP && StampedLock.isOptimisticReadStamp(stamp);
    }

    public static boolean isLockStamp(long stamp) {
        return stamp != REENTRANT_STAMP && StampedLock.isLockStamp(stamp);
    }

}
