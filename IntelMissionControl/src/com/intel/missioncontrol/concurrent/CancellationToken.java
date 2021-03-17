/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

public final class CancellationToken {

    private final CancellationTokenSource cancellationTokenSource;

    CancellationToken(CancellationTokenSource cancellationTokenSource) {
        this.cancellationTokenSource = cancellationTokenSource;
    }

    public boolean isCancellationRequested() {
        return cancellationTokenSource.isCancellationRequested();
    }

    public boolean isCancellable() {
        return cancellationTokenSource.isCancellable();
    }

    public ResetEvent getCancelledEvent() {
        return cancellationTokenSource.getCancelledEvent();
    }

    public void addListener(Runnable runnable) {
        cancellationTokenSource.addListener(runnable);
    }

}
