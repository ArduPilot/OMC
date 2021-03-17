/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.controls.skins;

import com.intel.missioncontrol.helper.WindowHelper;
import com.intel.missioncontrol.ui.Theme;
import com.intel.missioncontrol.ui.controls.AdornerSplitView;
import com.sun.javafx.scene.NodeHelper;
import java.util.Collections;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Transform;
import javafx.stage.Popup;
import javafx.util.Duration;

public class AdornerSplitViewSkin extends SkinBase<AdornerSplitView> {

    private static final String POPOVER_STYLESHEET = "/com/intel/missioncontrol/styles/popover.css";
    private static final double POPOVER_MIN_DISTANCE_TO_EDGE = 5;
    private static final double POPOVER_ARROW_SIZE = 10;
    private static final String NODE_PROPERTY = "node";
    private static final String LISTENER_PROPERTY = "listener";

    private final HBox container = new HBox();
    private final Pane pane = new Pane();

    public AdornerSplitViewSkin(AdornerSplitView control) {
        super(control);

        container.setFillHeight(true);
        pane.setPrefWidth(Region.USE_COMPUTED_SIZE);
        pane.setMinWidth(Region.USE_COMPUTED_SIZE);
        pane.setMaxWidth(Double.POSITIVE_INFINITY);
        HBox.setHgrow(pane, Priority.NEVER);

        Node content = control.getContent();
        if (content != null) {
            HBox.setHgrow(content, Priority.ALWAYS);
            container.getChildren().setAll(content, pane);
        }

        getChildren().add(container);

        if (content instanceof Parent) {
            addPopovers((Parent)content);
        }
    }

    @SuppressWarnings("unchecked")
    private void addPopovers(Parent node) {
        if (node instanceof AdornerSplitView) {
            return;
        }

        Object popover = node.getProperties().get(AdornerSplitView.ADORNMENT_CONTENT);
        Object styleClass = node.getProperties().get(AdornerSplitView.ADORNMENT_STYLE_CLASS);
        if (popover != null) {
            addNode(node, styleClass instanceof List ? (List<String>)styleClass : Collections.emptyList(), popover);
        }

        for (Node child : node.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                addPopovers((Parent)child);
            }
        }
    }

    private void addNode(Node node, List<String> styleClass, Object popover) {
        Button button = new Button();
        button.getStyleClass().setAll(styleClass);
        button.managedProperty().bind(node.managedProperty());
        button.visibleProperty().bind(NodeHelper.treeVisibleProperty(node));
        button.setLayoutY(
            node.getLocalToSceneTransform().getTy()
                - getSkinnable().getLocalToSceneTransform().getTy()
                - getSkinnable().getInsets().getTop());

        ChangeListener<Transform> listener =
            (observable, oldValue, transform) ->
                button.setLayoutY(
                    transform.getTy()
                        - getSkinnable().getLocalToSceneTransform().getTy()
                        - getSkinnable().getInsets().getTop());

        node.localToSceneTransformProperty().addListener(listener);

        button.getProperties().put(NODE_PROPERTY, node);
        button.getProperties().put(LISTENER_PROPERTY, listener);
        button.setOnAction(event -> showPopover(popover, button.localToScreen(button.getWidth() / 2, 0)));

        pane.getChildren().add(button);
    }

    private void showPopover(Object content, Point2D location) {
        StackPane contentPane;
        if (content instanceof Node) {
            contentPane = new StackPane((Node)content);
        } else if (content instanceof String) {
            contentPane = new StackPane(new Label((String)content));
            contentPane.setPadding(new Insets(5));
        } else {
            contentPane = new StackPane(new Label(content.toString()));
            contentPane.setPadding(new Insets(5));
        }

        contentPane.getStyleClass().add("popover-content");
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        final double arrowOffset = POPOVER_ARROW_SIZE * 2;
        Polygon arrow = new Polygon();
        arrow.getStyleClass().add("arrow");
        arrow.getPoints().addAll(0.0, 0.0, POPOVER_ARROW_SIZE, POPOVER_ARROW_SIZE, POPOVER_ARROW_SIZE * 2, 0.0);
        VBox.setVgrow(arrow, Priority.NEVER);

        VBox rootPane = new VBox(contentPane, arrow);
        rootPane.getStylesheets().addAll(Theme.currentTheme().getStylesheets());
        rootPane.getStylesheets().add(AdornerSplitViewSkin.class.getResource(POPOVER_STYLESHEET).toExternalForm());

        Bounds rootBounds;
        Parent root = WindowHelper.getRoot(container.getScene());
        if (root instanceof Pane) {
            rootBounds = root.localToScreen(new BoundingBox(0, 0, ((Pane)root).getWidth(), ((Pane)root).getHeight()));
        } else {
            rootBounds = root.localToParent(new BoundingBox(0, 0, 0, 0));
        }

        Popup popup = new Popup();
        popup.getContent().add(rootPane);
        popup.setAutoHide(true);
        popup.setAutoFix(false);
        popup.setOnShown(
            event -> {
                final double y = location.getY() - popup.getHeight();
                double x = location.getX() - POPOVER_ARROW_SIZE - arrowOffset;
                double dx = x + popup.getWidth() + POPOVER_MIN_DISTANCE_TO_EDGE - rootBounds.getMaxX();
                if (dx > 0) {
                    x -= dx;
                }

                arrow.setTranslateX(location.getX() - x - POPOVER_ARROW_SIZE);
                rootPane.setOpacity(0);
                rootPane.setTranslateY(POPOVER_ARROW_SIZE);
                popup.setX(x);
                popup.setY(y);

                FadeTransition fade = new FadeTransition(Duration.millis(100));
                fade.setNode(rootPane);
                fade.setToValue(1);
                fade.play();

                TranslateTransition translation = new TranslateTransition(Duration.millis(100));
                translation.setNode(rootPane);
                translation.setToY(0);
                translation.setInterpolator(Interpolator.EASE_OUT);
                translation.play();
                translation.setOnFinished(
                    actionEvent -> {
                        rootPane.setPrefWidth(rootPane.getWidth());
                        rootPane.setPrefHeight(rootPane.getHeight());
                    });
            });

        popup.show(container.getScene().getWindow(), 0, -30000);
    }

}
