/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.diagnostics;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

class StatisticsInfo {

    private int numFramesBelow16ms;
    private int numFramesBelow33ms;
    private int numFramesBelow66ms;

    private final IntegerProperty frameCount = new SimpleIntegerProperty();
    private final DoubleProperty percentFramesBelow16ms = new SimpleDoubleProperty();
    private final DoubleProperty percentFramesBelow33ms = new SimpleDoubleProperty();
    private final DoubleProperty percentFramesBelow66ms = new SimpleDoubleProperty();
    private final IntegerProperty maxFrameTimeMillis = new SimpleIntegerProperty();

    void reset() {
        numFramesBelow16ms = 0;
        numFramesBelow33ms = 0;
        numFramesBelow66ms = 0;
        percentFramesBelow16ms.set(0);
        percentFramesBelow33ms.set(0);
        percentFramesBelow66ms.set(0);
        frameCount.set(0);
        maxFrameTimeMillis.set(0);
    }

    void update(FrameInfo frame) {
        int frameCount = this.frameCount.get() + 1;
        this.frameCount.set(frameCount);

        long frameDurationNanos = frame.getFrameDurationNanos();
        int frameDurationMillis = (int)(frameDurationNanos / 1000000);

        if (frameDurationMillis > maxFrameTimeMillis.get()) {
            maxFrameTimeMillis.set(frameDurationMillis);
        }

        if (frameDurationMillis < 16) {
            ++numFramesBelow16ms;
        }

        if (frameDurationMillis < 33) {
            ++numFramesBelow33ms;
        }

        if (frameDurationMillis < 66) {
            ++numFramesBelow66ms;
        }

        percentFramesBelow16ms.set((double)numFramesBelow16ms / frameCount);
        percentFramesBelow33ms.set((double)numFramesBelow33ms / frameCount);
        percentFramesBelow66ms.set((double)numFramesBelow66ms / frameCount);
    }

    public ReadOnlyIntegerProperty frameCountProperty() {
        return frameCount;
    }

    public ReadOnlyDoubleProperty percentFramesBelow16msProperty() {
        return percentFramesBelow16ms;
    }

    public ReadOnlyDoubleProperty percentFramesBelow33msProperty() {
        return percentFramesBelow33ms;
    }

    public ReadOnlyDoubleProperty percentFramesBelow66msProperty() {
        return percentFramesBelow66ms;
    }

    public IntegerProperty maxFrameTimeMillisProperty() {
        return maxFrameTimeMillis;
    }

}
