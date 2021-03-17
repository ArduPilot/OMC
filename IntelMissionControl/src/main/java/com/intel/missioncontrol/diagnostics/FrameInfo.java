/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.diagnostics;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;

public class FrameInfo {

    private static final long RUN_SEPARATION_DURATION = 1000000000L;

    static class Builder {
        private boolean segmentStart;
        private long renderStartTimestamp;
        private long copyPixelsTimestamp;
        private long renderEndTimestamp;
        private double frameRate;
        private int drawCalls;
        private int readPixelsCount;
        private int renderedFrames;

        void startRendering() {
            this.renderStartTimestamp = System.nanoTime();
        }

        void copyPixels() {
            this.copyPixelsTimestamp = System.nanoTime();
        }

        void endRendering() {
            this.renderEndTimestamp = System.nanoTime();
        }

        void setFrameRate(double frameRate) {
            this.frameRate = frameRate;
        }

        void setDrawCalls(int drawCalls) {
            this.drawCalls = drawCalls;
        }

        void setReadPixelsCount(int readPixelsCount) {
            this.readPixelsCount = readPixelsCount;
        }

        void setRenderedFrames(int renderedFrames) {
            this.renderedFrames = renderedFrames;
        }

        FrameInfo getFrameInfo() {
            boolean segmentStart = this.segmentStart;
            this.segmentStart = false;
            long frameDuration = System.nanoTime() - renderStartTimestamp;
            long renderDuration = renderEndTimestamp - renderStartTimestamp;
            double renderFraction = (double)(renderDuration / 1000) / (double)(frameDuration / 1000);

            if (frameDuration > RUN_SEPARATION_DURATION && renderFraction < 0.5) {
                frameDuration = renderEndTimestamp - renderStartTimestamp;
                this.segmentStart = true;
            }

            var result =
                new FrameInfo(
                    segmentStart,
                    renderStartTimestamp,
                    copyPixelsTimestamp,
                    renderEndTimestamp,
                    frameDuration,
                    frameRate,
                    drawCalls,
                    readPixelsCount,
                    renderedFrames);
            frameRate = drawCalls = readPixelsCount = 0;
            renderStartTimestamp = renderEndTimestamp = copyPixelsTimestamp = 0;
            return result;
        }
    }

    private boolean segmentStart;
    private final long renderStartTimestamp;
    private final long renderEndTimestamp;
    private final long copyPixelsTimestamp;
    private final long frameDuration;
    private final double frameRate;
    private final int drawCalls;
    private final int readPixelsCount;
    private final int renderedFrames;
    private final List<Pair<String, String>> additionalData = new ArrayList<>();

    private FrameInfo(
            boolean segmentStart,
            long renderStartTimestamp,
            long copyPixelsTimestamp,
            long renderEndTimestamp,
            long frameDuration,
            double frameRate,
            int drawCalls,
            int readPixelsCount,
            int renderedFrames) {
        this.segmentStart = segmentStart;
        this.renderStartTimestamp = renderStartTimestamp;
        this.renderEndTimestamp = renderEndTimestamp;
        this.copyPixelsTimestamp = copyPixelsTimestamp;
        this.frameDuration = frameDuration;
        this.frameRate = frameRate;
        this.drawCalls = drawCalls;
        this.readPixelsCount = readPixelsCount;
        this.renderedFrames = renderedFrames;
    }

    public boolean isSegmentStart() {
        return segmentStart;
    }

    public double getFrameRate() {
        return frameRate;
    }

    public int getDrawCalls() {
        return drawCalls;
    }

    public int getReadPixelsCount() {
        return readPixelsCount;
    }

    public int getRenderedFrames() {
        return renderedFrames;
    }

    public long getRenderStartTimestamp() {
        return renderStartTimestamp;
    }

    public long getRenderEndTimestamp() {
        return renderEndTimestamp;
    }

    public long getFrameDurationNanos() {
        return frameDuration;
    }

    public long getTotalDurationNanos() {
        return copyPixelsTimestamp - renderStartTimestamp;
    }

    public long getRenderDurationNanos() {
        return renderEndTimestamp - renderStartTimestamp;
    }

    public long getCopyPixelsDurationNanos() {
        return renderEndTimestamp - copyPixelsTimestamp;
    }

    public int getFrameDurationMillis() {
        return (int)(getFrameDurationNanos() / 1000000);
    }

    public int getTotalDurationMillis() {
        return (int)(getTotalDurationNanos() / 1000000);
    }

    public int getRenderDurationMillis() {
        return (int)(getRenderDurationNanos() / 1000000);
    }

    public int getCopyPixelsDurationMillis() {
        return (int)(getCopyPixelsDurationNanos() / 1000000);
    }

    public List<Pair<String, String>> getAdditionalData() {
        return additionalData;
    }

}
