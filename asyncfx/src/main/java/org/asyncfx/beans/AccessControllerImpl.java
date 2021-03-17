/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;
import org.asyncfx.beans.property.ReadOnlyAsyncProperty;

public final class AccessControllerImpl implements AccessController {

    private final StampedLock valueLock = new StampedLock();
    private volatile ReentrantLock eventLock;

    private GroupLock groupLock;

    public void defer(Runnable runnable) {
        groupLock.defer(runnable);
    }

    public void lockEvent() {
        if (eventLock == null) {
            synchronized (this) {
                if (eventLock == null) {
                    eventLock = new ReentrantLock();
                }
            }
        }

        eventLock.lock();
    }

    public void unlockEvent() {
        eventLock.unlock();
    }

    public long writeLock(boolean group) {
        if (!group) {
            return valueLock.writeLock();
        }

        while (true) {
            long stamp = valueLock.writeLock();

            // We might need to access 'lockGroup' after releasing 'valueLock'. Since the 'lockGroup' field
            // is protected by that lock, we need to make a local copy while we're holding the lock.
            GroupLock groupLock = this.groupLock;

            if (groupLock == null || groupLock.hasAccess()) {
                return stamp;
            }

            valueLock.unlockWrite(stamp);

            // It's okay to use the local copy of 'lockGroup' after releasing the lock.
            groupLock.await();
        }
    }

    public long tryWriteLock(boolean group) {
        if (!group) {
            return valueLock.tryWriteLock();
        }

        long stamp = valueLock.tryWriteLock();
        if (stamp == 0) {
            return 0;
        }

        if (groupLock == null || groupLock.hasAccess()) {
            return stamp;
        }

        valueLock.unlockWrite(stamp);
        return 0;
    }

    public long tryConvertToWriteLock(boolean group, long stamp) {
        if (!group) {
            return valueLock.tryConvertToWriteLock(stamp);
        }

        long newStamp = valueLock.tryConvertToWriteLock(stamp);
        if (newStamp == 0) {
            return 0;
        }

        if (groupLock == null || groupLock.hasAccess()) {
            return newStamp;
        }

        valueLock.unlockWrite(newStamp);
        return 0;
    }

    public void unlock(long stamp) {
        if (StampedLock.isWriteLockStamp(stamp)) {
            valueLock.unlockWrite(stamp);
        } else if (StampedLock.isReadLockStamp(stamp)) {
            valueLock.unlockRead(stamp);
        }
    }

    public void unlockWrite(long stamp) {
        if (StampedLock.isWriteLockStamp(stamp)) {
            valueLock.unlockWrite(stamp);
        }
    }

    public long readLock(boolean group) {
        if (!group) {
            return valueLock.readLock();
        }

        while (true) {
            long stamp = valueLock.readLock();

            // We might need to access 'lockGroup' after releasing 'valueLock'. Since the 'lockGroup' field
            // is protected by that lock, we need to make a local copy while we're holding the lock.
            GroupLock groupLock = this.groupLock;

            if (groupLock == null || groupLock.hasAccess()) {
                return stamp;
            }

            valueLock.unlockRead(stamp);

            // It's okay to use the local copy of 'lockGroup' after releasing the lock.
            groupLock.await();
        }
    }

    public long tryOptimisticRead(boolean group) {
        if (!group) {
            return valueLock.tryOptimisticRead();
        }

        long stamp = valueLock.tryOptimisticRead();
        if (stamp != 0) {
            GroupLock groupLock = this.groupLock;
            if (valueLock.validate(stamp)) {
                if (groupLock == null || groupLock.hasAccess()) {
                    return stamp;
                }
            }
        }

        return 0;
    }

    public void unlockRead(long stamp) {
        if (StampedLock.isReadLockStamp(stamp)) {
            valueLock.unlockRead(stamp);
        }
    }

    public boolean validate(boolean group, long stamp) {
        if (!group) {
            return valueLock.validate(stamp);
        }

        GroupLock groupLock = this.groupLock;
        if (valueLock.validate(stamp)) {
            return groupLock == null || groupLock.hasAccess();
        }

        return false;
    }

    public void setGroupLock(GroupLock groupLock) {
        this.groupLock = groupLock;
    }

    @Override
    public boolean isLocked() {
        return groupLock != null;
    }

    public static final class GroupLock {
        private final Thread ownerThread = Thread.currentThread();
        private final Collection<ReadOnlyAsyncProperty<?>> properties;
        private List<Runnable> deferred;
        private boolean set;

        public GroupLock(Collection<ReadOnlyAsyncProperty<?>> properties) {
            this.properties = properties;
        }

        public Collection<ReadOnlyAsyncProperty<?>> getProperties() {
            return properties;
        }

        public boolean hasAccess() {
            return Thread.currentThread() == ownerThread;
        }

        public synchronized void await() {
            if (set || Thread.currentThread() == ownerThread) {
                return;
            }

            try {
                // We need to check the condition in a loop to protect against spurious wake-ups.
                while (!set) {
                    wait();
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        public synchronized void set() {
            set = true;

            notifyAll();

            if (deferred != null) {
                for (Runnable runnable : deferred) {
                    runnable.run();
                }
            }
        }

        public synchronized void defer(Runnable runnable) {
            if (deferred == null) {
                deferred = new ArrayList<>(2);
            }

            deferred.add(runnable);
        }
    }

}
