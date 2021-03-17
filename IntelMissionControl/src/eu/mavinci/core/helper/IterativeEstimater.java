/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.helper;

public class IterativeEstimater extends AMeanEstimater {

    public IterativeEstimater(int windowSize) {
        super(windowSize);
    }

    double currentValue = 0;

    public double getMean() {
        return currentValue;
    }

    public void setMean(double value) {
        currentValue = value;
    }

    public void pushValue(double newVal) {
        if (currentSize >= windowSize) {
            currentSize--;
        }

        currentValue = currentSize * currentValue + newVal;
        currentSize++;
        currentValue /= currentSize;
    }

    @Override
    public void reset() {
        currentSize = 0;
    }

}
