/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

/** FutureCompletionSource is the producer of a future that can be manually completed. */
public class FutureCompletionSource<V> {

    private final CompletableFuture<V> future;

    public FutureCompletionSource() {
        future =
            new CompletableFuture<>() {
                @Override
                boolean cancellationRequested(boolean mayInterruptIfRunning) {
                    FutureCompletionSource.this.cancellationRequested(mayInterruptIfRunning);
                    completeWithCancellation(getException());
                    return true;
                }
            };
    }

    public FutureCompletionSource(CancellationSource cancellationSource) {
        future =
            new CompletableFuture<>() {
                @Override
                boolean cancellationRequested(boolean mayInterruptIfRunning) {
                    FutureCompletionSource.this.cancellationRequested(mayInterruptIfRunning);
                    completeWithCancellation(getException());
                    return true;
                }
            };

        cancellationSource.registerFuture(future);
    }

    public Future<V> getFuture() {
        return future;
    }

    public void setProgress(double progress) {
        future.getProgressInfo().setProgress(progress);
    }

    public void setResult(V value) {
        future.completeWithResult(value);
    }

    public void setException(Throwable throwable) {
        future.completeWithException(throwable);
    }

    public void setCancelled() {
        future.completeWithCancellation(null);
    }

    public void setCancelled(AggregateException exception) {
        future.completeWithCancellation(exception);
    }

    protected void cancellationRequested(boolean mayInterruptIfRunning) {}

}
