/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.StampedLock;
import org.asyncfx.beans.property.ReadOnlyAsyncProperty;
import org.asyncfx.concurrent.ReentrantStampedLock;

public final class AccessControllerImpl implements AccessController {

    public enum LockName {
        VALUE,
        EVENT
    }

    public enum LockType {
        INSTANCE,
        GROUP
    }

    private final StampedLock valueLock = new StampedLock();
    private final ReentrantStampedLock eventLock = new ReentrantStampedLock();

    private GroupLock groupLock;

    public void defer(Runnable runnable) {
        groupLock.defer(runnable);
    }

    public long writeLock(LockName lockName, LockType lockType) {
        if (lockName == LockName.VALUE) {
            if (lockType == LockType.INSTANCE) {
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

        return eventLock.writeLock();
    }

    public long tryWriteLock(LockName lockName, LockType lockType) {
        if (lockName == LockName.VALUE) {
            if (lockType == LockType.INSTANCE) {
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

        return eventLock.tryWriteLock();
    }

    public long tryConvertToWriteLock(LockName type, LockType lockType, long stamp) {
        if (type == LockName.VALUE) {
            if (lockType == LockType.INSTANCE) {
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

        throw new UnsupportedOperationException();
    }

    public void unlock(LockName type, long stamp) {
        if (type == LockName.VALUE) {
            if (StampedLock.isWriteLockStamp(stamp)) {
                valueLock.unlockWrite(stamp);
            } else if (StampedLock.isReadLockStamp(stamp)) {
                valueLock.unlockRead(stamp);
            }
        } else {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                eventLock.unlockWrite(stamp);
            } else if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                eventLock.unlockRead(stamp);
            }
        }
    }

    public void unlockWrite(LockName type, long stamp) {
        if (type == LockName.VALUE) {
            if (StampedLock.isWriteLockStamp(stamp)) {
                valueLock.unlockWrite(stamp);
            }
        } else if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
            eventLock.unlockWrite(stamp);
        }
    }

    public void unlockWrite(long valueStamp, long eventStamp) {
        if (StampedLock.isWriteLockStamp(valueStamp)) {
            valueLock.unlockWrite(valueStamp);
        }

        if (ReentrantStampedLock.isWriteLockStamp(eventStamp)) {
            eventLock.unlockWrite(eventStamp);
        }
    }

    public long readLock(LockName lockName, LockType lockType) {
        if (lockName == LockName.VALUE) {
            if (lockType == LockType.INSTANCE) {
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

        return eventLock.readLock();
    }

    public long tryOptimisticRead(LockName lockName, LockType lockType) {
        if (lockName == LockName.VALUE) {
            if (lockType == LockType.INSTANCE) {
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

        return eventLock.tryOptimisticRead();
    }

    public void unlockRead(LockName type, long stamp) {
        if (type == LockName.VALUE) {
            if (StampedLock.isReadLockStamp(stamp)) {
                valueLock.unlockRead(stamp);
            }
        } else if (ReentrantStampedLock.isReadLockStamp(stamp)) {
            eventLock.unlockRead(stamp);
        }
    }

    public boolean validate(LockName lockName, LockType lockType, long stamp) {
        if (lockName == LockName.VALUE) {
            if (lockType == LockType.INSTANCE) {
                return valueLock.validate(stamp);
            }

            GroupLock groupLock = this.groupLock;
            if (valueLock.validate(stamp)) {
                return groupLock == null || groupLock.hasAccess();
            }

            return false;
        }

        return eventLock.validate(stamp);
    }

    public void changeEventLockOwner(Thread newOwner) {
        eventLock.changeOwner(newOwner);
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
        private final Collection<ReadOnlyAsyncProperty> properties;
        private List<Runnable> deferred;
        private boolean set;

        public GroupLock(Collection<ReadOnlyAsyncProperty> properties) {
            this.properties = properties;
        }

        public Collection<ReadOnlyAsyncProperty> getProperties() {
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
