/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.List;
import javafx.collections.ListChangeListener;

@FunctionalInterface
public interface AsyncListChangeListener<E> extends ListChangeListener<E> {

    abstract class AsyncChange<E> extends Change<E> {

        public AsyncChange(AsyncObservableList<E> list) {
            super(list);
        }

        public AsyncObservableList<E> getList() {
            return (AsyncObservableList<E>)super.getList();
        }

        public abstract List<E> getUpdatedSubList();

    }

    void onChanged(AsyncChange<? extends E> c);

    @Override
    @SuppressWarnings("unchecked")
    default void onChanged(Change<? extends E> c) {
        if (c instanceof AsyncChange) {
            onChanged((AsyncChange<? extends E>)c);
        }
    }

}
