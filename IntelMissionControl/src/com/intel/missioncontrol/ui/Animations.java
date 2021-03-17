/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui;

import com.intel.missioncontrol.helper.ScaleHelper;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Animations {

    public static final int ROTATION_CLOCK_WISE = 360;

    private static final Interpolator EASE_OUT = Interpolator.EASE_OUT;
    private static final Duration MEDIUM_DURATION = Duration.millis(200);
    private static final Duration LONG_DURATION = Duration.millis(350);
    private static final double HORIZONTAL_TRANSLATION_DELTA = 20;
    private static final double VERTICAL_TRANSLATION_DELTA = 20;

    public static void animatePrefWidth(Pane view, double width) {
        new Timeline(
                new KeyFrame(
                    Duration.ZERO, new KeyValue(view.prefWidthProperty(), view.getPrefWidth(), EASE_OUT)),
                new KeyFrame(MEDIUM_DURATION, new KeyValue(view.prefWidthProperty(), width, EASE_OUT)))
            .play();
    }

    public static void translateLeftToRight(Parent view, boolean reverse) {
        translateLeftToRight(view, reverse, () -> {});
    }

    public static void translateLeftToRight(Parent view, boolean reverse, Runnable onFinished) {
        view.setCacheHint(CacheHint.SPEED);
        TranslateTransition translation = new TranslateTransition(MEDIUM_DURATION, view);
        translation.setFromX(reverse ? 0 : -HORIZONTAL_TRANSLATION_DELTA);
        translation.setToX(reverse ? -HORIZONTAL_TRANSLATION_DELTA : 0);
        translation.setInterpolator(EASE_OUT);
        translation.setOnFinished(
            (event) -> {
                view.setCacheHint(CacheHint.DEFAULT);
                onFinished.run();
            });

        translation.play();
    }

    public static void translateRightToLeft(Parent view, boolean reverse) {
        translateRightToLeft(view, reverse, () -> {});
    }

    public static void translateRightToLeft(Parent view, boolean reverse, Runnable onFinished) {
        view.setCacheHint(CacheHint.SPEED);
        TranslateTransition translation = new TranslateTransition(MEDIUM_DURATION, view);
        translation.setFromX(reverse ? 0 : HORIZONTAL_TRANSLATION_DELTA);
        translation.setToX(reverse ? HORIZONTAL_TRANSLATION_DELTA : 0);
        translation.setInterpolator(EASE_OUT);
        translation.setOnFinished(
            (event) -> {
                view.setCacheHint(CacheHint.DEFAULT);
                onFinished.run();
            });

        translation.play();
    }

    public static void fade(Parent view, boolean fadeOut) {
        fade(view, fadeOut, () -> {});
    }

    public static void fade(Parent view, boolean fadeOut, Runnable onFinished) {
        if (fadeOut && !view.isVisible()) {
            onFinished.run();
            return;
        }

        view.setVisible(true);
        FadeTransition fade = new FadeTransition(MEDIUM_DURATION, view);
        fade.setFromValue(fadeOut ? 1 : 0);
        fade.setToValue(fadeOut ? 0 : 1);
        fade.setInterpolator(EASE_OUT);

        if (fadeOut) {
            fade.setOnFinished(
                event -> {
                    view.setVisible(false);
                    onFinished.run();
                });
        }

        fade.play();
    }

    public static void horizontalFadeInLeft(Node view) {
        horizontalFadeIn(view, () -> {}, false);
    }

    public static void horizontalFadeInRight(Node view) {
        horizontalFadeIn(view, () -> {}, true);
    }

    public static void horizontalFadeInLeft(Node view, Runnable onFinished) {
        horizontalFadeIn(view, onFinished, false);
    }

    public static void horizontalFadeInRight(Node view, Runnable onFinished) {
        horizontalFadeIn(view, onFinished, true);
    }

    private static void horizontalFadeIn(final Node view, final Runnable onFinished, boolean reverse) {
        view.setCacheHint(CacheHint.SPEED);

        TranslateTransition translation = new TranslateTransition(MEDIUM_DURATION, view);
        translation.setFromX(reverse ? -HORIZONTAL_TRANSLATION_DELTA : HORIZONTAL_TRANSLATION_DELTA);
        translation.setToX(0);
        translation.setInterpolator(Interpolator.EASE_OUT);
        translation.setOnFinished(
            event -> {
                view.setCacheHint(CacheHint.DEFAULT);
                if (onFinished != null) {
                    onFinished.run();
                }
            });
        translation.play();

        FadeTransition fade = new FadeTransition(MEDIUM_DURATION, view);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(EASE_OUT);
        fade.play();

        view.setVisible(true);
        view.setManaged(true);
    }

    public static void verticalFadeInUp(Node view) {
        verticalFadeIn(view, () -> {}, false);
    }

    public static void verticalFadeInDown(Node view) {
        verticalFadeIn(view, () -> {}, true);
    }

    public static void verticalFadeInUp(Node view, Runnable onFinished) {
        verticalFadeIn(view, onFinished, false);
    }

    public static void verticalFadeInDown(Node view, Runnable onFinished) {
        verticalFadeIn(view, onFinished, true);
    }

    private static void verticalFadeIn(final Node view, final Runnable onFinished, boolean reverse) {
        view.setCacheHint(CacheHint.SPEED);

        TranslateTransition translation = new TranslateTransition(MEDIUM_DURATION, view);
        translation.setFromY(reverse ? -VERTICAL_TRANSLATION_DELTA : VERTICAL_TRANSLATION_DELTA);
        translation.setToY(0);
        translation.setInterpolator(EASE_OUT);
        translation.setOnFinished(
            event -> {
                view.setCacheHint(CacheHint.DEFAULT);
                if (onFinished != null) {
                    onFinished.run();
                }
            });
        translation.play();

        FadeTransition fade = new FadeTransition(MEDIUM_DURATION, view);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(EASE_OUT);
        fade.play();

        view.setVisible(true);
        view.setManaged(true);
    }

    public static void verticalSlideUp(Region view) {
        verticalSlide(view, true);
    }

    public static void verticalSlideDown(Region view) {
        verticalSlide(view, false);
    }

    private static void verticalSlide(Region view, boolean visible) {
        view.setCacheHint(CacheHint.SPEED);

        TranslateTransition translation = new TranslateTransition(LONG_DURATION, view);
        translation.setFromY(visible ? view.getHeight() : 0);
        translation.setToY(visible ? 0 : view.getHeight());
        translation.setFromX(0);
        translation.setToX(0);
        translation.setOnFinished(event -> view.setCacheHint(CacheHint.DEFAULT));
        translation.play();
        view.setVisible(visible);
    }

    public static void spinForever(Node node) {
        RotateTransition rotation = new RotateTransition(Duration.millis(1000), node);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        rotation.setCycleCount(Animation.INDEFINITE);
        rotation.play();
    }

    public static Animation forButtonGraphicRotation(Button button, int rotationDirection) {
        Animation buttonAnimation = forImageRotation(button.getGraphic(), rotationDirection);

        // Disable button on animation play
        if (!button.disableProperty().isBound()) {
            buttonAnimation
                .statusProperty()
                .addListener((obs, oldStatus, newStatus) -> button.setDisable(newStatus == Animation.Status.RUNNING));
        }

        return buttonAnimation;
    }

    public static Animation forImageRotation(Node graphic, int rotationDirection) {
        graphic.getTransforms().clear();
        Bounds parentBounds = graphic.boundsInParentProperty().get();
        Rotate rotationTransform = new Rotate(0, parentBounds.getHeight() / 2, parentBounds.getWidth() / 2);
        graphic.getTransforms().add(rotationTransform);
        Timeline progressAnimation = new Timeline();
        progressAnimation
            .getKeyFrames()
            .add(new KeyFrame(Duration.seconds(2), new KeyValue(rotationTransform.angleProperty(), rotationDirection)));
        progressAnimation.setCycleCount(Animation.INDEFINITE);

        return progressAnimation;
    }
}
