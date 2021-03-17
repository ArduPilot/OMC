/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

class FutureFlags {

    static final int SUCCEEDED = 1;
    static final int FAILED = 1 << 2;
    static final int CANCELLED = 1 << 3;
    static final int DONE = SUCCEEDED | FAILED | CANCELLED;
    static final int RUNNING = 1 << 4;
    static final int ALREADY_EXECUTED = 1 << 5;
    static final int PENDING_CANCELLATION = 1 << 6;

}
