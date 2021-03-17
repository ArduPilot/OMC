/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.value;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.ListenerWrapper;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class AsyncSubChangeListenerWrapper implements SubChangeListener, ListenerWrapper<SubChangeListener> {

    public static SubChangeListener wrap(SubChangeListener listener, Executor executor) {
        if (executor == MoreExecutors.directExecutor()) {
            return listener;
        }

        return new AsyncSubChangeListenerWrapper(listener, executor);
    }

    private final SubChangeListener listener;
    private final Executor executor;

    public AsyncSubChangeListenerWrapper(SubChangeListener listener, Executor executor) {
        this.listener = listener;
        this.executor = executor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SubChangeListener getWrappedListener() {
        SubChangeListener listener = this.listener;
        while (listener instanceof ListenerWrapper) {
            listener = ((ListenerWrapper<SubChangeListener>)listener).getWrappedListener();
        }

        return listener;
    }

    @Override
    public void changed(
            final ObservableValue<?> observable, final Object oldValue, final Object newValue, boolean subChange) {
        executor.execute(() -> listener.changed(observable, oldValue, newValue, subChange));
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
