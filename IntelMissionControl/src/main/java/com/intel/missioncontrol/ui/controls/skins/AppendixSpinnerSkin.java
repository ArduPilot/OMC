/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.intel.missioncontrol.ui.controls.skins;

import com.intel.missioncontrol.ui.controls.SpinnerBehavior;
import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.control.FakeFocusTextField;
import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleRole;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

public class AppendixSpinnerSkin<T> extends SkinBase<Spinner<T>> {
    private final Text measureText = new Text();
    private StackPane container;
    private TextField textField;
    private Label appendixLabel;

    private Region incrementArrow;
    private StackPane incrementArrowButton;

    private Region decrementArrow;
    private StackPane decrementArrowButton;

    private static final int ARROWS_ON_RIGHT_VERTICAL = 0;
    private static final int ARROWS_ON_LEFT_VERTICAL = 1;
    private static final int ARROWS_ON_RIGHT_HORIZONTAL = 2;
    private static final int ARROWS_ON_LEFT_HORIZONTAL = 3;
    private static final int SPLIT_ARROWS_VERTICAL = 4;
    private static final int SPLIT_ARROWS_HORIZONTAL = 5;

    private int layoutMode = 0;

    private final SpinnerBehavior behavior;

    public AppendixSpinnerSkin(Spinner<T> control, String appendixText, double appendixOpacity) {
        super(control);

        behavior = new SpinnerBehavior<>(control);

        textField = control.getEditor();
        textField.textProperty().addListener(((observable, oldValue, newValue) -> updateAppendix()));
        textField.insetsProperty().addListener(((observable, oldValue, newValue) -> updateAppendix()));
        textField.widthProperty().addListener(((observable, oldValue, newValue) -> updateAppendix()));
        measureText.fontProperty().bind(textField.fontProperty());

        appendixLabel = new Label(appendixText);
        appendixLabel.setOpacity(appendixOpacity);
        appendixLabel.setCursor(Cursor.TEXT);
        appendixLabel.fontProperty().bind(textField.fontProperty());
        appendixLabel.prefHeightProperty().bind(textField.heightProperty());
        appendixLabel.setMouseTransparent(true);
        appendixLabel.setFocusTraversable(false);
        StackPane.setAlignment(appendixLabel, Pos.CENTER_LEFT);

        // In the usual SpinnerSkin, once the textfield is focused, it requests the focus of its parent.
        // Since our textField and appendixLabel are embedded in a StackPane, the focus of the StackPane would
        // be requested, which leads to faulty behavior. Hence, we override requestFocus to skip one node in
        // the hierarchy.
        container =
            new StackPane(textField, appendixLabel) {
                @Override
                public void requestFocus() {
                    super.getParent().requestFocus();
                }
            };

        getChildren().add(container);

        updateStyleClass();
        updateAppendix();
        control.getStyleClass().addListener((ListChangeListener<String>)c -> updateStyleClass());

        incrementArrow = new Region();
        incrementArrow.setFocusTraversable(false);
        incrementArrow.getStyleClass().setAll("increment-arrow");
        incrementArrow.setMaxWidth(Region.USE_PREF_SIZE);
        incrementArrow.setMaxHeight(Region.USE_PREF_SIZE);
        incrementArrow.setMouseTransparent(true);

        incrementArrowButton =
            new StackPane() {
                public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
                    switch (action) {
                    case FIRE:
                        getSkinnable().increment();
                        break;
                    default:
                        super.executeAccessibleAction(action, parameters);
                    }
                }
            };
        incrementArrowButton.setAccessibleRole(AccessibleRole.INCREMENT_BUTTON);
        incrementArrowButton.setFocusTraversable(false);
        incrementArrowButton.getStyleClass().setAll("increment-arrow-button");
        incrementArrowButton.getChildren().add(incrementArrow);
        incrementArrowButton.setOnMousePressed(
            e -> {
                getSkinnable().requestFocus();
                behavior.startSpinning(true);
            });
        incrementArrowButton.setOnMouseReleased(e -> behavior.stopSpinning());

        decrementArrow = new Region();
        decrementArrow.setFocusTraversable(false);
        decrementArrow.getStyleClass().setAll("decrement-arrow");
        decrementArrow.setMaxWidth(Region.USE_PREF_SIZE);
        decrementArrow.setMaxHeight(Region.USE_PREF_SIZE);
        decrementArrow.setMouseTransparent(true);

        decrementArrowButton =
            new StackPane() {
                public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
                    switch (action) {
                    case FIRE:
                        getSkinnable().decrement();
                        break;
                    default:
                        super.executeAccessibleAction(action, parameters);
                    }
                }
            };
        decrementArrowButton.setAccessibleRole(AccessibleRole.DECREMENT_BUTTON);
        decrementArrowButton.setFocusTraversable(false);
        decrementArrowButton.getStyleClass().setAll("decrement-arrow-button");
        decrementArrowButton.getChildren().add(decrementArrow);
        decrementArrowButton.setOnMousePressed(
            e -> {
                getSkinnable().requestFocus();
                behavior.startSpinning(false);
            });
        decrementArrowButton.setOnMouseReleased(e -> behavior.stopSpinning());

        getChildren().addAll(incrementArrowButton, decrementArrowButton);

        control.focusedProperty()
            .addListener(
                (ov, t, hasFocus) -> {
                    ((FakeFocusTextField)textField).setFakeFocus(hasFocus);
                });

        control.addEventFilter(
            KeyEvent.ANY,
            ke -> {
                if (control.isEditable()) {
                    if (ke.getTarget().equals(textField)) {
                        return;
                    }

                    // Fix for RT-38527 which led to a stack overflow
                    if (ke.getCode() == KeyCode.ESCAPE) {
                        return;
                    }

                    // Fix for the regression noted in a comment in RT-29885.
                    // This forwards the event down into the TextField when
                    // the key event is actually received by the Spinner.
                    textField.fireEvent(ke.copyFor(textField, textField));
                    ke.consume();
                }
            });

        // This event filter is to enable keyboard events being delivered to the
        // spinner when the user has mouse clicked into the TextField area of the
        // Spinner control. Without this the up/down/left/right arrow keys don't
        // work when you click inside the TextField area (but they do in the case
        // of tabbing in).
        textField.addEventFilter(
            KeyEvent.ANY,
            ke -> {
                if (!control.isEditable()) {
                    control.fireEvent(ke.copyFor(control, control));
                    ke.consume();
                }
            });

        textField
            .focusedProperty()
            .addListener(
                (ov, t, hasFocus) -> {
                    // Fix for RT-29885
                    control.getProperties().put("FOCUSED", hasFocus);
                    // --- end of RT-29885

                    // RT-21454 starts here
                    if (!hasFocus) {
                        pseudoClassStateChanged(CONTAINS_FOCUS_PSEUDOCLASS_STATE, false);
                    } else {
                        pseudoClassStateChanged(CONTAINS_FOCUS_PSEUDOCLASS_STATE, true);
                    }
                    // --- end of RT-21454
                });

        // end of comboBox-esque fixes

        textField.focusTraversableProperty().bind(control.editableProperty());

        // Following code borrowed from ComboBoxPopupControl, to resolve the
        // issue initially identified in RT-36902, but specifically (for Spinner)
        // identified in RT-40625
        ParentHelper.setTraversalEngine(
            control,
            new ParentTraversalEngine(
                control,
                new Algorithm() {

                    @Override
                    public Node select(Node owner, Direction dir, TraversalContext context) {
                        return null;
                    }

                    @Override
                    public Node selectFirst(TraversalContext context) {
                        return null;
                    }

                    @Override
                    public Node selectLast(TraversalContext context) {
                        return null;
                    }
                }));
    }

    @Override
    public void dispose() {
        super.dispose();

        if (behavior != null) {
            behavior.dispose();
        }
    }

    @Override
    protected void layoutChildren(final double x, final double y, final double w, final double h) {
        final double incrementArrowButtonWidth =
            incrementArrowButton.snappedLeftInset()
                + snapSizeX(incrementArrow.prefWidth(-1))
                + incrementArrowButton.snappedRightInset();

        final double decrementArrowButtonWidth =
            decrementArrowButton.snappedLeftInset()
                + snapSizeX(decrementArrow.prefWidth(-1))
                + decrementArrowButton.snappedRightInset();

        final double widestArrowButton = Math.max(incrementArrowButtonWidth, decrementArrowButtonWidth);

        // we need to decide on our layout approach, and this depends on
        // the presence of style classes in the Spinner styleClass list.
        // To be a bit more efficient, we observe the list for changes, so
        // here in layoutChildren we can just react to a few booleans.
        if (layoutMode == ARROWS_ON_RIGHT_VERTICAL || layoutMode == ARROWS_ON_LEFT_VERTICAL) {
            final double textFieldStartX = layoutMode == ARROWS_ON_RIGHT_VERTICAL ? x : x + widestArrowButton;
            final double buttonStartX = layoutMode == ARROWS_ON_RIGHT_VERTICAL ? x + w - widestArrowButton : x;
            final double halfHeight = Math.floor(h / 2.0);

            container.resizeRelocate(textFieldStartX, y, w - widestArrowButton, h);

            incrementArrowButton.resize(widestArrowButton, halfHeight);
            positionInArea(
                incrementArrowButton, buttonStartX, y, widestArrowButton, halfHeight, 0, HPos.CENTER, VPos.CENTER);

            decrementArrowButton.resize(widestArrowButton, halfHeight);
            positionInArea(
                decrementArrowButton,
                buttonStartX,
                y + halfHeight,
                widestArrowButton,
                h - halfHeight,
                0,
                HPos.CENTER,
                VPos.BOTTOM);
        } else if (layoutMode == ARROWS_ON_RIGHT_HORIZONTAL || layoutMode == ARROWS_ON_LEFT_HORIZONTAL) {
            final double totalButtonWidth = incrementArrowButtonWidth + decrementArrowButtonWidth;
            final double textFieldStartX = layoutMode == ARROWS_ON_RIGHT_HORIZONTAL ? x : x + totalButtonWidth;
            final double buttonStartX = layoutMode == ARROWS_ON_RIGHT_HORIZONTAL ? x + w - totalButtonWidth : x;

            container.resizeRelocate(textFieldStartX, y, w - totalButtonWidth, h);

            // decrement is always on the left
            decrementArrowButton.resize(decrementArrowButtonWidth, h);
            positionInArea(
                decrementArrowButton, buttonStartX, y, decrementArrowButtonWidth, h, 0, HPos.CENTER, VPos.CENTER);

            // ... and increment is always on the right
            incrementArrowButton.resize(incrementArrowButtonWidth, h);
            positionInArea(
                incrementArrowButton,
                buttonStartX + decrementArrowButtonWidth,
                y,
                incrementArrowButtonWidth,
                h,
                0,
                HPos.CENTER,
                VPos.CENTER);
        } else if (layoutMode == SPLIT_ARROWS_VERTICAL) {
            final double incrementArrowButtonHeight =
                incrementArrowButton.snappedTopInset()
                    + snapSizeY(incrementArrow.prefHeight(-1))
                    + incrementArrowButton.snappedBottomInset();

            final double decrementArrowButtonHeight =
                decrementArrowButton.snappedTopInset()
                    + snapSizeY(decrementArrow.prefHeight(-1))
                    + decrementArrowButton.snappedBottomInset();

            final double tallestArrowButton = Math.max(incrementArrowButtonHeight, decrementArrowButtonHeight);

            // increment is at the top
            incrementArrowButton.resize(w, tallestArrowButton);
            positionInArea(incrementArrowButton, x, y, w, tallestArrowButton, 0, HPos.CENTER, VPos.CENTER);

            // textfield in the middle
            container.resizeRelocate(x, y + tallestArrowButton, w, h - (2 * tallestArrowButton));

            // decrement is at the bottom
            decrementArrowButton.resize(w, tallestArrowButton);
            positionInArea(
                decrementArrowButton, x, h - tallestArrowButton, w, tallestArrowButton, 0, HPos.CENTER, VPos.CENTER);
        } else if (layoutMode == SPLIT_ARROWS_HORIZONTAL) {
            // decrement is on the left-hand side
            decrementArrowButton.resize(widestArrowButton, h);
            positionInArea(decrementArrowButton, x, y, widestArrowButton, h, 0, HPos.CENTER, VPos.CENTER);

            // textfield in the middle
            container.resizeRelocate(x + widestArrowButton, y, w - (2 * widestArrowButton), h);

            // increment is on the right-hand side
            incrementArrowButton.resize(widestArrowButton, h);
            positionInArea(
                incrementArrowButton, w - widestArrowButton, y, widestArrowButton, h, 0, HPos.CENTER, VPos.CENTER);
        }
    }

    @Override
    protected double computeMinWidth(
            double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return container.minWidth(height);
    }

    @Override
    protected double computeMinHeight(
            double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computePrefWidth(
            double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        final double textfieldWidth = container.prefWidth(height);
        return leftInset + textfieldWidth + rightInset;
    }

    @Override
    protected double computePrefHeight(
            double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        double ph;
        double textFieldHeight = container.prefHeight(width);

        if (layoutMode == SPLIT_ARROWS_VERTICAL) {
            ph =
                topInset
                    + incrementArrowButton.prefHeight(width)
                    + textFieldHeight
                    + decrementArrowButton.prefHeight(width)
                    + bottomInset;
        } else {
            ph = topInset + textFieldHeight + bottomInset;
        }

        return ph;
    }

    @Override
    protected double computeMaxWidth(
            double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefWidth(height);
    }

    @Override
    protected double computeMaxHeight(
            double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefHeight(width);
    }

    // Overridden so that we use the textfield as the baseline, rather than the arrow.
    // See RT-30754 for more information.
    @Override
    protected double computeBaselineOffset(double topInset, double rightInset, double bottomInset, double leftInset) {
        return textField.getLayoutBounds().getMinY() + textField.getLayoutY() + textField.getBaselineOffset();
    }

    private void updateAppendix() {
        measureText.setText(textField.getText());
        double width = measureText.getLayoutBounds().getWidth() + textField.getInsets().getLeft();
        boolean visible = width < textField.getWidth();
        appendixLabel.setManaged(visible);
        appendixLabel.setVisible(visible);
        appendixLabel.setTranslateX(width + 5);
        appendixLabel.setTranslateY(0.4);
    }

    private void updateStyleClass() {
        final List<String> styleClass = getSkinnable().getStyleClass();

        if (styleClass.contains(Spinner.STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL)) {
            layoutMode = ARROWS_ON_LEFT_VERTICAL;
        } else if (styleClass.contains(Spinner.STYLE_CLASS_ARROWS_ON_LEFT_HORIZONTAL)) {
            layoutMode = ARROWS_ON_LEFT_HORIZONTAL;
        } else if (styleClass.contains(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL)) {
            layoutMode = ARROWS_ON_RIGHT_HORIZONTAL;
        } else if (styleClass.contains(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL)) {
            layoutMode = SPLIT_ARROWS_VERTICAL;
        } else if (styleClass.contains(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL)) {
            layoutMode = SPLIT_ARROWS_HORIZONTAL;
        } else {
            layoutMode = ARROWS_ON_RIGHT_VERTICAL;
        }
    }

    private static PseudoClass CONTAINS_FOCUS_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("contains-focus");
}
