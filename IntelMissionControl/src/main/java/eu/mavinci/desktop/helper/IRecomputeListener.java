/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import eu.mavinci.core.listeners.IListener;
import eu.mavinci.core.listeners.IListener;

public interface IRecomputeListener extends IListener {

    /**
     * this notification will be called after recomputation is done INSIDE THE RECOMPUTE THREAD
     *
     * <p>please take care of SwingThread Changes /Deadlocks / Concurrect modification yourself!
     *
     * @param recomputer
     * @param anotherRecomputeIsWaiting
     */
    public void recomputeReady(Recomputer recomputer, boolean anotherRecomputeIsWaiting, long runNo);

}
