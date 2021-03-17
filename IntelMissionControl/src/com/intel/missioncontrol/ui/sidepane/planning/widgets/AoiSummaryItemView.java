/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AoiSummaryItemView extends VBox implements FxmlView<AoiSummaryItemViewModel>, Initializable {

    @FXML
    private Label labelKey;

    @FXML
    private Label labelValue;

    @InjectViewModel
    private AoiSummaryItemViewModel viewModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labelKey.textProperty().bind(viewModel.keyProperty());
        labelValue.textProperty().bind(viewModel.valueProperty());

        labelKey.setMinWidth(Region.USE_PREF_SIZE);
        labelValue.setMinWidth(Region.USE_PREF_SIZE);
    }

}
