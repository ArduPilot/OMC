/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.AsyncObservable;
import java.util.concurrent.Executor;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncObservableSet<E> extends ObservableSet<E>, AsyncCollection<E>, AsyncObservable {

    @Override
    LockedSet<E> lock();

    void addListener(SetChangeListener<? super E> listener, Executor executor);

}
