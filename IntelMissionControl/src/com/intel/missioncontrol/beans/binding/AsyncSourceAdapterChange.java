/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.binding;

import com.intel.missioncontrol.collections.AsyncListChangeListener;
import java.util.List;
import javafx.collections.ObservableList;

class AsyncSourceAdapterChange<E> extends AsyncListChangeListener.AsyncChange<E> {

    private final AsyncListChangeListener.AsyncChange<? extends E> change;
    private int[] perm;

    public AsyncSourceAdapterChange(ObservableList<E> list, AsyncListChangeListener.AsyncChange<? extends E> change) {
        super(list);
        this.change = change;
    }

    @Override
    public boolean next() {
        perm = null;
        return change.next();
    }

    @Override
    public void reset() {
        change.reset();
    }

    @Override
    public int getTo() {
        return change.getTo();
    }

    @Override
    public List<E> getAddedSubList() {
        return (List<E>)change.getAddedSubList();
    }

    @Override
    public List<E> getUpdatedSubList() {
        return (List<E>)change.getUpdatedSubList();
    }

    @Override
    public List<E> getRemoved() {
        return (List<E>)change.getRemoved();
    }

    @Override
    public int getFrom() {
        return change.getFrom();
    }

    @Override
    public boolean wasUpdated() {
        return change.wasUpdated();
    }

    @Override
    protected int[] getPermutation() {
        if (perm == null) {
            if (change.wasPermutated()) {
                final int from = change.getFrom();
                final int n = change.getTo() - from;
                perm = new int[n];
                for (int i = 0; i < n; i++) {
                    perm[i] = change.getPermutation(from + i);
                }
            } else {
                perm = new int[0];
            }
        }

        return perm;
    }

    @Override
    public String toString() {
        return change.toString();
    }

}
