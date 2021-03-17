/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;

public interface ListenableExecutor extends Executor {

    ListenableFuture<Void> executeListen(Runnable command);

}
