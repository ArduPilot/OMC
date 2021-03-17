/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.drone.connection.mavlink;

import java.time.Duration;
import org.asyncfx.concurrent.CancellationSource;
import org.asyncfx.concurrent.Dispatcher;

class RefreshableTimeout {

    private Duration timeout;
    private Runnable onTimeout;
    private CancellationSource cancellationSource;
    private CancellationSource externalCancellationSource;
    private final Object lock = new Object();

    RefreshableTimeout(Runnable onTimeout, Duration timeout, CancellationSource externalCancellationSource) {
        this.timeout = timeout;
        this.onTimeout = onTimeout;
        this.externalCancellationSource = externalCancellationSource;
        if (externalCancellationSource != null) {
            externalCancellationSource.addListener(
                mayInterruptIfRunning -> {
                    synchronized (lock) {
                        if (cancellationSource != null) {
                            cancellationSource.cancel();
                        }
                    }
                });
        }
    }

    void refreshOrStart() {
        if (externalCancellationSource != null && externalCancellationSource.isCancellationRequested()) {
            return;
        }

        synchronized (lock) {
            if (cancellationSource != null) {
                // refresh item: cancel old timer
                cancellationSource.cancel();
            }

            cancellationSource = new CancellationSource();

            Dispatcher.background().runLaterAsync(onTimeout, timeout, cancellationSource);
        }
    }

}
