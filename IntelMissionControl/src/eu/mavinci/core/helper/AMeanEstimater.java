/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

public abstract class AMeanEstimater {

    /**
     * Add new measurement value to estimation
     *
     * @param newVal
     */
    public abstract void pushValue(double newVal);

    /**
     * Get current estimation
     *
     * @return
     */
    public abstract double getMean();

    int windowSize;
    int currentSize = 0;

    public AMeanEstimater(int windowSize) {
        this.windowSize = windowSize;
    }

    public int getCurrentCount() {
        return currentSize;
    }

    public abstract void reset();
}
