/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

import java.util.Enumeration;

public class WrappingIterateable<E> implements Iterable<E> {

    WrappingIterator<E> iterator;

    public WrappingIterateable(Enumeration<E> enumeration) {
        this.iterator = new WrappingIterator<E>(enumeration);
    }

    public WrappingIterator<E> iterator() {
        return iterator;
    }

}
