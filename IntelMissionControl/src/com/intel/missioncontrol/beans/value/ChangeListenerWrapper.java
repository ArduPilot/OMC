/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.beans.value;

import com.google.common.util.concurrent.MoreExecutors;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.PropertyHelper;
import com.intel.missioncontrol.concurrent.ListenableExecutors;
import java.util.concurrent.Executor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public class ChangeListenerWrapper<T> implements ChangeListener<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeListenerWrapper.class);
    private static final String PACKAGE_NAME;

    static {
        var className = ChangeListenerWrapper.class.getName();
        PACKAGE_NAME =
            className.substring(0, className.length() - ChangeListenerWrapper.class.getSimpleName().length() - 1);
    }

    public static <T> ChangeListener<T> wrap(ChangeListener<T> listener, Executor executor) {
        if (executor == MoreExecutors.directExecutor() || executor == ListenableExecutors.directExecutor()) {
            return listener;
        }

        return new ChangeListenerWrapper<>(listener, executor);
    }

    private final ChangeListener<T> listener;
    private final Executor executor;
    private int expiredTimeouts;

    public ChangeListenerWrapper(ChangeListener<T> listener, Executor executor) {
        this.listener = listener;
        this.executor = executor;
    }

    @Override
    public void changed(final ObservableValue<? extends T> observable, final T oldValue, final T newValue) {
        executor.execute(
            () -> {
                long startTime = System.nanoTime();
                listener.changed(observable, oldValue, newValue);
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

        if (obj instanceof ChangeListenerWrapper) {
            return listener.equals(((ChangeListenerWrapper)obj).listener);
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
            "ChangeListener exceeded maximum execution time [max: "
                + max
                + " ms; elapsed: "
                + elapsed
                + " ms]\n\tat "
                + (stackFrame == null ? "<unknown>" : stackFrame));
    }

}
