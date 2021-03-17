/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import org.asyncfx.PublishSource;
import org.jetbrains.annotations.NotNull;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public abstract class LockedSet<E> extends LockedCollection<E> implements Set<E>, AutoCloseable {

    private static final String NO_MODIFICATION = "An empty set cannot be modified.";

    private static class EmptySet<E> extends LockedSet<E> {
        private final ListIterator<E> iterator =
            new ListIterator<>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public E next() {
                    throw new NoSuchElementException();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException(NO_MODIFICATION);
                }

                @Override
                public boolean hasPrevious() {
                    return false;
                }

                @Override
                public E previous() {
                    throw new NoSuchElementException();
                }

                @Override
                public int nextIndex() {
                    return 0;
                }

                @Override
                public int previousIndex() {
                    return -1;
                }

                @Override
                public void set(E e) {
                    throw new UnsupportedOperationException(NO_MODIFICATION);
                }

                @Override
                public void add(E e) {
                    throw new UnsupportedOperationException(NO_MODIFICATION);
                }
            };

        @Override
        void closeInternal() {}

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

        @Override
        public @NotNull Iterator<E> iterator() {
            return iterator;
        }

        @Override
        public @NotNull Object[] toArray() {
            return new Object[0];
        }

        @Override
        public @NotNull <T> T[] toArray(@NotNull T[] a) {
            return a;
        }

        @Override
        public boolean add(E e) {
            throw new UnsupportedOperationException(NO_MODIFICATION);
        }

        @Override
        public boolean addAll(@NotNull Collection c) {
            throw new UnsupportedOperationException(NO_MODIFICATION);
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean removeAll(@NotNull Collection c) {
            return false;
        }

        @Override
        public boolean retainAll(@NotNull Collection c) {
            return false;
        }

        @Override
        public void clear() {}

        @Override
        public boolean containsAll(@NotNull Collection c) {
            return false;
        }

        @Override
        public void changeOwner(Thread thread) {}
    }

    public static <T> LockedSet<T> empty() {
        return new EmptySet<>();
    }

}
