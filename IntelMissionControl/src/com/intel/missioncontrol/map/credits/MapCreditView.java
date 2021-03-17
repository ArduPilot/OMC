/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.credits;

import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class MapCreditView extends HBox implements JavaView<MapCreditViewModel> {

    private static final double NORMAL_OPACITY = 0.7;

    @InjectViewModel
    private MapCreditViewModel viewModel;

    public void initialize() {
        setAlignment(Pos.BOTTOM_LEFT);
        setPickOnBounds(false);
        setSpacing(6);
        if (viewModel.imageProperty().get() != null) {
            ImageView imageView = new ImageView(viewModel.imageProperty().get());
            imageView.setOnMouseClicked(e -> viewModel.execute());
            imageView.setOnMouseEntered(e -> imageView.setOpacity(1));
            imageView.setOnMouseExited(e -> imageView.setOpacity(NORMAL_OPACITY));
            imageView.setFitHeight(18);
            imageView.setPreserveRatio(true);
            imageView.setPickOnBounds(true);
            imageView.setOpacity(NORMAL_OPACITY);
            imageView.setCursor(Cursor.HAND);
            getChildren().add(imageView);
        }

        if (viewModel.textProperty().get() != null) {
            Label label = new Label(viewModel.textProperty().get());
            label.setOnMouseClicked(e -> viewModel.execute());
            label.setOnMouseEntered(e -> label.setOpacity(1));
            label.setOnMouseExited(e -> label.setOpacity(NORMAL_OPACITY));
            label.setOpacity(NORMAL_OPACITY);
            label.setStyle("-fx-text-fill:white");
            label.setFocusTraversable(false);
            label.setCursor(Cursor.HAND);
            getChildren().add(label);
        }
    }

}
