/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package playground.liveview;

import javafx.geometry.Orientation;
import javafx.scene.layout.Pane;

public class WrapperPane extends Pane {
    public WrapperPane() {
        setMaxHeight(400);
        setMaxWidth(900);
        setPrefSize(500, 250);
        setMinSize(160, 90);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.VERTICAL;
    }

    @Override
    protected double computePrefHeight(double width) {
        System.out.println("cPH(" + width + ")");
        if (width > 0) return width/2.2;
        return getPrefHeight();
    }



    @Override
    public void resize(double width, double height) {
        setWidth(width);
        setHeight(height);
    }
}
