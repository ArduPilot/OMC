/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.livevideo;

import javafx.geometry.Orientation;
import javafx.scene.image.ImageView;

public class WrappedImageView extends ImageView {
    private final double DEFAULT_ASPECT_RATIO = 16. / 9;
    private double aspectRatio = DEFAULT_ASPECT_RATIO;
    private double width = 0;
    private double height = 0;

    public WrappedImageView() {
        super();
        setPreserveRatio(false);
        this.imageProperty()
            .addListener(
                (obs, oldValue, newValue) -> {
                    if (newValue != null) setResolution((int)newValue.getWidth(), (int)newValue.getHeight());
                });
    }

    private void setResolution(int width, int height) {
        if (height == 0) {
            this.height = 0;
            this.width = 0;
            this.aspectRatio = DEFAULT_ASPECT_RATIO;
        } else {
            this.height = height;
            this.width = width;
            this.aspectRatio = (double)width / height;
        }
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double minWidth(double height) {
        return 160;
    }

    @Override
    public double minHeight(double width) {
        return Math.round(minWidth(-1) / aspectRatio);
    }

    @Override
    public double maxWidth(double height) {
        return 4096;
    } // just make it large

    @Override
    public double maxHeight(double width) {
        return Math.round(maxWidth(-1) / aspectRatio);
    }

    //
    @Override
    public double prefWidth(double height) {
        if (height > 0) {
            return Math.round(height * aspectRatio);
        } else {
            return width;
        }
    }

    @Override
    public double prefHeight(double width) {
        if (width > 0) {
            return Math.round(width / aspectRatio);
        } else {
            return height;
        }
    }

    @Override
    public void resize(double width, double height) {
        double newAspectRatio = width / height;
        if (newAspectRatio > aspectRatio) {
            // width to large, or height too small, this can happen
            setFitHeight(height);
            setFitWidth(Math.round(height * aspectRatio));
            return;
        } else {
            // width to small or height too large - this should not happen in horizontal mode
        }

        setFitWidth(width);
        setFitHeight(height);
    }
}
