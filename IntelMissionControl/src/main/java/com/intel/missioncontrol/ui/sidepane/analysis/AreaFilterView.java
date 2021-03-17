/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.JavaView;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class AreaFilterView extends HBox implements FxmlView<AreaFilterViewModel> {

    @InjectViewModel
    private AreaFilterViewModel viewModel;

    @FXML
    private Label label;

    @FXML
    private CheckBox checkBox;

    public void initialize() {
        checkBox.selectedProperty().bindBidirectional(viewModel.enabledProperty());
        label.textProperty().bind(viewModel.nameProperty());
    }

    @FXML
    void deleteClicked() {
        viewModel.delete();
    }

}
