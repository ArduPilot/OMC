/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.InvalidationListenerWrapper;
import com.intel.missioncontrol.concurrent.ReentrantStampedLock;
import com.sun.javafx.collections.SetListenerHelper;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.collections.SetChangeListener;
import org.checkerframework.checker.nullness.qual.NonNull;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public class AsyncObservableSetWrapper<E> implements AsyncObservableSet<E> {

    private class Itr implements Iterator<E> {
        private final Iterator<E> backingIt = backingSet.iterator();
        private E lastElement;

        Itr() {
            if (!lock.isWriteLockedByCurrentThread()) {
                throw new IllegalMonitorStateException("Cannot iterate over an unlocked set.");
            }
        }

        @Override
        public boolean hasNext() {
            return backingIt.hasNext();
        }

        @Override
        public E next() {
            lastElement = backingIt.next();
            return lastElement;
        }

        @Override
        public void remove() {
            backingIt.remove();
            callObservers(new SimpleRemoveChange(lastElement));
        }
    }

    private final ReentrantStampedLock lock = new ReentrantStampedLock();
    private final Set<E> backingSet;

    private SetListenerHelper<E> listenerHelper;

    public AsyncObservableSetWrapper(Set<E> set) {
        this.backingSet = set;
    }

    private class SimpleAddChange extends SetChangeListener.Change<E> {
        private final E added;

        public SimpleAddChange(E added) {
            super(AsyncObservableSetWrapper.this);
            this.added = added;
        }

        @Override
        public boolean wasAdded() {
            return true;
        }

        @Override
        public boolean wasRemoved() {
            return false;
        }

        @Override
        public E getElementAdded() {
            return added;
        }

        @Override
        public E getElementRemoved() {
            return null;
        }

        @Override
        public String toString() {
            return "added " + added;
        }
    }

    private class SimpleRemoveChange extends SetChangeListener.Change<E> {
        private final E removed;

        public SimpleRemoveChange(E removed) {
            super(AsyncObservableSetWrapper.this);
            this.removed = removed;
        }

        @Override
        public boolean wasAdded() {
            return false;
        }

        @Override
        public boolean wasRemoved() {
            return true;
        }

        @Override
        public E getElementAdded() {
            return null;
        }

        @Override
        public E getElementRemoved() {
            return removed;
        }

        @Override
        public String toString() {
            return "removed " + removed;
        }
    }

    private void callObservers(SetChangeListener.Change<E> change) {
        SetListenerHelper.fireValueChangedEvent(listenerHelper, change);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        listenerHelper = SetListenerHelper.addListener(listenerHelper, listener);
    }

    @Override
    public void addListener(InvalidationListener listener, Executor executor) {
        listenerHelper =
            SetListenerHelper.addListener(listenerHelper, InvalidationListenerWrapper.wrap(listener, executor));
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        listenerHelper = SetListenerHelper.removeListener(listenerHelper, listener);
    }

    @Override
    public void addListener(SetChangeListener<? super E> observer) {
        listenerHelper = SetListenerHelper.addListener(listenerHelper, observer);
    }

    @Override
    public void addListener(SetChangeListener<? super E> listener, Executor executor) {
        listenerHelper =
            SetListenerHelper.addListener(listenerHelper, new SetChangeListenerWrapper<>(listener, executor));
    }

    @Override
    public void removeListener(SetChangeListener<? super E> observer) {
        listenerHelper = SetListenerHelper.removeListener(listenerHelper, observer);
    }

    @Override
    public LockedSet<E> lock() {
        long stamp = lock.writeLock();

        return new LockedSet<>() {
            @Override
            public void changeOwner(Thread thread) {
                lock.changeOwner(Thread.currentThread());
            }

            @Override
            void closeInternal() {
                if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                    lock.unlockWrite(stamp);
                }
            }

            @Override
            public int size() {
                return backingSet.size();
            }

            @Override
            public boolean isEmpty() {
                return backingSet.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return backingSet.contains(o);
            }

            @NonNull
            @Override
            public Iterator<E> iterator() {
                return new Itr();
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return backingSet.toArray();
            }

            @NonNull
            @Override
            public <T> T[] toArray(@NonNull T[] a) {
                return backingSet.toArray(a);
            }

            @Override
            public boolean add(E e) {
                return backingSet.add(e);
            }

            @Override
            public boolean remove(Object o) {
                return backingSet.remove(o);
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> c) {
                return backingSet.containsAll(c);
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends E> c) {
                return backingSet.addAll(c);
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> c) {
                return backingSet.retainAll(c);
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> c) {
                return backingSet.removeAll(c);
            }

            @Override
            public void clear() {
                backingSet.clear();
            }
        };
    }

    @Override
    public int size() {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return backingSet.size();
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return backingSet.isEmpty();
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public boolean contains(Object o) {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return backingSet.contains(o);
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public Object[] toArray() {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return backingSet.toArray();
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return backingSet.toArray(a);
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public boolean add(E o) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            boolean ret = backingSet.add(o);
            if (ret) {
                callObservers(new SimpleAddChange(o));
            }

            return ret;
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    @Override
    public boolean remove(Object o) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            boolean ret = backingSet.remove(o);
            if (ret) {
                callObservers(new SimpleRemoveChange((E)o));
            }

            return ret;
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return backingSet.containsAll(c);
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            boolean ret = false;
            for (E element : c) {
                boolean r = backingSet.add(element);
                if (r) {
                    callObservers(new SimpleAddChange(element));
                }

                ret |= r;
            }

            return ret;
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            return removeRetain(c, false);
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            return removeRetain(c, true);
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    private boolean removeRetain(Collection<?> c, boolean remove) {
        boolean removed = false;
        for (Iterator<E> i = backingSet.iterator(); i.hasNext(); ) {
            E element = i.next();
            if (remove == c.contains(element)) {
                removed = true;
                i.remove();
                callObservers(new SimpleRemoveChange(element));
            }
        }

        return removed;
    }

    @Override
    public void clear() {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            for (Iterator<E> i = backingSet.iterator(); i.hasNext(); ) {
                E element = i.next();
                i.remove();
                callObservers(new SimpleRemoveChange(element));
            }
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    @Override
    public String toString() {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return backingSet.toString();
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return backingSet.equals(obj);
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public int hashCode() {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return backingSet.hashCode();
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

}
