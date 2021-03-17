/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import com.google.common.util.concurrent.ListenableScheduledFuture;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FluentScheduledFuture<V> extends FluentFuture<V> implements ScheduledFuture<V> {

    @Override
    @SuppressWarnings("unchecked")
    public long getDelay(TimeUnit unit) {
        return ((ScheduledFuture<V>)getNestedFuture()).getDelay(unit);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(Delayed o) {
        return ((ScheduledFuture<V>)getNestedFuture()).compareTo(o);
    }

    /** Creates a FluentScheduledFuture by wrapping a ListenableScheduledFuture instance. */
    @SuppressWarnings("unchecked")
    public static <V> FluentScheduledFuture<V> from(ListenableScheduledFuture<V> future) {
        if (future instanceof FluentScheduledFuture) {
            return (FluentScheduledFuture<V>)future;
        }

        FluentScheduledFuture<V> fluentFuture = new FluentScheduledFuture<>();
        fluentFuture.initialize(future);
        return fluentFuture;
    }

}
