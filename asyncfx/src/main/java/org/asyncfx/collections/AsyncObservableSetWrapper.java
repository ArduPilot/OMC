/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.SetChangeListener;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AsyncInvalidationListenerWrapper;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.AsyncSubInvalidationListenerWrapper;
import org.asyncfx.beans.property.AsyncSetPropertyBase;
import org.asyncfx.beans.property.PropertyObject;
import org.asyncfx.beans.property.PropertyObjectHelper;
import org.asyncfx.concurrent.ReentrantStampedLock;
import org.jetbrains.annotations.NotNull;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public class AsyncObservableSetWrapper<E> implements AsyncSubObservableSet<E>, AsyncSetPropertyBase.SetInitializer {

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

    private class UnmodifiableItr extends Itr {
        private final LockedCollectionType type;

        UnmodifiableItr(LockedCollectionType type) {
            this.type = type;
        }

        @Override
        public void remove() {
            throw new IllegalStateException(type.getNoModificationReason());
        }
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

    private class LockedSetImpl extends LockedSet<E> {
        private final LockedCollectionType type;
        private final long stamp;

        LockedSetImpl(LockedCollectionType type) {
            this.type = type;

            switch (type) {
            case WRITABLE:
                stamp = lock.writeLock();
                break;
            case EXPLICIT_READONLY:
                stamp = lock.readLock();
                break;
            default:
                stamp = 0;
                break;
            }
        }

        @Override
        public void changeOwner(Thread thread) {
            if (type == LockedCollectionType.NESTED_READONLY) {
                throw new IllegalStateException("The owner of a read-only list cannot be changed.");
            }

            lock.changeOwner(thread);
        }

        @Override
        public void closeInternal() {
            if (type.isReadOnly()) {
                if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                    lock.unlockRead(stamp);
                }
            } else {
                List<SetChangeListener.Change<E>> changes = aggregatedChanges;
                aggregatedChanges = null;

                if (changes != null) {
                    for (SetChangeListener.Change<E> change : changes) {
                        callObservers(change);
                    }
                }

                if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                    lock.unlockWrite(stamp);
                }
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

        @Override
        public @NotNull Iterator<E> iterator() {
            if (type.isReadOnly()) {
                return new UnmodifiableItr(type);
            }

            return new Itr();
        }

        @Override
        public @NotNull Object[] toArray() {
            return backingSet.toArray();
        }

        @Override
        public boolean add(E o) {
            checkReadOnly();
            return addCore(o);
        }

        @Override
        public boolean remove(Object o) {
            checkReadOnly();
            return removeCore(o);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends E> c) {
            checkReadOnly();
            return addAllCore(c);
        }

        @Override
        public void clear() {
            checkReadOnly();
            clearCore();
        }

        @Override
        public boolean retainAll(@NotNull Collection c) {
            checkReadOnly();
            return removeRetainCore(c, false);
        }

        @Override
        public boolean removeAll(@NotNull Collection c) {
            checkReadOnly();
            return removeRetainCore(c, true);
        }

        @Override
        public boolean containsAll(@NotNull Collection c) {
            return containsAllCore(c);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull Object[] toArray(@NotNull Object[] a) {
            return backingSet.toArray((E[])a);
        }

        private void checkReadOnly() {
            if (type.isReadOnly()) {
                throw new IllegalStateException(type.getNoModificationReason());
            }
        }
    }

    private final ReentrantStampedLock lock = new ReentrantStampedLock();
    private final Set<E> backingSet;
    private List<SetChangeListener.Change<E>> aggregatedChanges;
    private InvalidationListener subInvalidationListener = this::fireSubValueChangedEvent;
    private AsyncSetListenerHelper<E> listenerHelper;

    public AsyncObservableSetWrapper(Set<E> set) {
        this.backingSet = set;
    }

    @Override
    public void initializeSet(Executor executor) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();

            subInvalidationListener = AsyncInvalidationListenerWrapper.wrap(this::fireSubValueChangedEvent, executor);

            for (E item : backingSet) {
                if (item instanceof PropertyObject) {
                    PropertyObjectHelper.addListener((PropertyObject)item, subInvalidationListener);
                }
            }
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    private void callObservers(SetChangeListener.Change<E> change) {
        if (aggregatedChanges != null) {
            aggregatedChanges.add(change);
        } else {
            AsyncSetListenerHelper.fireValueChangedEvent(listenerHelper, change);
        }
    }

    @Override
    public void addListener(InvalidationListener listener) {
        listenerHelper = AsyncSetListenerHelper.addListener(listenerHelper, this, listener);
    }

    @Override
    public void addListener(InvalidationListener listener, Executor executor) {
        listenerHelper =
            AsyncSetListenerHelper.addListener(
                listenerHelper, this, AsyncInvalidationListenerWrapper.wrap(listener, executor));
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        listenerHelper = AsyncSetListenerHelper.removeListener(listenerHelper, listener);
    }

    @Override
    public void addListener(SubInvalidationListener listener) {
        listenerHelper = AsyncSetListenerHelper.addListener(listenerHelper, this, listener);
    }

    @Override
    public void addListener(SubInvalidationListener listener, Executor executor) {
        listenerHelper =
            AsyncSetListenerHelper.addListener(
                listenerHelper, this, AsyncSubInvalidationListenerWrapper.wrap(listener, executor));
    }

    @Override
    public void removeListener(SubInvalidationListener listener) {
        listenerHelper = AsyncSetListenerHelper.removeListener(listenerHelper, listener);
    }

    @Override
    public void addListener(SetChangeListener<? super E> observer) {
        listenerHelper = AsyncSetListenerHelper.addListener(listenerHelper, this, observer);
    }

    @Override
    public void addListener(SetChangeListener<? super E> listener, Executor executor) {
        listenerHelper =
            AsyncSetListenerHelper.addListener(
                listenerHelper, this, new SetChangeListenerWrapper<>(listener, executor));
    }

    @Override
    public void removeListener(SetChangeListener<? super E> observer) {
        listenerHelper = AsyncSetListenerHelper.removeListener(listenerHelper, observer);
    }

    @Override
    public final LockedSet<E> lock() {
        return new LockedSetImpl(
            lock.isWriteLockedByCurrentThread() ? LockedCollectionType.NESTED_READONLY : LockedCollectionType.WRITABLE);
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
            return addCore(o);
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    private boolean addCore(E o) {
        boolean ret = backingSet.add(o);
        if (ret) {
            if (o instanceof PropertyObject) {
                PropertyObjectHelper.addListener((PropertyObject)o, subInvalidationListener);
            }

            callObservers(new SimpleAddChange(o));
        }

        return ret;
    }

    @Override
    public boolean remove(Object o) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            return removeCore(o);
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    private boolean removeCore(Object o) {
        boolean ret = backingSet.remove(o);
        if (ret) {
            if (o instanceof PropertyObject) {
                PropertyObjectHelper.removeListener((PropertyObject)o, subInvalidationListener);
            }

            callObservers(new SimpleRemoveChange((E)o));
        }

        return ret;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return containsAllCore(c);
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean containsAllCore(Collection<?> c) {
        if (c instanceof AsyncCollection) {
            try (LockedCollection<? extends E> locked = ((AsyncCollection<? extends E>)c).lock()) {
                return backingSet.containsAll(locked);
            }
        } else {
            return backingSet.containsAll(c);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(Collection<? extends E> c) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();

            if (c instanceof AsyncCollection) {
                try (LockedCollection<? extends E> locked = ((AsyncCollection<? extends E>)c).lock()) {
                    return addAllCore(locked);
                }
            } else {
                return addAllCore(c);
            }
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    private boolean addAllCore(Collection<? extends E> c) {
        boolean ret = false;

        for (E element : c) {
            boolean r = backingSet.add(element);
            if (r) {
                if (element instanceof PropertyObject) {
                    PropertyObjectHelper.addListener((PropertyObject)element, subInvalidationListener);
                }

                callObservers(new SimpleAddChange(element));
            }

            ret |= r;
        }

        return ret;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean retainAll(Collection<?> c) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            if (c instanceof AsyncCollection) {
                try (LockedCollection<? extends E> locked = ((AsyncCollection<? extends E>)c).lock()) {
                    return removeRetainCore(locked, false);
                }
            } else {
                return removeRetainCore(c, false);
            }
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean removeAll(Collection<?> c) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            if (c instanceof AsyncCollection) {
                try (LockedCollection<? extends E> locked = ((AsyncCollection<? extends E>)c).lock()) {
                    return removeRetainCore(locked, true);
                }
            } else {
                return removeRetainCore(c, true);
            }
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    private boolean removeRetainCore(Collection<?> c, boolean remove) {
        boolean removed = false;
        for (Iterator<E> i = backingSet.iterator(); i.hasNext(); ) {
            E element = i.next();
            if (remove == c.contains(element)) {
                if (element instanceof PropertyObject) {
                    PropertyObjectHelper.removeListener((PropertyObject)element, subInvalidationListener);
                }

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
            clearCore();
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    private void clearCore() {
        for (Iterator<E> i = backingSet.iterator(); i.hasNext(); ) {
            E element = i.next();
            if (element instanceof PropertyObject) {
                PropertyObjectHelper.removeListener((PropertyObject)element, subInvalidationListener);
            }

            i.remove();
            callObservers(new SimpleRemoveChange(element));
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

    private void fireSubValueChangedEvent(@SuppressWarnings("unused") Observable observable) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            callObservers(null);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

}
