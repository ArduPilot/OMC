/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

@FunctionalInterface
public interface AsyncListChangeListener<E> extends ListChangeListener<E> {

    abstract class AsyncChange<E> extends Change<E> {

        public AsyncChange(ObservableList<E> list) {
            super(list);
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
