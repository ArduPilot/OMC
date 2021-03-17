/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.asyncfx.concurrent;

public interface ProgressListener {

    /**
     * Occurs when the progress of a future or a chain of futures has changed. The value ranges from 0..1 and reflects
     * the combined progress of the current future and all of its predecessors. This callback may occur on any thread.
     */
    void progressChanged(double progress);

}
