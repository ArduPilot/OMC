/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import java.util.concurrent.Callable;

/**
 * {@link SynchronizationRoot} is a wrapper for {@link SynchronizationContext} that allows the wrapped context to be
 * changed at runtime.
 */
public class SynchronizationRoot extends SynchronizationContext {

    private final Object mutex = new Object();

    private SynchronizationContext synchronizationContext = SynchronizationContext.getCurrent();

    @SuppressWarnings("unchecked")
    public void setContext(SynchronizationContext synchronizationContext) {
        synchronized (mutex) {
            this.synchronizationContext = synchronizationContext;
        }
    }

    @Override
    public void run(Runnable runnable) {
        synchronized (mutex) {
            synchronizationContext.run(runnable);
        }
    }

    @Override
    public <T> T run(Callable<T> callable) {
        synchronized (mutex) {
            return synchronizationContext.run(callable);
        }
    }

    @Override
    public FluentFuture<Void> post(Runnable runnable) {
        synchronized (mutex) {
            return synchronizationContext.post(runnable);
        }
    }

    @Override
    public <V> FluentFuture<V> post(Callable<V> runnable) {
        synchronized (mutex) {
            return synchronizationContext.post(runnable);
        }
    }

    @Override
    public FluentFuture<Void> dispatch(Runnable runnable) {
        synchronized (mutex) {
            return synchronizationContext.dispatch(runnable);
        }
    }

    @Override
    public <V> FluentFuture<V> dispatch(Callable<V> callable) {
        synchronized (mutex) {
            return synchronizationContext.dispatch(callable);
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
