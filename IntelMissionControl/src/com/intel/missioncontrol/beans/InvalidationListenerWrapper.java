/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans;

import com.google.common.util.concurrent.MoreExecutors;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.PropertyHelper;
import com.intel.missioncontrol.concurrent.ListenableExecutors;
import java.util.concurrent.Executor;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public final class InvalidationListenerWrapper implements InvalidationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvalidationListenerWrapper.class);
    private static final String PACKAGE_NAME;

    static {
        var className = InvalidationListenerWrapper.class.getName();
        PACKAGE_NAME =
            className.substring(0, className.length() - InvalidationListenerWrapper.class.getSimpleName().length() - 1);
    }

    public static InvalidationListener wrap(InvalidationListener listener, Executor executor) {
        if (executor == MoreExecutors.directExecutor() || executor == ListenableExecutors.directExecutor()) {
            return listener;
        }

        return new InvalidationListenerWrapper(listener, executor);
    }

    private final InvalidationListener listener;
    private final Executor executor;
    private int expiredTimeouts;

    public InvalidationListenerWrapper(InvalidationListener listener, Executor executor) {
        this.listener = listener;
        this.executor = executor;
    }

    @Override
    public void invalidated(final Observable observable) {
        executor.execute(
            () -> {
                long startTime = System.nanoTime();
                listener.invalidated(observable);
                long duration = (System.nanoTime() - startTime) / 1000000;
                if (duration > PropertyHelper.getEventHandlerTimeout()) {
                    handleTimeout(PropertyHelper.getEventHandlerTimeout(), duration);
                }
            });
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

    int getExpiredTimeouts() {
        return expiredTimeouts;
    }

    private void handleTimeout(long max, long elapsed) {
        ++expiredTimeouts;

        String stackFrame = null;
        for (var element : new Exception().getStackTrace()) {
            if (!element.getClassName().startsWith(PACKAGE_NAME)) {
                stackFrame = element.toString();
                break;
            }
        }

        LOGGER.warn(
            "InvalidationListener exceeded maximum execution time [max: "
                + max
                + " ms; elapsed: "
                + elapsed
                + " ms]\n\tat "
                + (stackFrame == null ? "<unknown>" : stackFrame));
    }

}
