/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import java.util.ArrayList;
import java.util.List;

public final class CancellationTokenSource implements AutoCloseable {

    private final Object mutex = new Object();
    private final boolean cancellable;
    private volatile boolean cancellationRequested;
    private ResetEvent cancelledEvent;
    private Runnable singleListener;
    private List<Runnable> listeners;

    public CancellationTokenSource() {
        this(true);
    }

    public CancellationTokenSource(boolean isCancellable) {
        this.cancellable = isCancellable;
    }

    public CancellationToken createToken() {
        return new CancellationToken(this);
    }

    @Override
    public void close() {
        synchronized (mutex) {
            if (!cancellable || cancellationRequested) {
                return;
            }

            cancellationRequested = true;

            if (cancelledEvent != null) {
                cancelledEvent.set();
            }

            if (singleListener != null) {
                singleListener.run();
            } else if (listeners != null) {
                for (var listener : listeners) {
                    listener.run();
                }
            }
        }
    }

    boolean isCancellable() {
        return cancellable;
    }

    boolean isCancellationRequested() {
        return cancellationRequested;
    }

    ResetEvent getCancelledEvent() {
        if (!cancellable) {
            throw new RuntimeException("This cancellation token is not cancellable.");
        }

        synchronized (mutex) {
            if (cancelledEvent == null) {
                cancelledEvent = new ResetEvent();
            }

            return cancelledEvent;
        }
    }

    void addListener(Runnable runnable) {
        synchronized (mutex) {
            if (cancellationRequested) {
                runnable.run();
            } else {
                if (listeners != null) {
                    listeners.add(runnable);
                } else if (singleListener != null) {
                    listeners = new ArrayList<>();
                    listeners.add(singleListener);
                    listeners.add(runnable);
                    singleListener = null;
                } else {
                    singleListener = runnable;
                }
            }
        }
    }

}
