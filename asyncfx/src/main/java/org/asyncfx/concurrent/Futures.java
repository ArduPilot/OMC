/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

import com.google.common.util.concurrent.ListenableFuture;

public class Futures {

    public static <V> Future<V> fromListenableFuture(ListenableFuture<V> future) {
        return DefaultFutureFactory.getInstance().fromListenableFuture(future);
    }

    /** Creates an immediately successful future. */
    public static <V> Future<V> successful(V result) {
        return DefaultFutureFactory.getInstance().successful(result);
    }

    /** Creates an immediately successful future. */
    public static Future<Void> successful() {
        return DefaultFutureFactory.getInstance().successful(null);
    }

    /** Creates an immediately failed future. */
    public static <V> Future<V> failed(Throwable throwable) {
        return DefaultFutureFactory.getInstance().failed(throwable);
    }

    /** Creates an immediately cancelled future. */
    public static <V> Future<V> cancelled() {
        return DefaultFutureFactory.getInstance().cancelled();
    }

    /** Creates a future that completes when all given futures have completed. */
    public static Future<Future<?>[]> whenAll(Future<?>... futures) {
        return DefaultFutureFactory.getInstance().whenAll(futures);
    }

}
