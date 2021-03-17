/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.widget;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;

public class ProgressButton extends StackPane {

    private static final String CLASS_PRIMARY_BUTTON = "primary-button";
    private static final String CLASS_SECONDARY_BUTTON = "secondary-button";

    private static final String CLASS_PROGRESS_BUTTON_PROGRESS_REGION = "progress-button-progress-region";
    private static final String CLASS_PROGRESS_BUTTON_COMMON = "progress-button-common";

    private static final Duration TEXT_ANIMATION_DURATION = Duration.millis(500);
    private static final Duration PROGRESS_ANIMATION_DURATION = Duration.millis(2000);

    private final Pane progressRegion = new Pane();
    private final Button button = new Button();
    private final HBox progressRegionContainer = new HBox();

    private final Timeline progressLineAnimation;
    private Timeline buttonTextAnimation;

    private EventHandler<? super MouseEvent> onMouseClickHandler;
    private EventHandler<? super ActionEvent> onActionHandler;

    private String text;
    private String textInProgress;
    private boolean isAnimatedDots;

    public enum State {
        PRE_PROGRESS,
        IN_PROGRESS,
        POST_PROGRESS,
        DISABLED
    }

    private ObjectProperty<State> currentStateProperty = new SimpleObjectProperty<>();

    private static final List<String> PROGRESS_POINT_FORMAT = Arrays.asList("%s.", " %s..", "  %s...");

    private BooleanProperty isPrimary = new SimpleBooleanProperty(false);

    public ProgressButton() {
        progressLineAnimation = new Timeline();
        progressLineAnimation.setCycleCount(Animation.INDEFINITE);

        progressRegion.setMouseTransparent(true);

        progressRegion.getStyleClass().add(CLASS_PROGRESS_BUTTON_PROGRESS_REGION);

        progressRegionContainer.setMouseTransparent(true);
        progressRegionContainer.getStyleClass().add(CLASS_PROGRESS_BUTTON_COMMON);
        progressRegionContainer.getChildren().add(progressRegion);

        updateButtonStyle();
        isPrimary.addListener((observable, oldValue, newValue) -> updateButtonStyle());

        getChildren().add(button);
        getChildren().add(progressRegionContainer);

        initBindings();
    }

    public ObjectProperty<State> stateProperty() {
        return currentStateProperty;
    }

    public void setOnMouseClickedHandler(EventHandler<? super MouseEvent> handler) {
        onMouseClickHandler = handler;
        button.onMouseClickedProperty().set(handler);
    }

    public EventHandler<? super ActionEvent> getOnActionHandler() {
        return onActionHandler;
    }

    public void setOnActionHandler(EventHandler<ActionEvent> handler) {
        onActionHandler = handler;
        button.onActionProperty().set(handler);
    }

    public EventHandler<? super MouseEvent> getOnMouseClickedHandler() {
        return onMouseClickHandler;
    }

    public void setText(String text) {
        this.text = text;
        button.setText(text);
    }

    public String getText() {
        return text;
    }

    public void setTextInProgress(String text) {
        textInProgress = text;
    }

    public String getTextInProgress() {
        return textInProgress;
    }

    public void setAnimatedDots(boolean isAnimatedDots) {
        this.isAnimatedDots = isAnimatedDots;

        if (!isAnimatedDots) {
            return;
        }

        buttonTextAnimation =
            new Timeline(
                new KeyFrame(
                    TEXT_ANIMATION_DURATION,
                    new EventHandler<ActionEvent>() {
                        int index = 0;

                        @Override
                        public void handle(ActionEvent event) {
                            String formatString =
                                PROGRESS_POINT_FORMAT.get(Math.abs(index++) % PROGRESS_POINT_FORMAT.size());
                            button.textProperty().set(String.format(formatString, getTextInProgress()));
                        }
                    }));
        buttonTextAnimation.setCycleCount(Animation.INDEFINITE);
    }

    public boolean getAnimatedDots() {
        return isAnimatedDots;
    }

    private void actionPreProgress() {
        setDisable(false);
        button.setText(getText());
        progressLineAnimation.stop();
        progressRegion.setPrefWidth(0);
        if (null != buttonTextAnimation) {
            buttonTextAnimation.stop();
        }
    }

    private void actionInProgress() {
        setDisable(true);
        button.setText(getTextInProgress());
        button.toFront();
        startProgressAnimation();
        if (null != buttonTextAnimation) {
            buttonTextAnimation.play();
        }
    }

    private void startProgressAnimation() {
        if (progressLineAnimation.getKeyFrames().isEmpty()) {
            progressLineAnimation
                .getKeyFrames()
                .add(
                    new KeyFrame(
                        PROGRESS_ANIMATION_DURATION,
                        new KeyValue(progressRegion.prefWidthProperty(), button.getWidth())));
        }

        progressLineAnimation.play();
    }

    private void actionPostProgress() {
        setDisable(true);
        button.setText(getText());
        progressLineAnimation.stop();
        progressRegion.setPrefWidth(0);
        if (null != buttonTextAnimation) {
            buttonTextAnimation.stop();
        }
    }

    private void initBindings() {
        progressRegionContainer.prefWidthProperty().bind(super.prefWidthProperty());
        progressRegionContainer.prefHeightProperty().bind(super.prefHeightProperty());
        progressRegionContainer.minWidthProperty().bind(super.minWidthProperty());
        progressRegionContainer.minHeightProperty().bind(super.minHeightProperty());
        progressRegionContainer.maxWidthProperty().bind(super.maxWidthProperty());
        progressRegionContainer.maxHeightProperty().bind(super.maxHeightProperty());

        progressRegion.prefHeightProperty().bind(progressRegionContainer.prefHeightProperty());
        progressRegion.minHeightProperty().bind(progressRegionContainer.minHeightProperty());
        progressRegion.maxHeightProperty().bind(progressRegionContainer.maxHeightProperty());

        button.prefWidthProperty().bind(super.prefWidthProperty());
        button.prefHeightProperty().bind(super.prefHeightProperty());
        button.minWidthProperty().bind(super.minWidthProperty());
        button.minHeightProperty().bind(super.minHeightProperty());
        button.maxWidthProperty().bind(super.maxWidthProperty());
        button.maxHeightProperty().bind(super.maxHeightProperty());

        currentStateProperty.addListener(
            (observable, oldValue, newValue) -> {
                switch (newValue) {
                case PRE_PROGRESS:
                    actionPreProgress();
                    break;
                case IN_PROGRESS:
                    actionInProgress();
                    break;
                case POST_PROGRESS:
                case DISABLED:
                    actionPostProgress();
                    break;
                }
            });
    }

    private void updateButtonStyle() {
        ObservableList<String> buttonStyleClass = button.getStyleClass();

        if (isPrimary()) {
            buttonStyleClass.remove(CLASS_SECONDARY_BUTTON);
            buttonStyleClass.add(CLASS_PRIMARY_BUTTON);
        } else {
            buttonStyleClass.remove(CLASS_PRIMARY_BUTTON);
            buttonStyleClass.add(CLASS_SECONDARY_BUTTON);
        }
    }

    public boolean isPrimary() {
        return isPrimary.get();
    }

    public void setPrimary(boolean isPrimary) {
        this.isPrimary.set(isPrimary);
    }

}
