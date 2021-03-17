/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.asyncfx.PublishSource;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public final class AsyncInvalidationListenerWrapper
        implements InvalidationListener, ListenerWrapper<InvalidationListener> {

    public static InvalidationListener wrap(InvalidationListener listener, Executor executor) {
        if (executor == MoreExecutors.directExecutor()) {
            return listener;
        }

        return new AsyncInvalidationListenerWrapper(listener, executor);
    }

    private final InvalidationListener listener;
    private final Executor executor;

    public AsyncInvalidationListenerWrapper(InvalidationListener listener, Executor executor) {
        this.listener = listener;
        this.executor = executor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public InvalidationListener getWrappedListener() {
        InvalidationListener listener = this.listener;
        while (listener instanceof ListenerWrapper) {
            listener = ((ListenerWrapper<InvalidationListener>)listener).getWrappedListener();
        }

        return listener;
    }

    @Override
    public void invalidated(final Observable observable) {
        executor.execute(() -> listener.invalidated(observable));
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
