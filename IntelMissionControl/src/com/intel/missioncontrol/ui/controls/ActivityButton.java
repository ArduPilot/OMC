/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.ui.accessibility.IShortcutAware;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ActivityButton extends javafx.scene.control.Button implements IShortcutAware {

    private final StringProperty shortcut = new SimpleStringProperty();
//    private final int ICON_SIZE = (int)ScaleHelper.emsToPixels(1);
    private final int ICON_SIZE = 16;
    
    private final BooleanProperty isBusy =
        new SimpleBooleanProperty() {
            private RotateTransition rotateTransition;

            @Override
            protected void invalidated() {
                super.invalidated();

                Node graphic = getGraphic();
                if (graphic == null) {
                    ImageView imageView =
                        new ImageView(
                            ActivityButton.class
                                .getResource("/com/intel/missioncontrol/icons/icon_progress.svg")
                                .toExternalForm());
                    imageView.setFitWidth(ICON_SIZE);
                    imageView.setFitHeight(ICON_SIZE);
                    graphic = imageView;
                    setGraphic(imageView);
                }

                if (get()) {
                    setPrefWidth(getWidth());
                    rotateTransition = new RotateTransition(Duration.millis(500), graphic);
                    rotateTransition.setFromAngle(0);
                    rotateTransition.setToAngle(360);
                    rotateTransition.setCycleCount(Animation.INDEFINITE);
                    rotateTransition.play();
                    setStyle("-fx-content-display: graphic-only");
                    graphic.setVisible(true);
                    graphic.setManaged(true);

                    if (!disableProperty().isBound()) {
                        setDisable(true);
                    }
                } else {
                    if (rotateTransition != null) {
                        rotateTransition.stop();
                        rotateTransition = null;
                    }

                    setStyle("-fx-content-display: left");
                    graphic.setVisible(false);
                    graphic.setManaged(false);

                    if (!disableProperty().isBound()) {
                        setDisable(false);
                    }
                }
            }

            @Override
            public Object getBean() {
                return ActivityButton.this;
            }

            @Override
            public String getName() {
                return "isBusy";
            }
        };

    public StringProperty shortcutProperty() {
        return shortcut;
    }

    @Nullable
    public String getShortcut() {
        return shortcut.get();
    }

    public void setShortcut(@Nullable String shortcut) {
        this.shortcut.set(shortcut);
    }

    public BooleanProperty isBusyProperty() {
        return isBusy;
    }

    public boolean isBusy() {
        return isBusy.get();
    }

    public void setIsBusy(boolean isBusy) {
        this.isBusy.set(isBusy);
    }

}
