/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import com.google.common.util.concurrent.MoreExecutors;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.beans.property.PropertyHelper;
import com.intel.missioncontrol.concurrent.ListenableExecutors;
import java.util.concurrent.Executor;
import javafx.collections.SetChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PublishSource(module = "openjfx", licenses = "intel-gpl-classpath-exception")
public final class SetChangeListenerWrapper<E> implements SetChangeListener<E> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SetChangeListenerWrapper.class);
    private static final String PACKAGE_NAME;

    static {
        var className = SetChangeListenerWrapper.class.getName();
        PACKAGE_NAME =
            className.substring(0, className.length() - SetChangeListenerWrapper.class.getSimpleName().length() - 1);
    }

    public static <T> SetChangeListener<T> wrap(SetChangeListener<T> listener, Executor executor) {
        if (executor == MoreExecutors.directExecutor() || executor == ListenableExecutors.directExecutor()) {
            return listener;
        }

        return new SetChangeListenerWrapper<>(listener, executor);
    }

    private final SetChangeListener<E> listener;
    private final Executor executor;
    private int expiredTimeouts;

    public SetChangeListenerWrapper(SetChangeListener<E> listener, Executor executor) {
        this.listener = listener;
        this.executor = executor;
    }

    @Override
    public void onChanged(Change<? extends E> c) {
        executor.execute(
            () -> {
                long startTime = System.nanoTime();
                listener.onChanged(c);
                long duration = (System.nanoTime() - startTime) / 1000000;
                if (duration > PropertyHelper.getEventHandlerTimeout()) {
                    handleTimeout(PropertyHelper.getEventHandlerTimeout(), duration);
                }
            });
    }

    @Override
    public boolean equals(Object obj) {
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
            "SetChangeListener exceeded maximum execution time [max: "
                + max
                + " ms; elapsed: "
                + elapsed
                + " ms]\n\tat "
                + (stackFrame == null ? "<unknown>" : stackFrame));
    }
}
