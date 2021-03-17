/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.value;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class ChangeListenerWrapper<T> implements ChangeListener<T> {

    public static <T> ChangeListener<T> wrap(ChangeListener<T> listener, Executor executor) {
        if (executor == MoreExecutors.directExecutor()) {
            return listener;
        }

        return new ChangeListenerWrapper<>(listener, executor);
    }

    private final ChangeListener<T> listener;
    private final Executor executor;

    public ChangeListenerWrapper(ChangeListener<T> listener, Executor executor) {
        this.listener = listener;
        this.executor = executor;
    }

    @Override
    public void changed(final ObservableValue<? extends T> observable, final T oldValue, final T newValue) {
        executor.execute(() -> listener.changed(observable, oldValue, newValue));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ChangeListenerWrapper) {
            return listener.equals(((ChangeListenerWrapper)obj).listener);
        }

        return listener.equals(obj);
    }

}
