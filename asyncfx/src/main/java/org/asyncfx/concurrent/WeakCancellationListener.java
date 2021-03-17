/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.lang.ref.WeakReference;

public class WeakCancellationListener implements CancellationListener {

    private final WeakReference<CancellationListener> listener;

    public WeakCancellationListener(CancellationListener listener) {
        this.listener = new WeakReference<>(listener);
    }

    @Override
    public void cancel(boolean mayInterruptIfRunning) {
        CancellationListener listener = this.listener.get();
        if (listener != null) {
            listener.cancel(mayInterruptIfRunning);
        }
    }

    boolean wasGarbageCollected() {
        return listener.get() == null;
    }

}
