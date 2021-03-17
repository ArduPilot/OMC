/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import javafx.beans.value.ObservableValue;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.value.SubChangeListener;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
class SubChangeListenerWrapper implements SubChangeListener {

    public static SubChangeListener wrap(SubChangeListener listener, Executor executor) {
        if (executor == MoreExecutors.directExecutor()) {
            return listener;
        }

        return new SubChangeListenerWrapper(listener, executor);
    }

    private final SubChangeListener listener;
    private final Executor executor;

    public SubChangeListenerWrapper(SubChangeListener listener, Executor executor) {
        this.listener = listener;
        this.executor = executor;
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

        if (obj instanceof SubChangeListenerWrapper) {
            return listener.equals(((SubChangeListenerWrapper)obj).listener);
        }

        return listener.equals(obj);
    }

}
