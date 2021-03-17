/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.List;
import org.asyncfx.PublishSource;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class AsyncSourceAdapterChange<E> extends AsyncListChangeListener.AsyncChange<E> implements UnsafeListAccess<E> {

    private final AsyncListChangeListener.AsyncChange<? extends E> change;
    private int[] perm;

    public AsyncSourceAdapterChange(
            AsyncObservableList<E> list, AsyncListChangeListener.AsyncChange<? extends E> change) {
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
    public AsyncObservableList<E> getListUnsafe() {
        return super.getList();
    }

    @Override
    public AsyncObservableList<E> getList() {
        throw new UnsupportedOperationException("Cannot query the source list of an async change.");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> getAddedSubList() {
        return (List<E>)change.getAddedSubList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> getUpdatedSubList() {
        return (List<E>)change.getUpdatedSubList();
    }

    @Override
    @SuppressWarnings("unchecked")
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
