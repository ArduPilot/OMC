/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class RingQueue<T> extends AbstractQueue<T> {

    private static class RingQueueIterator<T> implements Iterator<T> {
        private Object[] items;
        private int pos;
        private int remaining;

        RingQueueIterator(Object[] items, int start, int length) {
            this.items = items;
            this.pos = start;
            this.remaining = length;
        }

        public boolean hasNext() {
            return remaining > 0;
        }

        @SuppressWarnings("unchecked")
        public T next() {
            if (remaining == 0) {
                throw new NoSuchElementException();
            }

            T item = (T)items[pos];
            pos = (pos + 1) % items.length;
            --remaining;
            return item;
        }

        public void remove() {
            throw new UnsupportedOperationException("Removing elements is not supported.");
        }
    }

    private Object[] items;
    private int start;
    private int length;

    public RingQueue(int size) {
        items = new Object[size];
    }

    @Override
    public Iterator<T> iterator() {
        return new RingQueueIterator<>(items, start, length);
    }

    @Override
    public int size() {
        return length;
    }

    @Override
    public boolean offer(T t) {
        int next = (start + length) % items.length;

        if (next == start && length > 0) {
            ++start;

            if (start >= items.length) {
                start = 0;
            }
        } else {
            ++length;
        }

        items[next] = t;

        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T poll() {
        if (length == 0) {
            return null;
        }

        T item = (T)items[start];
        items[start] = null;
        start = (start + 1) % items.length;
        --length;

        return item;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T peek() {
        if (length == 0) {
            return null;
        }

        return (T)items[start];
    }

}
