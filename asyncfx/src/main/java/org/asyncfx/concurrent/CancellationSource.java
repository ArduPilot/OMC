/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class CancellationSource {

    static final CancellationSource DEFAULT =
        new CancellationSource() {
            public synchronized void addListener(CancellationListener cancellationListener) {}
        };

    private List<CancellationListener> cancellationListeners;
    private List<WeakReference<Future>> futures;
    private boolean cancellationRequested;
    private boolean mayInterruptIfRunning;

    public boolean isCancellationRequested() {
        return cancellationRequested;
    }

    public void cancel() {
        cancel(false);
    }

    public synchronized void cancel(boolean mayInterruptIfRunning) {
        if (cancellationRequested) {
            return;
        }

        cancellationRequested = true;
        this.mayInterruptIfRunning = mayInterruptIfRunning;

        if (cancellationListeners != null) {
            ListIterator<CancellationListener> it = cancellationListeners.listIterator();
            while (it.hasNext()) {
                CancellationListener listener = it.next();
                listener.cancel(mayInterruptIfRunning);

                if (listener instanceof WeakCancellationListener
                        && ((WeakCancellationListener)listener).wasGarbageCollected()) {
                    it.remove();
                }
            }

            cancellationListeners = null;
        }

        if (futures != null) {
            for (WeakReference<Future> ref : futures) {
                Future future = ref.get();
                if (future != null) {
                    future.cancel(mayInterruptIfRunning);
                }
            }

            futures = null;
        }
    }

    public synchronized void addListener(CancellationListener cancellationListener) {
        if (cancellationRequested) {
            cancellationListener.cancel(mayInterruptIfRunning);
        } else {
            if (cancellationListeners == null) {
                cancellationListeners = new ArrayList<>(1);
            }

            cancellationListeners.add(cancellationListener);
        }
    }

    synchronized void registerFuture(Future future) {
        if (futures == null) {
            futures = new ArrayList<>(1);
        }

        futures.add(new WeakReference<>(future));

        if (futures.size() % 10 == 0) {
            futures.removeIf(ref -> ref.get() == null);
        }
    }

}
