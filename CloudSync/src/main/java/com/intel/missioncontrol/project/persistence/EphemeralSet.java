/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.persistence;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import org.asyncfx.collections.ArraySet;
import org.jetbrains.annotations.NotNull;

/** Items placed in an {@link EphemeralSet} are automatically removed after a specified time. */
public class EphemeralSet<E> implements Set<E> {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(50);

    private static class Entry<E> {
        E value;
        long nanoTime;

        Entry(E value) {
            this.value = value;
            this.nanoTime = System.nanoTime();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Entry)) {
                return false;
            }

            Entry other = (Entry)obj;
            return value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    private final long timeoutNanos;
    private final Set<Entry<E>> set = new ArraySet<>();

    public EphemeralSet() {
        timeoutNanos = DEFAULT_TIMEOUT.toNanos();
    }

    public EphemeralSet(Duration timeout) {
        this.timeoutNanos = timeout.toNanos();
    }

    @Override
    public int size() {
        purge();
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        purge();
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        purge();

        for (Entry<E> e : set) {
            if (Objects.equals(e.value, o)) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        purge();

        return new Iterator<>() {
            final Iterator<Entry<E>> it = set.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public E next() {
                return it.next().value;
            }
        };
    }

    @NotNull
    @Override
    public Object[] toArray() {
        Iterator<E> it = iterator();
        Object[] res = new Object[set.size()];
        for (int i = 0; i < res.length; ++i) {
            res[i] = it.next();
        }

        return res;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(@NotNull T[] a) {
        Object[] temp = toArray();
        return (T[])Arrays.copyOf(temp, temp.length, a.getClass());
    }

    @Override
    public boolean add(E e) {
        return set.add(new Entry<>(e));
    }

    @Override
    public boolean remove(Object o) {
        long now = System.nanoTime();
        Iterator<Entry<E>> it = set.iterator();
        while (it.hasNext()) {
            Entry<E> entry = it.next();

            if (Math.abs(now - entry.nanoTime) < timeoutNanos || Objects.equals(entry.value, o)) {
                it.remove();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        boolean res = false;
        for (E e : c) {
            res |= add(e);
        }

        return res;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean res = false;
        long now = System.nanoTime();
        Iterator<Entry<E>> it = set.iterator();
        while (it.hasNext()) {
            Entry<E> entry = it.next();
            if (Math.abs(now - entry.nanoTime) < timeoutNanos || !c.contains(entry.value)) {
                it.remove();
                res = true;
            }
        }

        return res;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean res = false;
        for (Object e : c) {
            res |= remove(e);
        }

        return res;
    }

    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EphemeralSet)) {
            return false;
        }

        EphemeralSet other = (EphemeralSet)o;
        Iterator thisIt = iterator();
        Iterator otherIt = other.iterator();

        while (true) {
            if (!thisIt.hasNext() && !otherIt.hasNext()) {
                return true;
            }

            if (thisIt.hasNext() != otherIt.hasNext()) {
                return false;
            }

            if (!Objects.equals(thisIt.next(), otherIt.next())) {
                return false;
            }
        }
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    @SuppressWarnings("Java8CollectionRemoveIf")
    private void purge() {
        long now = System.nanoTime();
        Iterator<Entry<E>> it = set.iterator();
        while (it.hasNext()) {
            if (Math.abs(now - it.next().nanoTime) > timeoutNanos) {
                it.remove();
            }
        }
    }

}
