/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.google.common.util.concurrent.SettableFuture;
import com.intel.missioncontrol.concurrent.FluentFuture;
import com.intel.missioncontrol.concurrent.SynchronizationContext;
import gov.nasa.worldwind.javafx.WWNode;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WWSyncContext extends SynchronizationContext {

    private static Logger LOGGER = LogManager.getLogger(WWSyncContext.class);

    private static class RunNowData<T> {
        T result;
        Exception exception;
    }

    private final WWNode worldWindNode;

    public WWSyncContext(WWNode worldWindNode) {
        this.worldWindNode = worldWindNode;
        worldWindNode.accept(() -> SynchronizationContext.setSynchronizationContext(this));
    }

    @Override
    public void run(Runnable runnable) {
        worldWindNode.acceptAndWait(runnable);
    }

    @Override
    public <T> T run(Callable<T> callable) {
        final var data = new RunNowData<T>();

        worldWindNode.acceptAndWait(
            () -> {
                try {
                    data.result = callable.call();
                } catch (Exception e) {
                    data.exception = e;
                }
            });

        if (data.exception != null) {
            throw new RuntimeException(data.exception);
        }

        return data.result;
    }

    @Override
    public FluentFuture<Void> post(Runnable runnable) {
        var future = SettableFuture.<Void>create();

        worldWindNode.accept(
            () -> {
                try {
                    runnable.run();
                    future.set(null);
                } catch (Exception e) {
                    future.setException(e);
                }
            });

        return FluentFuture.from(future);
    }

    @Override
    public <T> FluentFuture<T> post(Callable<T> callable) {
        var future = SettableFuture.<T>create();

        worldWindNode.accept(
            () -> {
                try {
                    future.set(callable.call());
                } catch (Exception e) {
                    future.setException(e);
                }
            });

        return FluentFuture.from(future);
    }

    @Override
    public FluentFuture<Void> dispatch(Runnable runnable) {
        if (hasAccess()) {
            try {
                runnable.run();
                return FluentFuture.fromResult(null);
            } catch (Exception e) {
                LOGGER.debug("Exception in async execution:", e);
                return FluentFuture.fromThrowable(e);
            }
        } else {
            return post(runnable);
        }
    }

    @Override
    public <V> FluentFuture<V> dispatch(Callable<V> callable) {
        if (hasAccess()) {
            try {
                return FluentFuture.fromResult(callable.call());
            } catch (Exception e) {
                LOGGER.debug("Exception in async execution:", e);
                return FluentFuture.fromThrowable(e);
            }
        } else {
            return post(callable);
        }
    }

    @Override
    public boolean hasAccess() {
        return Thread.currentThread() == worldWindNode.getRenderThread();
    }

    @Override
    public String toString() {
        return "WWNode render thread";
    }
}
