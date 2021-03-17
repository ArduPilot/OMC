/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.concurrent;

@FunctionalInterface
public interface CancellableRunnable {
    void run(CancellationToken cancellationToken);
}
