/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper.sync;

import java.util.Iterator;

public class ArrayIterator<T> implements Iterator<T> {

    T[] arr;
    int i = 0;

    public ArrayIterator(T[] arr) {
        this.arr = arr;
    }

    @Override
    public boolean hasNext() {
        return i < arr.length;
    }

    @Override
    public T next() {
        return arr[i++];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
