/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.sync;

import java.util.Iterator;

public abstract class AWrapperListSyncDelegator extends AWrappedListSyncSourceHandler {

    Iterator<?> iterator;
    Object lastChild = null;

    public AWrapperListSyncDelegator(Object[] arr) {
        this(arr == null ? new EmptyIterator<Object>() : new ArrayIterator<Object>(arr));
    }

    public AWrapperListSyncDelegator(Iterable<?> list) {
        this(list.iterator());
    }

    public AWrapperListSyncDelegator(Iterator<?> iterator) {
        this.iterator = iterator;
    }

    @Override
    public Object getWrapperObjectForLastObject() {
        return getWrapperForObject(lastChild);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Object next() {
        lastChild = iterator.next();
        return lastChild;
    }

    public abstract Object getWrapperForObject(Object o);

}
