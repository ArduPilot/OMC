/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.AsyncObservable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.Set;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.util.Callback;
import org.checkerframework.checker.nullness.qual.NonNull;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public final class FXAsyncCollections {

    private FXAsyncCollections() {}

    public static <E> AsyncObservableList<E> emptyObservableList() {
        return new UnmodifiableAsyncObservableListWrapper<>(new ArrayList<>());
    }

    public static <E> AsyncObservableList<E> unmodifiableObservableList(List<E> list) {
        return new UnmodifiableAsyncObservableListWrapper<>(list);
    }

    public static <E> AsyncObservableList<E> unmodifiableObservableList(AsyncObservableList<E> list) {
        return new UnmodifiableAsyncObservableListWrapper<>(list);
    }

    public static <E> AsyncObservableList<E> observableArrayList() {
        return observableList(new ArrayList<>());
    }

    public static <E> AsyncObservableList<E> observableArrayList(Callback<E, AsyncObservable[]> extractor) {
        return observableList(new ArrayList<>(), extractor);
    }

    public static <E> AsyncObservableList<E> observableList(List<E> list) {
        return new AsyncObservableListWrapper<>(list);
    }

    public static <E> AsyncObservableList<E> observableList(List<E> list, Callback<E, AsyncObservable[]> extractor) {
        return new AsyncObservableListWrapper<>(list, extractor);
    }

    public static <E> AsyncObservableSet<E> emptyObservableSet() {
        return new UnmodifiableAsyncObservableSetWrapper<>(new ArraySet<>());
    }

    public static <E> AsyncObservableSet<E> unmodifiableObservableSet(Set<E> set) {
        return new UnmodifiableAsyncObservableSetWrapper<>(set);
    }

    public static <E> AsyncObservableSet<E> observableHashSet() {
        return new AsyncObservableSetWrapper<>(new HashSet<>());
    }

    public static <E> AsyncObservableSet<E> observableArraySet() {
        return new AsyncObservableSetWrapper<>(new ArraySet<>());
    }

    public static <E> AsyncObservableSet<E> observableSet(Set<E> set) {
        return new AsyncObservableSetWrapper<>(set);
    }

    private static class UnmodifiableAsyncObservableListWrapper<E> implements AsyncObservableList<E>, RandomAccess {
        private AsyncObservableListWrapper<E> backingList;

        UnmodifiableAsyncObservableListWrapper(List<E> backingList) {
            this.backingList = new AsyncObservableListWrapper<>(backingList);
        }

        UnmodifiableAsyncObservableListWrapper(AsyncObservableList<E> backingList) {
            this.backingList = new AsyncObservableListWrapper<>(backingList);
        }

        @Override
        public E get(int index) {
            return backingList.get(index);
        }

        @Override
        public boolean isEmpty() {
            return backingList.isEmpty();
        }

        @Override
        public LockedList<E> lock() {
            return backingList.lockUnmodifiable();
        }

        @Override
        public boolean add(E e) {
            throw fail();
        }

        @Override
        public void add(int index, E element) {
            throw fail();
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            throw fail();
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            throw fail();
        }

        @Override
        public boolean addAll(E... elements) {
            throw fail();
        }

        @Override
        public E set(int index, E element) {
            throw fail();
        }

        @Override
        public boolean setAll(Collection<? extends E> col) {
            throw fail();
        }

        @Override
        public boolean setAll(E... elements) {
            throw fail();
        }

        @Override
        public void remove(int from, int to) {
            throw fail();
        }

        @Override
        public boolean remove(Object o) {
            throw fail();
        }

        @Override
        public E remove(int index) {
            throw fail();
        }

        @Override
        public boolean removeAll(@NonNull Collection<?> c) {
            throw fail();
        }

        @Override
        public boolean removeAll(E... elements) {
            throw fail();
        }

        @Override
        public boolean retainAll(@NonNull Collection<?> c) {
            throw fail();
        }

        @Override
        public boolean retainAll(E... elements) {
            throw fail();
        }

        @Override
        public void clear() {
            throw fail();
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
        public Iterator<E> iterator() {
            return backingList.iteratorUnmodifiable();
        }

        @Override
        public ListIterator<E> listIterator(int index) {
            return backingList.listIteratorUnmodifiable(index);
        }

        @Override
        public ListIterator<E> listIterator() {
            return backingList.listIteratorUnmodifiable();
        }

        @Override
        public void addListener(ListChangeListener<? super E> listener, Executor executor) {
            backingList.addListener(listener, executor);
        }

        @Override
        public void addListener(InvalidationListener listener, Executor executor) {
            backingList.addListener(listener, executor);
        }

        @Override
        public void addListener(ListChangeListener<? super E> listener) {
            backingList.addListener(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            backingList.addListener(listener);
        }

        @Override
        public void removeListener(ListChangeListener<? super E> listener) {
            backingList.removeListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            backingList.removeListener(listener);
        }

        @Override
        public Object[] toArray() {
            return backingList.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return backingList.toArray(a);
        }

        @Override
        public int size() {
            return backingList.size();
        }

        @Override
        public boolean contains(Object o) {
            return backingList.contains(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return backingList.containsAll(c);
        }

        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            return backingList.subList(fromIndex, toIndex);
        }

        private IllegalStateException fail() {
            return new IllegalStateException("The list cannot be modified.");
        }
    }

    private static class UnmodifiableAsyncObservableSetWrapper<E> extends AsyncObservableSetWrapper<E>
            implements RandomAccess {
        UnmodifiableAsyncObservableSetWrapper(Set<E> set) {
            super(set);
        }

        @Override
        public boolean add(E e) {
            throw fail();
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            throw fail();
        }

        @Override
        public boolean remove(Object o) {
            throw fail();
        }

        @Override
        public boolean removeAll(@NonNull Collection<?> c) {
            throw fail();
        }

        @Override
        public boolean retainAll(@NonNull Collection<?> c) {
            throw fail();
        }

        private IllegalStateException fail() {
            return new IllegalStateException("The set cannot be modified.");
        }
    }

}
