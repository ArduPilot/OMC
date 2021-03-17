/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.property;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.binding.AsyncSetExpression;
import com.intel.missioncontrol.collections.AsyncObservableSet;
import com.intel.missioncontrol.collections.LockedSet;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class ReadOnlyAsyncSetProperty<E> extends AsyncSetExpression<E>
        implements ReadOnlyAsyncProperty<AsyncObservableSet<E>> {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ReadOnlyAsyncSetProperty)) {
            return false;
        }

        ReadOnlyAsyncSetProperty c = (ReadOnlyAsyncSetProperty)obj;
        if (c.size() != size()) {
            return false;
        }

        try {
            return containsAll(c);
        } catch (ClassCastException unused) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        try (LockedSet<E> view = lock()) {
            int hashCode = 0;
            for (E e : view) {
                hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
            }

            return hashCode;
        }
    }

}
