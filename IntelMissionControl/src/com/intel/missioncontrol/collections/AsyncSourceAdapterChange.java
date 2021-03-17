/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import com.intel.missioncontrol.PublishSource;
import com.sun.javafx.collections.SourceAdapterChange;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class AsyncSourceAdapterChange<E> extends SourceAdapterChange<E> {

    public AsyncSourceAdapterChange(ObservableList<E> list, ListChangeListener.Change<? extends E> change) {
        super(list, change);
    }

    ObservableList<E> getListInternal() {
        return super.getList();
    }

    @Override
    public ObservableList<E> getList() {
        throw new UnsupportedOperationException("Cannot query the source list of an async change.");
    }

}
