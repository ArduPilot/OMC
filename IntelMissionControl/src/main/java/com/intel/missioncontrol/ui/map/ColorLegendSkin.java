/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.map;

import com.intel.missioncontrol.helper.ScaleHelper;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

class ColorLegendSkin extends SkinBase<ColorLegend> {

    private final VBox root = new VBox();

    ColorLegendSkin(ColorLegend control) {
        super(control);
        root.setPickOnBounds(false);
        root.setSpacing(ScaleHelper.emsToPixels(0.5));
        getChildren().add(root);
        control.setMouseTransparent(true);
        control.setMaxHeight(Region.USE_PREF_SIZE);
        control.setMaxWidth(Region.USE_PREF_SIZE);
        control.itemsProperty().addListener(this::refresh);
        refresh(control.itemsProperty());
    }

    private void refresh(Observable observable) {
        ColorLegend control = getSkinnable();
        List<HBox> itemContainers = new ArrayList<>();

        for (ColorLegendItem item : control.itemsProperty()) {
            Rectangle rect = new Rectangle();
            rect.setWidth(ScaleHelper.emsToPixels(1));
            rect.setHeight(ScaleHelper.emsToPixels(1));
            rect.fillProperty().bind(item.colorProperty());

            Label label = new Label();
            label.textProperty().bind(item.textProperty());
            label.textFillProperty().bind(control.textFillProperty());
            label.fontProperty().bind(control.fontProperty());
            label.setMinWidth(Region.USE_PREF_SIZE);

            HBox hbox = new HBox(rect, label);
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setSpacing(ScaleHelper.emsToPixels(0.5));
            hbox.setMaxWidth(Region.USE_PREF_SIZE);
            itemContainers.add(hbox);
        }

        Label captionLabel = new Label();
        captionLabel.textProperty().bind(control.captionProperty());
        captionLabel.textFillProperty().bind(control.textFillProperty());
        captionLabel.fontProperty().bind(control.fontProperty());

        root.setMaxHeight(Region.USE_PREF_SIZE);
        root.setMaxWidth(Region.USE_PREF_SIZE);
        root.getChildren().clear();
        root.getChildren().add(captionLabel);
        root.getChildren().addAll(itemContainers.toArray(new HBox[0]));
    }

}
