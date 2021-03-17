/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class RotatingImageView extends ImageView {
    private final BooleanProperty isRotating =
        new SimpleBooleanProperty() {
            private RotateTransition rotateTransition;

            @Override
            protected void invalidated() {
                super.invalidated();

                if (get()) {
                    startRotating();
                } else {
                    stopRotating();
                }
            }

            private void startRotating()
            {
                stopRotating();

                rotateTransition = new RotateTransition(Duration.millis(2000), RotatingImageView.this);
                rotateTransition.setInterpolator(Interpolator.LINEAR);
                rotateTransition.setFromAngle(0);
                rotateTransition.setToAngle(360);
                rotateTransition.setCycleCount(Animation.INDEFINITE);
                rotateTransition.play();
            }

            private void stopRotating()
            {
                if (rotateTransition != null) {
                    rotateTransition.stop();
                    rotateTransition = null;
                    setRotate(0.0);
                }
            }

            @Override
            public Object getBean() {
                return RotatingImageView.this;
            }

            @Override
            public String getName() {
                return "isRotating";
            }
        };

    public RotatingImageView() {}

    public RotatingImageView(String s) {
        super(s);
    }

    public RotatingImageView(Image image) {
        super(image);
    }

    public BooleanProperty isRotatingProperty() {
        return isRotating;
    }

    public void setIsRotating(boolean isRotating) {
        this.isRotating.set(isRotating);
    }
}
