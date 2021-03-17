/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class ArraySet<T> extends ArrayList<T> implements Set<T> {

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
