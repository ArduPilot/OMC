/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import java.util.concurrent.Executor;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.AsyncObservable;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public interface AsyncObservableList<E> extends ObservableList<E>, AsyncCollection<E>, AsyncObservable {

    @Override
    LockedList<E> lock();

    void addListener(ListChangeListener<? super E> listener, Executor executor);

}
