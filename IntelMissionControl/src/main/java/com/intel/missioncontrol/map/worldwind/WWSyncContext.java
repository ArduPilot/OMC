/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.google.common.util.concurrent.SettableFuture;
import gov.nasa.worldwind.javafx.WWGLNode;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.asyncfx.concurrent.SynchronizationContext;

public class WWSyncContext extends SynchronizationContext {

    private static Logger LOGGER = LogManager.getLogger(WWSyncContext.class);

    private final WWGLNode worldWindNode;

    public WWSyncContext(WWGLNode worldWindNode) {
        this.worldWindNode = worldWindNode;
        worldWindNode.accept(() -> SynchronizationContext.setSynchronizationContext(this));
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable) {
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

        return Futures.fromListenableFuture(future);
    }

    @Override
    public <T> Future<T> getLaterAsync(Supplier<T> supplier) {
        var future = SettableFuture.<T>create();

        worldWindNode.accept(
            () -> {
                try {
                    future.set(supplier.get());
                } catch (Exception e) {
                    future.setException(e);
                }
            });

        return Futures.fromListenableFuture(future);
    }

    @Override
    public Future<Void> runAsync(Runnable runnable) {
        if (hasAccess()) {
            try {
                runnable.run();
                return Futures.successful(null);
            } catch (Exception e) {
                LOGGER.debug("Exception in async execution:", e);
                return Futures.failed(e);
            }
        } else {
            return runLaterAsync(runnable);
        }
    }

    @Override
    public <V> Future<V> getAsync(Supplier<V> supplier) {
        if (hasAccess()) {
            try {
                return Futures.successful(supplier.get());
            } catch (Exception e) {
                LOGGER.debug("Exception in async execution:", e);
                return Futures.failed(e);
            }
        } else {
            return getLaterAsync(supplier);
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
