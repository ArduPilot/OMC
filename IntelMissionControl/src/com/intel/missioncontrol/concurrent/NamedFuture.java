/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import com.google.common.util.concurrent.ListenableFuture;

public class NamedFuture<V> extends FluentFuture<V> {

    private final String name;

    private NamedFuture(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static <V> NamedFuture<V> from(ListenableFuture<V> future, String name) {
        if (future instanceof NamedFuture) {
            return (NamedFuture<V>)future;
        }

        NamedFuture<V> namedFuture = new NamedFuture<>(name);
        namedFuture.initialize(future);
        return namedFuture;
    }

}
