/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls.skins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.controlsfx.control.ToggleSwitch;

/** Skin implementation for the {@link ToggleSwitch} based on {@link impl.org.controlsfx.skin.ToggleSwitchSkin}. */
public class ToggleSwitchSkin extends SkinBase<ToggleSwitch> {

    private static final CssMetaData<ToggleSwitch, Number> THUMB_MOVE_ANIMATION_TIME =
        new CssMetaData<>("-thumb-move-animation-time", SizeConverter.getInstance(), 200) {
            @Override
            public boolean isSettable(ToggleSwitch toggleSwitch) {
                final ToggleSwitchSkin skin = (ToggleSwitchSkin)toggleSwitch.getSkin();
                return skin.thumbMoveAnimationTime == null || !skin.thumbMoveAnimationTime.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(ToggleSwitch toggleSwitch) {
                final ToggleSwitchSkin skin = (ToggleSwitchSkin)toggleSwitch.getSkin();
                return (StyleableProperty<Number>)skin.thumbMoveAnimationTimeProperty();
            }
        };

    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

    static {
        final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(SkinBase.getClassCssMetaData());
        styleables.add(THUMB_MOVE_ANIMATION_TIME);
        STYLEABLES = Collections.unmodifiableList(styleables);
    }

    /** @return The CssMetaData associated with this class, which may include the CssMetaData of its super classes. */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return STYLEABLES;
    }

    private final StackPane thumb;
    private final StackPane thumbArea;
    private final Label label;
    private final StackPane labelContainer;
    private final TranslateTransition transition;
    private final String defaultSelectedText;
    private final String defaultNotSelectedText;

    /**
     * Constructor for all ToggleSwitchSkin instances.
     *
     * @param control The ToggleSwitch for which this Skin should attach to.
     */
    public ToggleSwitchSkin(ToggleSwitch control) {
        super(control);

        ResourceBundle bundle = ResourceBundle.getBundle("com/intel/missioncontrol/IntelMissionControl");
        defaultSelectedText = bundle.getString(ToggleSwitchSkin.class.getName() + ".yes");
        defaultNotSelectedText = bundle.getString(ToggleSwitchSkin.class.getName() + ".no");

        thumb = new StackPane();
        thumbArea = new StackPane();
        label = new Label();
        labelContainer = new StackPane();
        transition = new TranslateTransition(Duration.millis(getThumbMoveAnimationTime()), thumb);

        label.textProperty()
            .bind(
                Bindings.createStringBinding(
                    () -> {
                        String controlText = control.getText();
                        if (controlText == null || controlText.isEmpty()) {
                            return control.isSelected() ? defaultSelectedText : defaultNotSelectedText;
                        }

                        return controlText;
                    },
                    control.textProperty(),
                    control.selectedProperty()));

        getChildren().addAll(labelContainer, thumbArea, thumb);
        labelContainer.getChildren().addAll(label);
        StackPane.setAlignment(label, Pos.CENTER_LEFT);
        label.setStyle("-fx-padding: 0 0 0 0.5em");

        thumb.getStyleClass().setAll("thumb");
        thumbArea.getStyleClass().setAll("thumb-area");

        thumbArea.setOnMouseReleased(event -> mousePressedOnToggleSwitch(control));
        thumb.setOnMouseReleased(event -> mousePressedOnToggleSwitch(control));
        control.selectedProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.booleanValue() != oldValue.booleanValue()) {
                        selectedStateChanged();
                    }
                });
    }

    private void selectedStateChanged() {
        transition.stop();

        double thumbAreaWidth = snapSize(thumbArea.prefWidth(-1));
        double thumbWidth = snapSize(thumb.prefWidth(-1));
        double distance = thumbAreaWidth - thumbWidth;
        /** If we are not selected, we need to go from right to left. */
        if (!getSkinnable().isSelected()) {
            thumb.setLayoutX(thumbArea.getLayoutX());
            transition.setFromX(distance);
            transition.setToX(0);
        } else {
            thumb.setTranslateX(thumbArea.getLayoutX());
            transition.setFromX(0);
            transition.setToX(distance);
        }

        transition.setCycleCount(1);
        transition.play();
    }

    private void mousePressedOnToggleSwitch(ToggleSwitch toggleSwitch) {
        toggleSwitch.setSelected(!toggleSwitch.isSelected());
    }

    /** How many milliseconds it should take for the thumb to go from one edge to the other */
    private DoubleProperty thumbMoveAnimationTime = null;

    private DoubleProperty thumbMoveAnimationTimeProperty() {
        if (thumbMoveAnimationTime == null) {
            thumbMoveAnimationTime =
                new StyleableDoubleProperty(200) {

                    @Override
                    public Object getBean() {
                        return ToggleSwitchSkin.this;
                    }

                    @Override
                    public String getName() {
                        return "thumbMoveAnimationTime";
                    }

                    @Override
                    public CssMetaData<ToggleSwitch, Number> getCssMetaData() {
                        return THUMB_MOVE_ANIMATION_TIME;
                    }
                };
        }

        return thumbMoveAnimationTime;
    }

    private double getThumbMoveAnimationTime() {
        return thumbMoveAnimationTime == null ? 200 : thumbMoveAnimationTime.get();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        ToggleSwitch toggleSwitch = getSkinnable();
        double thumbWidth = snapSize(thumb.prefWidth(-1));
        double thumbHeight = snapSize(thumb.prefHeight(-1));
        thumb.resize(thumbWidth, thumbHeight);
        transition.stop();
        // We must reset the TranslateX otherwise the thumb is mis-aligned when window is resized.
        thumb.setTranslateX(0);

        double thumbAreaY = snapPosition(contentY);
        double thumbAreaWidth = snapSize(thumbArea.prefWidth(-1));
        double thumbAreaHeight = snapSize(thumbArea.prefHeight(-1));

        thumbArea.resize(thumbAreaWidth, thumbAreaHeight);
        thumbArea.setLayoutY(thumbAreaY);

        labelContainer.resize(contentWidth - thumbAreaWidth, thumbAreaHeight);
        labelContainer.setLayoutX(thumbAreaWidth);
        labelContainer.setLayoutY(thumbAreaY);

        if (!toggleSwitch.isSelected()) thumb.setLayoutX(thumbArea.getLayoutX());
        else thumb.setLayoutX(thumbArea.getLayoutX() + thumbAreaWidth - thumbWidth);
        thumb.setLayoutY(thumbAreaY + (thumbAreaHeight - thumbHeight) / 2);
    }

    @Override
    protected double computeMinWidth(
            double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return leftInset + label.prefWidth(-1) + thumbArea.prefWidth(-1) + rightInset;
    }

    @Override
    protected double computeMinHeight(
            double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return topInset + Math.max(thumb.prefHeight(-1), label.prefHeight(-1)) + bottomInset;
    }

    @Override
    protected double computePrefWidth(
            double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double labelPrefWidth = label.prefWidth(-1);
        if (labelPrefWidth > 0) {
            // according to style guide distance between toggle and label should be 0.5em
            labelPrefWidth += label.getFont().getSize() / 2;
        }

        return leftInset + thumbArea.prefWidth(-1) + labelPrefWidth + rightInset;
    }

    @Override
    protected double computePrefHeight(
            double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return topInset + Math.max(thumb.prefHeight(-1), label.prefHeight(-1)) + bottomInset;
    }

    /** {@inheritDoc} */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }
}
