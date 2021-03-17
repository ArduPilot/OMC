/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.function.Supplier;

/**
 * {@link SynchronizationRoot} is a wrapper for {@link SynchronizationContext} that allows the wrapped context to be
 * changed at runtime.
 */
public class SynchronizationRoot extends SynchronizationContext {

    private final Object mutex = new Object();

    private SynchronizationContext synchronizationContext = SynchronizationContext.getCurrent();

    public void setContext(SynchronizationContext synchronizationContext) {
        synchronized (mutex) {
            this.synchronizationContext = synchronizationContext;
        }
    }

    @Override
    public Future<Void> runLaterAsync(Runnable runnable) {
        synchronized (mutex) {
            return synchronizationContext.runLaterAsync(runnable);
        }
    }

    @Override
    public <V> Future<V> getLaterAsync(Supplier<V> supplier) {
        synchronized (mutex) {
            return synchronizationContext.getLaterAsync(supplier);
        }
    }

    @Override
    public Future<Void> runAsync(Runnable runnable) {
        synchronized (mutex) {
            return synchronizationContext.runAsync(runnable);
        }
    }

    @Override
    public <V> Future<V> getAsync(Supplier<V> supplier) {
        synchronized (mutex) {
            return synchronizationContext.getAsync(supplier);
        }
    }

    @Override
    public boolean hasAccess() {
        synchronized (mutex) {
            return synchronizationContext.hasAccess();
        }
    }

    @Override
    public String toString() {
        return synchronizationContext.toString();
    }

}
