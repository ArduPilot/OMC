/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import java.util.AbstractList;

/** List that can contain up to two values. */
public class PairList<E> extends AbstractList<E> {

    private E e0;
    private E e1;
    private int size;

    public PairList() {}

    public PairList(E e0, E e1) {
        this.e0 = e0;
        this.e1 = e1;
        this.size = 2;
    }

    @Override
    public boolean add(E e) {
        switch (size++) {
        case 0:
            e0 = e;
            return true;
        case 1:
            e1 = e;
            return true;
        default:
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public E get(int index) {
        switch (index) {
        case 0:
            return e0;
        case 1:
            return e1;
        default:
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int size() {
        return size;
    }

}
