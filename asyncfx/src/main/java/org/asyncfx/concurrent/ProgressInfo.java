/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

public class ProgressInfo {

    private final AbstractFuture future;
    private volatile boolean cancellationRequested;
    private List<ProgressListener> progressListeners;
    private double currentProgress;

    ProgressInfo(AbstractFuture future) {
        this.future = future;
    }

    ProgressInfo(AbstractFuture future, boolean cancellationRequested) {
        this.future = future;
        this.cancellationRequested = cancellationRequested;
    }

    public boolean isCancellationRequested() {
        return cancellationRequested;
    }

    public void throwIfCancellationRequested() {
        if (cancellationRequested) {
            throw new CancellationException();
        }
    }

    public void setProgress(double progress) {
        future.notifyProgressListeners(progress < 0 ? 0 : (progress > 1 ? 1 : progress));
    }

    synchronized void addListener(ProgressListener listener) {
        if (progressListeners == null) {
            progressListeners = new ArrayList<>(1);
        }

        progressListeners.add(listener);
        listener.progressChanged(currentProgress);
    }

    synchronized void notifyListeners(double cumulativeProgress, int rank) {
        if (progressListeners != null) {
            double progress = cumulativeProgress / (double)rank;
            if (currentProgress != progress) {
                currentProgress = progress;

                for (ProgressListener listener : progressListeners) {
                    listener.progressChanged(progress);
                }
            }
        }
    }

    synchronized boolean cancel() {
        return !cancellationRequested && (cancellationRequested = true);
    }

}
