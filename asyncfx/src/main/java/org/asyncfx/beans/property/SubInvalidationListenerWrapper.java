/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.beans.property;

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import javafx.beans.Observable;
import org.asyncfx.PublishSource;
import org.asyncfx.beans.SubInvalidationListener;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class SubInvalidationListenerWrapper implements SubInvalidationListener {

    public static SubInvalidationListener wrap(SubInvalidationListener listener, Executor executor) {
        if (executor == MoreExecutors.directExecutor()) {
            return listener;
        }

        return new SubInvalidationListenerWrapper(listener, executor);
    }

    private final SubInvalidationListener listener;
    private final Executor executor;

    public SubInvalidationListenerWrapper(SubInvalidationListener listener, Executor executor) {
        this.listener = listener;
        this.executor = executor;
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

        if (obj instanceof SubInvalidationListenerWrapper) {
            return listener.equals(((SubInvalidationListenerWrapper)obj).listener);
        }

        return listener.equals(obj);
    }

}
