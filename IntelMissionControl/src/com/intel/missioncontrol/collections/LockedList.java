/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import com.intel.missioncontrol.PublishSource;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public abstract class LockedList<E> extends LockedCollection<E> implements List<E>, AutoCloseable {

    private static class EmptyList extends LockedList {
        private static final ListIterator iterator =
            new ListIterator() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Object next() {
                    throw new NoSuchElementException();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean hasPrevious() {
                    return false;
                }

                @Override
                public Object previous() {
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
                public void set(Object e) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void add(Object e) {
                    throw new UnsupportedOperationException();
                }
            };

        @Override
        void closeInternal() {}

        @Override
        public Object get(int index) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public Object set(int index, Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int indexOf(Object o) {
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            return -1;
        }

        @Override
        public ListIterator listIterator() {
            return iterator;
        }

        @Override
        public ListIterator listIterator(int index) {
            return iterator;
        }

        @Override
        public List subList(int fromIndex, int toIndex) {
            if (fromIndex != 0 || toIndex != 0) {
                throw new IndexOutOfBoundsException();
            }

            return this;
        }

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
        public Iterator iterator() {
            return iterator;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public Object[] toArray(Object[] a) {
            return a;
        }

        @Override
        public boolean add(Object e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(int index, Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection c) {
            return false;
        }

        @Override
        public void changeOwner(Thread thread) {
            throw new UnsupportedOperationException();
        }
    }

    public static LockedList empty() {
        return new EmptyList();
    }

}
