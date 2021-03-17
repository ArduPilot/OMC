/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import java.util.ListIterator;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.binding.AsyncListExpression;
import org.asyncfx.beans.value.AsyncSubObservableValue;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.LockedList;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
public abstract class ReadOnlyAsyncListProperty<E> extends AsyncListExpression<E>
        implements ReadOnlyAsyncProperty<AsyncObservableList<E>>, AsyncSubObservableValue<AsyncObservableList<E>> {

    /** Gets the value of this property, regardless of whether it is protected by a {@link ConsistencyGroup}. */
    public abstract AsyncObservableList<E> getUncritical();

    @Override
    public AsyncObservableList<E> getValueUncritical() {
        return getUncritical();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ReadOnlyAsyncListProperty)) {
            return false;
        }

        final ReadOnlyAsyncListProperty list = (ReadOnlyAsyncListProperty)obj;
        try (LockedList<E> thisList = lock();
            LockedList otherList = list.lock()) {
            if (thisList.size() != otherList.size()) {
                return false;
            }

            ListIterator<E> e1 = thisList.listIterator();
            ListIterator e2 = otherList.listIterator();
            while (e1.hasNext() && e2.hasNext()) {
                E o1 = e1.next();
                Object o2 = e2.next();
                if (!(o1 == null ? o2 == null : o1.equals(o2))) {
                    return false;
                }
            }

            return true;
        }
    }

    @Override
    public int hashCode() {
        try (LockedList<E> view = lock()) {
            int hashCode = 1;
            for (E e : view) {
                hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
            }

            return hashCode;
        }
    }

}
