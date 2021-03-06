/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.util.Callback;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AsyncInvalidationListenerWrapper;
import org.asyncfx.beans.AsyncObservable;
import org.asyncfx.beans.SubInvalidationListener;
import org.asyncfx.beans.AsyncSubInvalidationListenerWrapper;
import org.asyncfx.beans.property.AsyncListPropertyBase;
import org.asyncfx.beans.property.PropertyObject;
import org.asyncfx.beans.property.PropertyObjectHelper;
import org.asyncfx.concurrent.ReentrantStampedLock;
import org.jetbrains.annotations.NotNull;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public class AsyncObservableListWrapper<E> extends AsyncObservableListBase<E>
        implements RandomAccess, AsyncListPropertyBase.ListInitializer {

    private class Itr implements Iterator<E> {
        final int start;
        int cursor;
        int end;
        int lastRet = -1;
        int expectedModCount = modCount;

        Itr(int offset) {
            verifyLockState();
            start = 0;
            end = backingList.size();
            cursor = offset;
        }

        Itr(int offset, int length) {
            verifyLockState();
            start = 0;
            end = offset + length;
            cursor = offset;
        }

        Itr(int start, int offset, int length) {
            verifyLockState();
            this.start = start;
            end = offset + length;
            cursor = offset;
        }

        @Override
        public boolean hasNext() {
            return cursor < end;
        }

        @Override
        public E next() {
            checkForComodification();
            try {
                if (cursor >= end) {
                    throw new NoSuchElementException();
                }

                int i = cursor;
                E next = backingList.get(i);
                lastRet = i;
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }

            checkForComodification();
            try {
                AsyncObservableListWrapper.this.removeCore(lastRet);
                if (lastRet < cursor) {
                    cursor--;
                }

                end--;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        void verifyLockState() {
            if (!lock.isWriteLockedByCurrentThread()) {
                throw new IllegalMonitorStateException("Cannot iterate over an unlocked list.");
            }
        }
    }

    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int offset) {
            super(offset);
        }

        ListItr(int start, int offset, int length) {
            super(start, offset, length);
        }

        @Override
        public boolean hasPrevious() {
            return cursor > start;
        }

        @Override
        public E previous() {
            checkForComodification();
            try {
                int i = cursor - 1;
                if (i < start) {
                    throw new NoSuchElementException();
                }

                E previous = backingList.get(i);
                lastRet = cursor = i;
                return previous;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void set(E e) {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }

            checkForComodification();
            try {
                AsyncObservableListWrapper.this.setCore(lastRet, e);
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void add(E e) {
            checkForComodification();
            try {
                int i = cursor;
                AsyncObservableListWrapper.this.addCore(i, e);
                lastRet = -1;
                cursor = i + 1;
                end++;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private class UnmodifiableItr extends Itr {
        private final LockedCollectionType type;

        UnmodifiableItr(LockedCollectionType type, int offset) {
            super(offset);
            this.type = type;
        }

        UnmodifiableItr(LockedCollectionType type, int offset, int length) {
            super(offset, length);
            this.type = type;
        }

        @Override
        public void remove() {
            throw new IllegalStateException(type.getNoModificationReason());
        }

        @Override
        void verifyLockState() {
            // unmodifiable iterators don't acquire write locks
        }
    }

    private class UnmodifiableListItr extends ListItr {
        private final LockedCollectionType type;

        UnmodifiableListItr(LockedCollectionType type, int offset) {
            super(offset);
            this.type = type;
        }

        UnmodifiableListItr(LockedCollectionType type, int start, int offset, int length) {
            super(start, offset, length);
            this.type = type;
        }

        @Override
        public void set(E e) {
            throw new IllegalStateException(type.getNoModificationReason());
        }

        @Override
        public void add(E e) {
            throw new IllegalStateException("The list cannot be modified.");
        }

        @Override
        void verifyLockState() {
            // unmodifiable iterators don't acquire write locks
        }
    }

    private enum ChangeState {
        ADDING,
        SETTING,
        REMOVING
    }

    private class LockedListImpl extends LockedList<E> {
        private final LockedCollectionType type;
        private final long stamp;

        LockedListImpl(LockedCollectionType type) {
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
                unlockWriteAndFireChanges(stamp);
            }
        }

        @Override
        public int size() {
            return backingList.size();
        }

        @Override
        public boolean isEmpty() {
            return backingList.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return backingList.contains(o);
        }

        @Override
        public @NotNull Iterator<E> iterator() {
            if (type.isReadOnly()) {
                return new UnmodifiableItr(type, 0);
            }

            return new Itr(0);
        }

        @Override
        public @NotNull Object[] toArray() {
            return backingList.toArray();
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
            return addAllCore(size, c);
        }

        @Override
        public boolean addAll(int index, @NotNull Collection<? extends E> c) {
            checkReadOnly();
            return addAllCore(index, c);
        }

        @Override
        public void clear() {
            checkReadOnly();
            clearCore();
        }

        @Override
        public E get(int index) {
            return backingList.get(index);
        }

        @Override
        public E set(int index, E element) {
            checkReadOnly();
            return setCore(index, element);
        }

        @Override
        public void add(int index, E element) {
            checkReadOnly();
            addCore(index, element);
        }

        @Override
        public E remove(int index) {
            checkReadOnly();
            return removeCore(index);
        }

        @Override
        public int indexOf(Object o) {
            return backingList.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return backingList.lastIndexOf(o);
        }

        @Override
        public @NotNull ListIterator<E> listIterator() {
            if (type.isReadOnly()) {
                return new UnmodifiableListItr(type, 0);
            }

            return new ListItr(0);
        }

        @Override
        public @NotNull ListIterator<E> listIterator(int index) {
            if (type.isReadOnly()) {
                return new UnmodifiableListItr(type, index);
            }

            return new ListItr(index);
        }

        @Override
        public @NotNull List<E> subList(int fromIndex, int toIndex) {
            if (type.isReadOnly()) {
                return new LockedUnmodifiableSubListImpl(this, fromIndex, toIndex);
            }

            return new LockedSubListImpl(this, fromIndex, toIndex);
        }

        @Override
        public boolean retainAll(@NotNull Collection c) {
            checkReadOnly();
            return retainAllCore(c);
        }

        @Override
        public boolean removeAll(@NotNull Collection c) {
            checkReadOnly();
            return removeAllCore(c);
        }

        @Override
        public boolean containsAll(@NotNull Collection c) {
            return backingList.containsAll(c);
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull Object[] toArray(@NotNull Object[] a) {
            return backingList.toArray((E[])a);
        }

        private void checkReadOnly() {
            if (type.isReadOnly()) {
                throw new IllegalStateException(type.getNoModificationReason());
            }
        }
    }

    private class LockedSubListImpl implements List<E> {
        final LockedListImpl lockedList;
        private final int fromIndex;
        private int toIndex;

        LockedSubListImpl(LockedListImpl lockedList, int fromIndex, int toIndex) {
            this.lockedList = lockedList;
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
        }

        int getFromIndex() {
            return fromIndex;
        }

        int getToIndex() {
            return toIndex;
        }

        @Override
        public int size() {
            return toIndex - fromIndex;
        }

        @Override
        public boolean isEmpty() {
            return toIndex == fromIndex;
        }

        @Override
        public boolean contains(Object o) {
            for (int i = fromIndex; i < toIndex; ++i) {
                E e = lockedList.get(i);
                if (e != null) {
                    if (e.equals(o)) {
                        return true;
                    }
                } else if (o == null) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public @NotNull Iterator<E> iterator() {
            return new Itr(fromIndex, toIndex - fromIndex);
        }

        @Override
        public @NotNull Object[] toArray() {
            Object[] items = new Object[toIndex - fromIndex];
            for (int i = fromIndex; i < toIndex; ++i) {
                items[i - fromIndex] = lockedList.get(i);
            }

            return items;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull <T> T[] toArray(@NotNull T[] a) {
            int size = toIndex - fromIndex;
            T[] items = a.length >= size ? a : (T[])Array.newInstance(a.getClass().getComponentType(), size);
            for (int i = fromIndex; i < toIndex; ++i) {
                items[i - fromIndex] = (T)lockedList.get(i);
            }

            return items;
        }

        @Override
        public boolean add(E e) {
            lockedList.add(toIndex++, e);
            return true;
        }

        @Override
        public boolean remove(Object o) {
            ListIterator<E> it = new ListItr(fromIndex, fromIndex, toIndex - fromIndex);
            while (it.hasNext()) {
                E item = it.next();
                if (item != null && item.equals(o)) {
                    it.remove();
                    --toIndex;
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return lockedList.containsAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends E> c) {
            boolean modified = lockedList.addAll(c);
            if (modified) {
                toIndex += c.size();
            }

            return modified;
        }

        @Override
        public boolean addAll(int index, @NotNull Collection<? extends E> c) {
            index = fromIndex + index;
            if (index > toIndex) {
                throw new IndexOutOfBoundsException();
            }

            boolean modified = lockedList.addAll(index, c);
            toIndex += c.size();
            return modified;
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return removeOrRetainCore(c, false);
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return removeOrRetainCore(c, true);
        }

        private boolean removeOrRetainCore(Collection<?> c, boolean retain) {
            int oldToIndex = toIndex;
            ListIterator<E> it = new ListItr(fromIndex, fromIndex, toIndex - fromIndex);
            while (it.hasNext()) {
                E item = it.next();
                if (item != null && (retain ^ c.contains(item))) {
                    it.remove();
                    --toIndex;
                }
            }

            return oldToIndex != toIndex;
        }

        @Override
        public void clear() {
            ListIterator<E> it = new ListItr(fromIndex, fromIndex, toIndex - fromIndex);
            while (it.hasNext()) {
                it.next();
                it.remove();
                --toIndex;
            }
        }

        @Override
        public E get(int index) {
            index = fromIndex + index;
            if (index < fromIndex || index > toIndex) {
                throw new IndexOutOfBoundsException();
            }

            return lockedList.get(index);
        }

        @Override
        public E set(int index, E element) {
            index = fromIndex + index;
            if (index < fromIndex || index > toIndex) {
                throw new IndexOutOfBoundsException();
            }

            return lockedList.set(index, element);
        }

        @Override
        public void add(int index, E element) {
            index = fromIndex + index;
            if (index < fromIndex || index > toIndex) {
                throw new IndexOutOfBoundsException();
            }

            lockedList.add(index, element);
        }

        @Override
        public E remove(int index) {
            index = fromIndex + index;
            if (index < fromIndex || index > toIndex) {
                throw new IndexOutOfBoundsException();
            }

            return lockedList.remove(index);
        }

        @Override
        public int indexOf(Object o) {
            ListIterator<E> it = new ListItr(fromIndex, fromIndex, toIndex - fromIndex);
            while (it.hasNext()) {
                E item = it.next();
                if (item != null) {
                    if (item.equals(o)) {
                        return it.nextIndex() - 1;
                    }
                } else if (o == null) {
                    return it.nextIndex() - 1;
                }
            }

            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            ListIterator<E> it = new ListItr(fromIndex, toIndex, toIndex - fromIndex);
            while (it.hasPrevious()) {
                E item = it.previous();
                if (item != null) {
                    if (item.equals(o)) {
                        return it.nextIndex() - 1;
                    }
                } else if (o == null) {
                    return it.nextIndex() - 1;
                }
            }

            return -1;
        }

        @Override
        public @NotNull ListIterator<E> listIterator() {
            return new ListItr(fromIndex, fromIndex, toIndex - fromIndex);
        }

        @Override
        public @NotNull ListIterator<E> listIterator(int index) {
            return new ListItr(fromIndex, toIndex, toIndex - fromIndex);
        }

        @Override
        public @NotNull List<E> subList(int fromIndex, int toIndex) {
            return new LockedSubListImpl(lockedList, this.fromIndex + fromIndex, this.fromIndex + toIndex);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object obj) {
            if (!(obj instanceof Iterable)) {
                return false;
            }

            ListIterator<E> myIt = new ListItr(fromIndex, fromIndex, toIndex - fromIndex);
            Iterable<E> otherList = (Iterable<E>)obj;
            Iterator<E> otherIt = otherList.iterator();

            while (myIt.hasNext() && otherIt.hasNext()) {
                E myObj = myIt.next();
                E otherObj = otherIt.next();

                if (myObj != null && !myObj.equals(otherObj)) {
                    return false;
                } else if (otherObj != null && otherObj.equals(myObj)) {
                    return false;
                }
            }

            return myIt.hasNext() == otherIt.hasNext();
        }

        @Override
        public int hashCode() {
            return backingList.subList(fromIndex, toIndex).hashCode();
        }

        @Override
        public String toString() {
            return backingList.subList(fromIndex, toIndex).toString();
        }
    }

    private class LockedUnmodifiableSubListImpl extends LockedSubListImpl {

        LockedUnmodifiableSubListImpl(LockedListImpl lockedList, int fromIndex, int toIndex) {
            super(lockedList, fromIndex, toIndex);
        }

        @Override
        public @NotNull Iterator<E> iterator() {
            int from = getFromIndex();
            return new UnmodifiableItr(lockedList.type, from, getToIndex() - from);
        }

        @Override
        public @NotNull ListIterator<E> listIterator() {
            int from = getFromIndex();
            return new UnmodifiableListItr(lockedList.type, from, from, getToIndex() - from);
        }

        @Override
        public @NotNull ListIterator<E> listIterator(int index) {
            int from = getFromIndex();
            int to = getToIndex();
            return new UnmodifiableListItr(lockedList.type, from, to, to - from);
        }
    }

    private final ReentrantStampedLock lock = new ReentrantStampedLock();
    private final List<E> backingList;
    private final AsyncElementObserver<E> elementObserver;
    private final AsyncListChangeListener<E> listener;
    private InvalidationListener subInvalidationListener = this::fireSubValueChangedEvent;
    private int size;
    private ChangeState changeState;
    private List<ListChangeListener.Change<? extends E>> aggregatedChanges;
    private boolean subChanged;

    AsyncObservableListWrapper(List<E> backingList) {
        this.backingList = backingList;
        this.elementObserver = null;
        this.listener = null;
    }

    AsyncObservableListWrapper(AsyncObservableList<E> backingList) {
        this.backingList = backingList;
        this.elementObserver = null;
        this.listener = c -> fireChange(new AsyncSourceAdapterChange<>(AsyncObservableListWrapper.this, c), false);
        backingList.addListener(new WeakListChangeListener<>(listener));
    }

    AsyncObservableListWrapper(List<E> backingList, Callback<E, AsyncObservable[]> extractor) {
        this.backingList = backingList;
        this.listener = null;
        this.elementObserver =
            new AsyncElementObserver<>(
                extractor,
                listenerGenerator ->
                    observable -> {
                        long stamp = 0;
                        try {
                            stamp = lock.writeLock();
                            ensureIsChanging(ChangeState.SETTING);
                            int i = 0;
                            final int size = size();
                            for (; i < size; ++i) {
                                if (get(i) == listenerGenerator) {
                                    nextUpdate(i);
                                }
                            }
                        } finally {
                            unlockWriteAndFireChanges(stamp);
                        }
                    },
                this);
    }

    @Override
    public void initializeList(Executor executor) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();

            subInvalidationListener = AsyncInvalidationListenerWrapper.wrap(this::fireSubValueChangedEvent, executor);

            if (elementObserver != null) {
                if (elementObserver.getExecutor() != null) {
                    throw new IllegalStateException("Executor already initialized.");
                }

                elementObserver.setExecutor(executor);

                for (E item : backingList) {
                    if (item instanceof PropertyObject) {
                        PropertyObjectHelper.addListener((PropertyObject)item, subInvalidationListener);
                    }

                    elementObserver.attachListener(item);
                }
            } else {
                for (E item : backingList) {
                    if (item instanceof PropertyObject) {
                        PropertyObjectHelper.addListener((PropertyObject)item, subInvalidationListener);
                    }
                }
            }
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    private void fireSubValueChangedEvent(@SuppressWarnings("unused") Observable observable) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            ensureIsChanging(ChangeState.SETTING);
            subChanged = true;
        } finally {
            unlockWriteAndFireChanges(stamp);
        }
    }

    private void unlockWriteAndFireChanges(long stamp) {
        List<ListChangeListener.Change<? extends E>> changes = null;
        if (changeState != null) {
            changeState = null;
            aggregatedChanges = new ArrayList<>();
            endChange();
            changes = aggregatedChanges;
            aggregatedChanges = null;
        }

        try {
            if (changes != null) {
                boolean anyChange = false;

                for (int i = 0; i < changes.size(); ++i) {
                    fireChange(changes.get(i), i == changes.size() - 1);
                    anyChange = true;
                }

                if (!anyChange && subChanged) {
                    fireChange(null, true);
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            subChanged = false;

            if (ReentrantStampedLock.isWriteLockStamp(stamp)) {
                lock.unlockWrite(stamp);
            }
        }
    }

    @Override
    protected void fireChange(ListChangeListener.Change<? extends E> change, boolean finalChange) {
        if (aggregatedChanges != null) {
            aggregatedChanges.add(change);
        } else {
            super.fireChange(change, finalChange);
        }
    }

    private void ensureIsChanging(ChangeState changeState) {
        if (this.changeState == null) {
            this.changeState = changeState;
            super.beginChange();
        } else if (this.changeState != changeState) {
            super.endChange();
            this.changeState = changeState;
            super.beginChange();
        }
    }

    @Override
    public void addListener(InvalidationListener listener, Executor executor) {
        addListener(AsyncInvalidationListenerWrapper.wrap(listener, executor));
    }

    @Override
    public void addListener(SubInvalidationListener listener, Executor executor) {
        addListener(AsyncSubInvalidationListenerWrapper.wrap(listener, executor));
    }

    @Override
    public void addListener(ListChangeListener<? super E> listener, Executor executor) {
        addListener(ListChangeListenerWrapper.wrap(listener, executor));
    }

    @Override
    public final LockedList<E> lock() {
        return new LockedListImpl(
            lock.isWriteLockedByCurrentThread() ? LockedCollectionType.NESTED_READONLY : LockedCollectionType.WRITABLE);
    }

    LockedList<E> lockUnmodifiable() {
        return new LockedListImpl(LockedCollectionType.EXPLICIT_READONLY);
    }

    @Override
    public final Iterator<E> iterator() {
        return new Itr(0);
    }

    @Override
    public final ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    @Override
    public final ListIterator<E> listIterator(int index) {
        return new ListItr(index);
    }

    final Iterator<E> iteratorUnmodifiable() {
        return new UnmodifiableItr(LockedCollectionType.NESTED_READONLY, 0);
    }

    final ListIterator<E> listIteratorUnmodifiable() {
        return new UnmodifiableListItr(LockedCollectionType.NESTED_READONLY, 0);
    }

    final ListIterator<E> listIteratorUnmodifiable(int index) {
        return new UnmodifiableListItr(LockedCollectionType.NESTED_READONLY, index);
    }

    // ******************************************************************************************************************
    // Read operations
    //
    @Override
    public final int size() {
        long stamp = 0;
        try {
            if ((stamp = lock.tryOptimisticRead()) != 0) {
                int size = this.size;
                if (lock.validate(stamp)) {
                    return size;
                }
            }

            stamp = lock.readLock();
            return this.size;
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
            if ((stamp = lock.tryOptimisticRead()) != 0) {
                boolean empty = this.size == 0;
                if (lock.validate(stamp)) {
                    return empty;
                }
            }

            stamp = lock.readLock();
            return this.size == 0;
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
            return backingList.contains(o);
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        long stamp = 0;
        try {
            stamp = lock.readLock();

            if (c instanceof AsyncCollection) {
                try (LockedCollection<?> locked = ((AsyncCollection<?>)c).lock()) {
                    return backingList.containsAll(locked);
                }
            } else {
                return backingList.containsAll(c);
            }
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public final E get(int index) {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return backingList.get(index);
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public int indexOf(Object o) {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return backingList.indexOf(o);
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return backingList.lastIndexOf(o);
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    // ******************************************************************************************************************
    // Simple write operations
    //
    @Override
    public boolean add(E e) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            return addCore(e);
        } finally {
            unlockWriteAndFireChanges(stamp);
        }
    }

    private boolean addCore(E e) {
        ensureIsChanging(ChangeState.ADDING);
        int index = backingList.size();
        backingList.add(index, e);

        if (e instanceof PropertyObject) {
            PropertyObjectHelper.addListener((PropertyObject)e, subInvalidationListener);
        }

        if (elementObserver != null) {
            elementObserver.attachListener(e);
        }

        nextAdd(index, index + 1);
        ++modCount;
        ++size;
        return true;
    }

    @Override
    public void add(int index, E element) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            addCore(index, element);
        } finally {
            unlockWriteAndFireChanges(stamp);
        }
    }

    private void addCore(int index, E element) {
        ensureIsChanging(ChangeState.ADDING);
        backingList.add(index, element);

        if (element instanceof PropertyObject) {
            PropertyObjectHelper.addListener((PropertyObject)element, subInvalidationListener);
        }

        if (elementObserver != null) {
            elementObserver.attachListener(element);
        }

        nextAdd(index, index + 1);
        ++modCount;
        ++size;
    }

    @Override
    public E set(int index, E element) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            return setCore(index, element);
        } finally {
            unlockWriteAndFireChanges(stamp);
        }
    }

    private E setCore(int index, E element) {
        ensureIsChanging(ChangeState.SETTING);
        E old = backingList.set(index, element);

        if (old instanceof PropertyObject) {
            PropertyObjectHelper.removeListener((PropertyObject)old, subInvalidationListener);
        }

        if (element instanceof PropertyObject) {
            PropertyObjectHelper.addListener((PropertyObject)element, subInvalidationListener);
        }

        if (elementObserver != null) {
            elementObserver.detachListener(old);
            elementObserver.attachListener(element);
        }

        nextSet(index, old);
        return old;
    }

    @Override
    public boolean remove(Object o) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            return removeCore(o);
        } finally {
            unlockWriteAndFireChanges(stamp);
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private boolean removeCore(Object o) {
        int i = backingList.indexOf(o);
        if (i != -1) {
            ensureIsChanging(ChangeState.REMOVING);
            E old = backingList.remove(i);

            if (old instanceof PropertyObject) {
                PropertyObjectHelper.removeListener((PropertyObject)old, subInvalidationListener);
            }

            if (elementObserver != null) {
                elementObserver.detachListener(old);
            }

            nextRemove(i, old);
            ++modCount;
            --size;
            return true;
        }

        return false;
    }

    @Override
    public E remove(int index) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            return removeCore(index);
        } finally {
            unlockWriteAndFireChanges(stamp);
        }
    }

    private E removeCore(int index) {
        ensureIsChanging(ChangeState.REMOVING);
        E old = backingList.remove(index);

        if (old instanceof PropertyObject) {
            PropertyObjectHelper.removeListener((PropertyObject)old, subInvalidationListener);
        }

        if (elementObserver != null) {
            elementObserver.detachListener(old);
        }

        nextRemove(index, old);
        ++modCount;
        --size;
        return old;
    }

    // ******************************************************************************************************************
    // Bulk write operations
    //
    @Override
    public void clear() {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            clearCore();
        } finally {
            unlockWriteAndFireChanges(stamp);
        }
    }

    private void clearCore() {
        ensureIsChanging(ChangeState.REMOVING);
        nextRemove(0, backingList);
        if (elementObserver != null) {
            for (E item : backingList) {
                if (item instanceof PropertyObject) {
                    PropertyObjectHelper.removeListener((PropertyObject)item, subInvalidationListener);
                }

                elementObserver.detachListener(item);
            }
        } else {
            for (E item : backingList) {
                if (item instanceof PropertyObject) {
                    PropertyObjectHelper.removeListener((PropertyObject)item, subInvalidationListener);
                }
            }
        }

        backingList.clear();
        ++modCount;
        size = 0;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            return addAllCore(backingList.size(), c);
        } finally {
            unlockWriteAndFireChanges(stamp);
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            return addAllCore(index, c);
        } finally {
            unlockWriteAndFireChanges(stamp);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean addAllCore(int index, Collection<? extends E> c) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index");
        }

        boolean modified = false;

        if (c instanceof AsyncCollection) {
            try (LockedCollection<? extends E> locked = ((AsyncCollection<? extends E>)c).lock()) {
                for (E e : locked) {
                    addCore(index++, e);
                    modified = true;
                }
            }
        } else {
            for (E e : c) {
                addCore(index++, e);
                modified = true;
            }
        }

        return modified;
    }

    @Override
    public boolean setAll(Collection<? extends E> col) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            return setAllCore(col);
        } finally {
            unlockWriteAndFireChanges(stamp);
        }
    }

    private boolean setAllCore(Collection<? extends E> col) {
        clearCore();
        return addAllCore(0, col);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            removeRangeCore(fromIndex, toIndex);
        } finally {
            unlockWriteAndFireChanges(stamp);
        }
    }

    private void removeRangeCore(int fromIndex, int toIndex) {
        ensureIsChanging(ChangeState.REMOVING);
        ListIterator<E> it = backingList.listIterator(fromIndex);
        for (int i = 0, n = toIndex - fromIndex; i < n; i++) {
            E old = it.next();

            if (old instanceof PropertyObject) {
                PropertyObjectHelper.removeListener((PropertyObject)old, subInvalidationListener);
            }

            if (elementObserver != null) {
                elementObserver.detachListener(old);
            }

            it.remove();
            nextRemove(i + fromIndex, old);
            ++modCount;
            --size;
        }
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            return removeAllCore(c);
        } finally {
            unlockWriteAndFireChanges(stamp);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean removeAllCore(Collection<?> c) {
        ensureIsChanging(ChangeState.REMOVING);
        boolean modified = false;
        Iterator<?> it = backingList.iterator();
        int i = 0;
        while (it.hasNext()) {
            Object old = it.next();
            if (c.contains(old)) {
                if (old instanceof PropertyObject) {
                    PropertyObjectHelper.removeListener((PropertyObject)old, subInvalidationListener);
                }

                if (elementObserver != null) {
                    elementObserver.detachListener((E)old);
                }

                it.remove();
                nextRemove(i, (E)old);
                ++modCount;
                --size;
                modified = true;
            }

            ++i;
        }

        return modified;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        long stamp = 0;
        try {
            stamp = lock.writeLock();
            return retainAllCore(c);
        } finally {
            unlockWriteAndFireChanges(stamp);
        }
    }

    private boolean retainAllCore(Collection<?> c) {
        ensureIsChanging(ChangeState.REMOVING);
        boolean modified = false;
        Iterator<E> it = backingList.iterator();
        int i = 0;
        while (it.hasNext()) {
            E old = it.next();
            if (!c.contains(old)) {
                if (old instanceof PropertyObject) {
                    PropertyObjectHelper.removeListener((PropertyObject)old, subInvalidationListener);
                }

                if (elementObserver != null) {
                    elementObserver.detachListener(old);
                }

                it.remove();
                nextRemove(i, old);
                ++modCount;
                --size;
                modified = true;
            }

            ++i;
        }

        return modified;
    }

    @Override
    public @NotNull List<E> subList(int fromIndex, int toIndex) {
        throw new IllegalStateException("Cannot get a sublist of an unlocked list.");
    }

    @Override
    public int hashCode() {
        long stamp = 0;
        try {
            stamp = lock.readLock();
            return backingList.hashCode();
        } finally {
            if (ReentrantStampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof AsyncObservableListWrapper)) {
            return false;
        }

        AsyncObservableListWrapper<?> otherList = (AsyncObservableListWrapper<?>)other;
        long myStamp = 0, otherStamp = 0;

        try {
            myStamp = lock.writeLock();
            otherStamp = otherList.lock.writeLock();

            ListIterator<E> e1 = listIterator();
            ListIterator<?> e2 = otherList.listIterator();
            while (e1.hasNext() && e2.hasNext()) {
                E o1 = e1.next();
                Object o2 = e2.next();
                if (!(o1 == null ? o2 == null : o1.equals(o2))) {
                    return false;
                }
            }

            return !(e1.hasNext() || e2.hasNext());
        } finally {
            if (ReentrantStampedLock.isWriteLockStamp(myStamp)) {
                lock.unlockWrite(myStamp);
            }

            if (ReentrantStampedLock.isWriteLockStamp(otherStamp)) {
                otherList.lock.unlockWrite(otherStamp);
            }
        }
    }

    public String toString() {
        if (lock.isWriteLockedByCurrentThread()) {
            return toStringImpl(this);
        }

        try (LockedList<E> lockedList = lock()) {
            return toStringImpl(lockedList);
        }
    }

    private String toStringImpl(List<E> list) {
        Iterator<E> it = list.iterator();
        if (!it.hasNext()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (; ; ) {
            E e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (!it.hasNext()) {
                return sb.append(']').toString();
            }

            sb.append(',').append(' ');
        }
    }

}
