/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

public class DragContext {
    private double startWidth;
    private double startHeight;
    private double startMouseX;
    private double startMouseY;

    private double startStageWidth = 0;
    private double startStageHeight = 0;

    private double minWidthDiff;
    private double minHeightDiff;

    public class Dimension {
        public final double width;
        public final double height;
        public final double stageWidth;
        public final double stageHeight;

         Dimension(double width, double height, double stageWidth, double stageHeight) {
            this.width = width;
            this.height = height;
            this.stageWidth = stageWidth;
            this.stageHeight = stageHeight;
        }
    }

    public DragContext(double startWidth, double startHeight, double startMouseX, double startMouseY) {
        this.startWidth = startWidth;
        this.startHeight = startHeight;
        this.startMouseX = startMouseX;
        this.startMouseY = startMouseY;
        this.minWidthDiff = -startWidth;
        this.minHeightDiff = -startHeight;
    }

    public void setInitialStageDimension(double startStageWidth, double startStageHeight) {
        this.startStageWidth = startStageWidth;
        this.startStageHeight = startStageHeight;
    }

    public void setMinimalDimension(double minWidth, double minHeight) {
        this.minWidthDiff = minWidth - this.startWidth;
        this.minHeightDiff = minHeight - this.startHeight;
    }

    public Dimension getNewDimension(double currentMouseX, double currentMouseY) {
        double newXDiff = currentMouseX - this.startMouseX;
        double newYDiff = currentMouseY - this.startMouseY;

        newXDiff = Math.max(newXDiff, this.minWidthDiff);
        newYDiff = Math.max(newYDiff, this.minHeightDiff);


        return new Dimension(
                startWidth + newXDiff,
                startHeight + newYDiff,
                startStageWidth + newXDiff,
                startStageHeight + newYDiff);
    }
}
