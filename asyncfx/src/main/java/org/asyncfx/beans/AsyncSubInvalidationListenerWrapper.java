/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import javafx.beans.Observable;
import org.asyncfx.PublishSource;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class AsyncSubInvalidationListenerWrapper
        implements SubInvalidationListener, ListenerWrapper<SubInvalidationListener> {

    public static SubInvalidationListener wrap(SubInvalidationListener listener, Executor executor) {
        if (executor == MoreExecutors.directExecutor()) {
            return listener;
        }

        return new AsyncSubInvalidationListenerWrapper(listener, executor);
    }

    private final SubInvalidationListener listener;
    private final Executor executor;

    public AsyncSubInvalidationListenerWrapper(SubInvalidationListener listener, Executor executor) {
        this.listener = listener;
        this.executor = executor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SubInvalidationListener getWrappedListener() {
        SubInvalidationListener listener = this.listener;
        while (listener instanceof ListenerWrapper) {
            listener = ((ListenerWrapper<SubInvalidationListener>)listener).getWrappedListener();
        }

        return listener;
    }

    @Override
    public void invalidated(final Observable observable, boolean subInvalidation) {
        executor.execute(() -> listener.invalidated(observable, subInvalidation));
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
