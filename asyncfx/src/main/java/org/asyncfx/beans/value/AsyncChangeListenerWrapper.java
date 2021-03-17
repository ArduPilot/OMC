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
import org.asyncfx.beans.ListenerWrapper;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class AsyncChangeListenerWrapper<T> implements ChangeListener<T>, ListenerWrapper<ChangeListener<T>> {

    public static <T> ChangeListener<T> wrap(ChangeListener<T> listener, Executor executor) {
        if (executor == MoreExecutors.directExecutor()) {
            return listener;
        }

        return new AsyncChangeListenerWrapper<>(listener, executor);
    }

    private final ChangeListener<T> listener;
    private final Executor executor;

    public AsyncChangeListenerWrapper(ChangeListener<T> listener, Executor executor) {
        this.listener = listener;
        this.executor = executor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChangeListener<T> getWrappedListener() {
        ChangeListener<T> listener = this.listener;
        while (listener instanceof ListenerWrapper) {
            listener = ((ListenerWrapper<ChangeListener<T>>)listener).getWrappedListener();
        }

        return listener;
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

        if (obj instanceof ListenerWrapper) {
            return listener.equals(((ListenerWrapper)obj).getWrappedListener());
        }

        return listener.equals(obj);
    }

}
