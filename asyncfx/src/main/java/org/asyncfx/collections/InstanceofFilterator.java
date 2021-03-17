/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.Collection;
import java.util.Iterator;

/** Iterator over a collection that yields all elements for which "E.class instanceof T.class" is true. */
public class InstanceofFilterator<T> implements Iterator<T> {

    private final Class<? extends T> type;
    private final Iterator<? super T> iterator;
    private final Iterator<? super T> previewIterator;
    private int advance;
    private boolean hasNext;

    public InstanceofFilterator(Collection<? super T> collection, Class<? extends T> type) {
        this.type = type;
        iterator = collection.iterator();
        previewIterator = collection.iterator();
        updateHasNext();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T next() {
        for (int i = 0; i < advance - 1; ++i) {
            iterator.next();
        }

        updateHasNext();
        return (T)iterator.next();
    }

    private void updateHasNext() {
        advance = 0;
        hasNext = false;

        for (; previewIterator.hasNext(); ++advance) {
            if (type.isInstance(previewIterator.next())) {
                hasNext = true;
                break;
            }
        }
    }

}
