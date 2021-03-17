/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.AsyncSetExpression;
import org.asyncfx.beans.value.AsyncSubObservableValue;
import org.asyncfx.collections.AsyncObservableSet;
import org.asyncfx.collections.LockedSet;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class ReadOnlyAsyncSetProperty<E> extends AsyncSetExpression<E>
        implements ReadOnlyAsyncProperty<AsyncObservableSet<E>>, AsyncSubObservableValue<AsyncObservableSet<E>> {

    /** Gets the value of this property, regardless of whether it is protected by a {@link ConsistencyGroup}. */
    public abstract AsyncObservableSet<E> getUncritical();

    @Override
    public AsyncObservableSet<E> getValueUncritical() {
        return getUncritical();
    }

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
