/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import javafx.geometry.Orientation;
import javafx.scene.layout.Region;

public class AspectRegion extends Region {
    private final double ar = 1.3;
    public AspectRegion() {
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    protected double computeMinWidth(double height) {
        return 160;
    }

    @Override
    protected double computeMinHeight(double width) {
        return 90;
    }

    @Override
    protected double computeMaxWidth(double height) {
        return super.computeMaxWidth(height);
    }

    @Override
    protected double computeMaxHeight(double width) {
        return super.computeMaxHeight(width);
    }

    @Override
    protected double computePrefWidth(double height) {
        System.out.println("I'm asked for prefWidth(" + height + ")");
        return 400;
    }

    @Override
    protected double computePrefHeight(double width) {
        System.out.println("I'm asked for prefHeight(" + width + ")");
        return width/ar;
    }

    @Override
    public void resize(double width, double height) {
        System.out.println("I should resize?" + " " + width + "x" + height);

        //setPrefSize(width, width/ar);
        setPrefHeight(width/ar);

        super.resize(width, height);

    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }
}
