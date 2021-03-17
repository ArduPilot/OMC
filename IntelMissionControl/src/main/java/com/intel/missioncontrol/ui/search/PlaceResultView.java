/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.search;

import com.intel.missioncontrol.helper.ScaleHelper;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;

public class PlaceResultView extends ToggleButton implements JavaView<PlaceResultViewModel> {

    @InjectViewModel
    private PlaceResultViewModel viewModel;

    public void initialize() {
        getStyleClass().add("search-result-button");
        HBox container = new HBox();
        container.setSpacing(ScaleHelper.emsToPixels(0.5));
        container.setAlignment(Pos.CENTER_LEFT);
        Label detailLabel = new Label(viewModel.getDetail());
        detailLabel.getStyleClass().add("assistive-label");
        container.getChildren().add(new Label(viewModel.getName()));
        container.getChildren().add(detailLabel);
        setGraphic(container);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

}
