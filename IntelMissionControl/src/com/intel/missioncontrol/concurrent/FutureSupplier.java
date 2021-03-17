/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import com.google.common.util.concurrent.ListenableFuture;

public interface FutureSupplier<TFuture, TContinuation> {
    ListenableFuture<TContinuation> apply(FluentFuture<TFuture> value);
}
