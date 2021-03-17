/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.project.property;

import java.util.function.Supplier;
import javafx.application.Platform;
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;
import org.asyncfx.concurrent.Futures;
import org.asyncfx.concurrent.SynchronizationContext;

public class PlatformSynchronizationContext extends SynchronizationContext {

    private static final PlatformSynchronizationContext INSTANCE = new PlatformSynchronizationContext();

    public static SynchronizationContext getInstance() {
        return INSTANCE;
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable) {
        return Dispatcher.platform().runLaterAsync(runnable);
    }

    @Override
    public <V> Future<V> getLaterAsync(Supplier<V> supplier) {
        return Dispatcher.platform().getLaterAsync(supplier);
    }

    @Override
    public Future<Void> runAsync(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            try {
                runnable.run();
                return Futures.successful(null);
            } catch (Throwable e) {
                return Futures.failed(e);
            }
        }

        return Dispatcher.platform().runLaterAsync(runnable);
    }

    @Override
    public <V> Future<V> getAsync(Supplier<V> supplier) {
        if (Platform.isFxApplicationThread()) {
            try {
                return Futures.successful(supplier.get());
            } catch (Throwable e) {
                return Futures.failed(e);
            }
        }

        return Dispatcher.background().getLaterAsync(supplier);
    }

    @Override
    public boolean hasAccess() {
        return Platform.isFxApplicationThread();
    }

    @Override
    public String toString() {
        return "JavaFX application thread";
    }

}
