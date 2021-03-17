/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.desktop.listener;

import java.util.Iterator;

public class WeakListenerListIterator<T> implements Iterator<T> {

    private ListenerItem<? extends T, T> next;
    private T nextRef;
    private Iterator<ListenerItem<? extends T, T>> it;

    int remainingCount = 0;

    private void findNext() {
        try {
            while (it.hasNext()) {
                next = it.next();
                nextRef = next.getListenerProxy();
                if (nextRef != null) {
                    remainingCount++;
                    return;
                }

                it.remove();
            }
        } catch (Throwable e) {
            // if in an other thread something was removed and
            // we are not try to get something beond the end of data
        }

        next = null;
    }

    WeakListenerListIterator(Iterator<ListenerItem<? extends T, T>> it) {
        this.it = it;
        findNext();
    }

    public boolean hasNext() {
        return next != null;
    }

    public T next() {
        T tmp = nextRef;
        findNext();
        return tmp;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
