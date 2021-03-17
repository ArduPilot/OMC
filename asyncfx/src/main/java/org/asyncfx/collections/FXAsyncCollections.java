/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.util.Callback;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AsyncObservable;
import org.jetbrains.annotations.NotNull;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public final class FXAsyncCollections {

    private FXAsyncCollections() {}

    /**
     * Returns an {@link AsyncObservableList} that will allow mutating methods to be called (add, set, etc.), but will
     * always remain empty.
     */
    public static <E> AsyncObservableList<E> nullObservableList() {
        return new NullAsyncObservableList<>();
    }

    /**
     * Returns an {@link AsyncObservableList} that is unmodifiable and will always be empty. Calling a mutating method
     * (add, set, etc.) will throw an {@link IllegalStateException}.
     */
    public static <E> AsyncObservableList<E> emptyObservableList() {
        return new UnmodifiableAsyncObservableListWrapper<>(new ArrayList<>());
    }

    /**
     * Returns an {@link AsyncObservableList} that is unmodifiable and is backed by the specified list. Calling a
     * mutating method add, set, etc.) will throw an {@link IllegalStateException}.
     */
    public static <E> AsyncObservableList<E> unmodifiableObservableList(List<E> list) {
        return new UnmodifiableAsyncObservableListWrapper<>(list);
    }

    /**
     * Returns an {@link AsyncObservableList} that is unmodifiable and is backed by the specified list. Calling a
     * mutating method add, set, etc.) will throw an {@link IllegalStateException}.
     */
    public static <E> AsyncObservableList<E> unmodifiableObservableList(AsyncObservableList<E> list) {
        return new UnmodifiableAsyncObservableListWrapper<>(list);
    }

    /** Returns a modifiable {@link AsyncObservableList} that is backed by an {@link ArrayList}. */
    public static <E> AsyncObservableList<E> observableArrayList() {
        return observableList(new ArrayList<>());
    }

    /**
     * Returns a modifiable {@link AsyncObservableList} that is backed by an {@link ArrayList}. The extractor interface
     * specifies properties of the elements that, when changed, will cause the list to raise an invalidation event.
     */
    public static <E> AsyncObservableList<E> observableArrayList(Callback<E, AsyncObservable[]> extractor) {
        return observableList(new ArrayList<>(), extractor);
    }

    /** Returns a modifiable {@link AsyncObservableList} that is backed by a pre-existing list. */
    public static <E> AsyncObservableList<E> observableList(List<E> list) {
        return new AsyncObservableListWrapper<>(list);
    }

    /**
     * Returns a modifiable {@link AsyncObservableList} that is backed by a pre-existing list. The extractor interface
     * specifies properties of the elements that, when changed, will cause the list to raise an invalidation event.
     */
    public static <E> AsyncObservableList<E> observableList(List<E> list, Callback<E, AsyncObservable[]> extractor) {
        return new AsyncObservableListWrapper<>(list, extractor);
    }

    /**
     * Returns an {@link AsyncObservableSet} that is unmodifiable and will always be empty. Calling a mutating method
     * (add, remove, etc.) will throw an {@link IllegalStateException}.
     */
    public static <E> AsyncObservableSet<E> emptyObservableSet() {
        return new UnmodifiableAsyncObservableSetWrapper<>(new ArraySet<>());
    }

    /**
     * Returns an {@link AsyncObservableSet} that is unmodifiable and is backed by the specified set. Calling a mutating
     * method (add, remove, etc.) will throw an {@link IllegalStateException}.
     */
    public static <E> AsyncObservableSet<E> unmodifiableObservableSet(Set<E> set) {
        return new UnmodifiableAsyncObservableSetWrapper<>(set);
    }

    /** Returns a modifiable {@link AsyncObservableSet} that is backed by a {@link HashSet}. */
    public static <E> AsyncObservableSet<E> observableHashSet() {
        return new AsyncObservableSetWrapper<>(new HashSet<>());
    }

    /** Returns a modifiable {@link AsyncObservableSet} that is backed by a {@link ArraySet}. */
    public static <E> AsyncObservableSet<E> observableArraySet() {
        return new AsyncObservableSetWrapper<>(new ArraySet<>());
    }

    /** Returns a modifiable {@link AsyncObservableSet} that is backed by a pre-existing set. */
    public static <E> AsyncObservableSet<E> observableSet(Set<E> set) {
        return new AsyncObservableSetWrapper<>(set);
    }

    private static class EmptyIterator<E> implements Iterator<E> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public E next() {
            return null;
        }
    }

    private static class EmptyListIterator<E> implements ListIterator<E> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public E next() {
            return null;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }

        @Override
        public E previous() {
            return null;
        }

        @Override
        public int nextIndex() {
            return -1;
        }

        @Override
        public int previousIndex() {
            return -1;
        }

        @Override
        public void remove() {}

        @Override
        public void set(E e) {}

        @Override
        public void add(E e) {}
    }

    private static class NullAsyncObservableList<E> implements AsyncObservableList<E>, RandomAccess {
        @Override
        public E get(int index) {
            throw new NoSuchElementException();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public LockedList<E> lock() {
            return new LockedList<>() {
                @Override
                public int size() {
                    return 0;
                }

                @Override
                public boolean isEmpty() {
                    return true;
                }

                @Override
                public boolean contains(Object o) {
                    return false;
                }

                @NotNull
                @Override
                public Iterator<E> iterator() {
                    return new EmptyIterator<>();
                }

                @NotNull
                @Override
                public Object[] toArray() {
                    return new Object[0];
                }

                @NotNull
                @Override
                @SuppressWarnings("unchecked")
                public <T> T[] toArray(@NotNull T[] a) {
                    return (T[])Array.newInstance(a.getClass(), 0);
                }

                @Override
                public boolean add(E e) {
                    return false;
                }

                @Override
                public boolean remove(Object o) {
                    return false;
                }

                @Override
                public boolean containsAll(@NotNull Collection<?> c) {
                    return false;
                }

                @Override
                public boolean addAll(@NotNull Collection<? extends E> c) {
                    return false;
                }

                @Override
                public boolean removeAll(@NotNull Collection<?> c) {
                    return false;
                }

                @Override
                public boolean retainAll(@NotNull Collection<?> c) {
                    return false;
                }

                @Override
                public void clear() {}

                @Override
                public boolean addAll(int index, @NotNull Collection<? extends E> c) {
                    return false;
                }

                @Override
                public E get(int index) {
                    throw new IndexOutOfBoundsException();
                }

                @Override
                public E set(int index, E element) {
                    return null;
                }

                @Override
                public void add(int index, E element) {}

                @Override
                public E remove(int index) {
                    throw new IndexOutOfBoundsException();
                }

                @Override
                public int indexOf(Object o) {
                    return -1;
                }

                @Override
                public int lastIndexOf(Object o) {
                    return -1;
                }

                @NotNull
                @Override
                public ListIterator<E> listIterator() {
                    return new EmptyListIterator<>();
                }

                @NotNull
                @Override
                public ListIterator<E> listIterator(int index) {
                    return new EmptyListIterator<>();
                }

                @NotNull
                @Override
                public List<E> subList(int fromIndex, int toIndex) {
                    return new ArrayList<>(0);
                }

                @Override
                void closeInternal() {}

                @Override
                public void changeOwner(Thread thread) {}
            };
        }

        @Override
        public boolean add(E e) {
            return true;
        }

        @Override
        public void add(int index, E element) {}

        @Override
        public boolean addAll(@NotNull Collection<? extends E> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, @NotNull Collection<? extends E> c) {
            return false;
        }

        @Override
        @SafeVarargs
        public final boolean addAll(E... elements) {
            return false;
        }

        @Override
        public E set(int index, E element) {
            return null;
        }

        @Override
        public boolean setAll(Collection<? extends E> col) {
            return false;
        }

        @Override
        @SafeVarargs
        public final boolean setAll(E... elements) {
            return false;
        }

        @Override
        public void remove(int from, int to) {}

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public E remove(int index) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        @SafeVarargs
        public final boolean removeAll(E... elements) {
            return false;
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        @SafeVarargs
        public final boolean retainAll(E... elements) {
            return false;
        }

        @Override
        public void clear() {}

        @Override
        public int indexOf(Object o) {
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            return -1;
        }

        @Override
        public @NotNull Iterator<E> iterator() {
            return new EmptyIterator<>();
        }

        @Override
        public @NotNull ListIterator<E> listIterator(int index) {
            return new EmptyListIterator<>();
        }

        @Override
        public @NotNull ListIterator<E> listIterator() {
            return new EmptyListIterator<>();
        }

        @Override
        public void addListener(ListChangeListener<? super E> listener, Executor executor) {}

        @Override
        public void addListener(InvalidationListener listener, Executor executor) {}

        @Override
        public void addListener(ListChangeListener<? super E> listener) {}

        @Override
        public void addListener(InvalidationListener listener) {}

        @Override
        public void removeListener(ListChangeListener<? super E> listener) {}

        @Override
        public void removeListener(InvalidationListener listener) {}

        @Override
        public @NotNull Object[] toArray() {
            return new Object[0];
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull <T> T[] toArray(@NotNull T[] a) {
            return (T[])Array.newInstance(a.getClass(), 0);
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public @NotNull List<E> subList(int fromIndex, int toIndex) {
            return new ArrayList<>(0);
        }
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
        public boolean addAll(@NotNull Collection<? extends E> c) {
            throw fail();
        }

        @Override
        public boolean addAll(int index, @NotNull Collection<? extends E> c) {
            throw fail();
        }

        @Override
        @SafeVarargs
        public final boolean addAll(E... elements) {
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
        @SafeVarargs
        public final boolean setAll(E... elements) {
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
        public boolean removeAll(@NotNull Collection<?> c) {
            throw fail();
        }

        @Override
        @SafeVarargs
        public final boolean removeAll(E... elements) {
            throw fail();
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            throw fail();
        }

        @Override
        @SafeVarargs
        public final boolean retainAll(E... elements) {
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
        public @NotNull Iterator<E> iterator() {
            return backingList.iteratorUnmodifiable();
        }

        @Override
        public @NotNull ListIterator<E> listIterator(int index) {
            return backingList.listIteratorUnmodifiable(index);
        }

        @Override
        public @NotNull ListIterator<E> listIterator() {
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
        public @NotNull Object[] toArray() {
            return backingList.toArray();
        }

        @Override
        public @NotNull <T> T[] toArray(@NotNull T[] a) {
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
        public boolean containsAll(@NotNull Collection<?> c) {
            return backingList.containsAll(c);
        }

        @Override
        public @NotNull List<E> subList(int fromIndex, int toIndex) {
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
        public boolean removeAll(@NotNull Collection<?> c) {
            throw fail();
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            throw fail();
        }

        private IllegalStateException fail() {
            return new IllegalStateException("The set cannot be modified.");
        }
    }

}
