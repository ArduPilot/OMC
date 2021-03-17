/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

import java.util.Iterator;
import java.util.Vector;

/**
 * this vector is able to remove the correct element by giving an object, even multiple "equal()" of them are in this
 * vector
 *
 * @author caller
 * @param <T>
 */
public class VectorNonEqual<T> extends Vector<T> {

    /** */
    private static final long serialVersionUID = -3587724066643096584L;

    @Override
    public synchronized int indexOf(Object o, int index) {

        if (o == null) {
            for (int i = index; i < elementCount; i++) {
                if (elementData[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = index; i < elementCount; i++) {
                if (o == elementData[i]) {
                    return i;
                }
            }
        }

        return -1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized VectorNonEqual<T> clone() {
        VectorNonEqual<T> tmp = new VectorNonEqual<T>();
        for (T i : this) {
            if (i instanceof MClonable) {
                MClonable cl = (MClonable)i;
                tmp.add((T)cl.clone());
            } else {
                tmp.add(i);
            }
        }

        return tmp;
    }

    @Override
    public synchronized Iterator<T> iterator() {
        return new ItrConcurrentModificationSave(this.size());
    }

    private class ItrConcurrentModificationSave implements Iterator<T> {
        int cursor = 0;
        T next;
        int size = 0;
        ItrConcurrentModificationSave(int _size) {
            size=_size;
            findNext();
        }

        void findNext() {
            if(cursor>=size) {
                next=null;
                return;
            }
            try {
                next = get(cursor);
                cursor++;
            }
            catch(Exception e) {
                next = null;
            }
        }

        public boolean hasNext() {
            return next != null;
        }

        public T next() {
            T o = next;
            findNext();
            return o;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}
