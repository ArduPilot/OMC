/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.AsyncObservable;
import java.util.concurrent.Executor;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncObservableList<E> extends ObservableList<E>, AsyncCollection<E>, AsyncObservable {

    @Override
    LockedList<E> lock();

    void addListener(ListChangeListener<? super E> listener, Executor executor);

}
