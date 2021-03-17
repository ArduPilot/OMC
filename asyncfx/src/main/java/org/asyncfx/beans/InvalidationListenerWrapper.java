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
public final class InvalidationListenerWrapper implements InvalidationListener {

    public static InvalidationListener wrap(InvalidationListener listener, Executor executor) {
        if (executor == MoreExecutors.directExecutor()) {
            return listener;
        }

        return new InvalidationListenerWrapper(listener, executor);
    }

    private final InvalidationListener listener;
    private final Executor executor;

    public InvalidationListenerWrapper(InvalidationListener listener, Executor executor) {
        this.listener = listener;
        this.executor = executor;
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

        if (obj instanceof InvalidationListenerWrapper) {
            return listener.equals(((InvalidationListenerWrapper)obj).listener);
        }

        return listener.equals(obj);
    }

}
