/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

/**
 * This waiting primitive can be used to wait for a signal. If the signal is set, it allows any number of threads to
 * pass until the signal is reset again.
 */
public class ResetEvent extends WaitHandle {

    public void reset() {
        sync.reset();
    }

    public void set() {
        sync.releaseShared(1);
    }

}
