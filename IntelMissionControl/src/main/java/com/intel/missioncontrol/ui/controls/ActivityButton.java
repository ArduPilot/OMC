/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls;

import de.saxsys.mvvmfx.utils.commands.Command;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class ActivityButton extends Button {

    private static final int ICON_SIZE = 16;

    private final BooleanProperty isBusy =
        new SimpleBooleanProperty() {
            private List<String> iconStyles = new ArrayList<>();
            private RotateTransition rotateTransition;

            @Override
            protected void invalidated() {
                super.invalidated();

                Node graphic = getGraphic();
                if (graphic == null) {
                    String url =
                        getStyleClass().contains("inverted")
                            ? "/com/intel/missioncontrol/icons/icon_progress(fill=theme-primary-button-text-color).svg"
                            : "/com/intel/missioncontrol/icons/icon_progress.svg";
                    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                    ImageView imageView = new ImageView(contextClassLoader.getResource(url).toExternalForm());
                    imageView.setFitWidth(ICON_SIZE);
                    imageView.setFitHeight(ICON_SIZE);
                    graphic = imageView;
                    setGraphic(imageView);
                }

                if (get()) {
                    ListIterator<String> it = getStyleClass().listIterator();
                    while (it.hasNext()) {
                        String styleClass = it.next();
                        if (styleClass.startsWith("icon-")) {
                            iconStyles.add(styleClass);
                            it.remove();
                        }
                    }

                    setPrefWidth(getWidth());
                    rotateTransition = new RotateTransition(Duration.millis(1500), graphic);
                    rotateTransition.setInterpolator(Interpolator.LINEAR);
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
                    getStyleClass().addAll(iconStyles);
                    iconStyles.clear();

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

    public ActivityButton() {
        commandProperty().addListener(this::commandChanged);
    }

    public ActivityButton(String text) {
        super(text);
        commandProperty().addListener(this::commandChanged);
    }

    public ActivityButton(String text, Node graphic) {
        super(text, graphic);
        commandProperty().addListener(this::commandChanged);
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

    private void commandChanged(ObservableValue<? extends Command> observable, Command oldValue, Command newValue) {
        if (newValue != null) {
            isBusyProperty().bind(newValue.runningProperty());
        } else {
            isBusyProperty().unbind();
        }
    }

}
