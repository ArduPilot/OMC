/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class ArraySet<T> extends ArrayList<T> implements Set<T> {

    public ArraySet() {}

    public ArraySet(int initialCapacity) {
        super(initialCapacity);
    }

    public ArraySet(Collection<? extends T> c) {
        super(c);
    }

    @Override
    public boolean add(T kvEntry) {
        if (contains(kvEntry)) {
            return false;
        }

        return super.add(kvEntry);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for (T t : c) {
            if (add(t)) {
                changed = true;
            }
        }

        return changed;
    }

}
