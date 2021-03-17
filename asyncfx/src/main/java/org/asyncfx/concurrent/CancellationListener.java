/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

public interface CancellationListener {

    void cancel(boolean mayInterruptIfRunning);

}
