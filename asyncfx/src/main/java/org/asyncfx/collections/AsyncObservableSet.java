/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.concurrent.Executor;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AsyncObservable;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncObservableSet<E> extends ObservableSet<E>, AsyncCollection<E>, AsyncObservable {

    @Override
    LockedSet<E> lock();

    void addListener(SetChangeListener<? super E> listener, Executor executor);

}
