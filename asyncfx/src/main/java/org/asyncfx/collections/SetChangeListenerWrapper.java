/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.collections;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import javafx.collections.SetChangeListener;
import org.asyncfx.PublishSource;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public final class SetChangeListenerWrapper<E> implements SetChangeListener<E> {

    public static <T> SetChangeListener<T> wrap(SetChangeListener<T> listener, Executor executor) {
        if (executor == MoreExecutors.directExecutor()) {
            return listener;
        }

        return new SetChangeListenerWrapper<>(listener, executor);
    }

    private final SetChangeListener<E> listener;
    private final Executor executor;

    SetChangeListenerWrapper(SetChangeListener<E> listener, Executor executor) {
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

        if (obj instanceof SetChangeListenerWrapper) {
            return listener.equals(((SetChangeListenerWrapper)obj).listener);
        }

        return listener.equals(obj);
    }

}
