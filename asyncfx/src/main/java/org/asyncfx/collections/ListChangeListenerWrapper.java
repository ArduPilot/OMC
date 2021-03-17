/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import javafx.collections.ListChangeListener;
import org.asyncfx.PublishSource;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public final class ListChangeListenerWrapper<E> implements ListChangeListener<E> {

    public static <T> ListChangeListener<T> wrap(ListChangeListener<T> listener, Executor executor) {
        if (executor == MoreExecutors.directExecutor()) {
            return listener;
        }

        return new ListChangeListenerWrapper<>(listener, executor);
    }

    private final ListChangeListener<E> listener;
    private final Executor executor;

    ListChangeListenerWrapper(ListChangeListener<E> listener, Executor executor) {
        this.listener = listener;
        this.executor = executor;
    }

    @Override
    public void onChanged(Change<? extends E> c) {
        executor.execute(() -> listener.onChanged(c));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ListChangeListenerWrapper) {
            return listener.equals(((ListChangeListenerWrapper)obj).listener);
        }

        return listener.equals(obj);
    }

}
