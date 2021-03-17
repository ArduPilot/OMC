/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.plane.listeners;

public interface IAirplaneListenerLogReplay extends IAirplaneListener {

    /**
     * Receive time that has been played back so far.
     *
     * @param secs
     * @param secsTotal
     */
    public void elapsedSimTime(double secs, double secsTotal);

    public void replayStopped(boolean stopped);

    public void replayPaused(boolean paused);

    public void replaySkipPhase(boolean isSkipping);

    public void replayFinished();

}
