/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class WindView extends VBox implements FxmlView<WindViewModel>, Initializable {

    @FXML
    private Label labelSpeed;

    @FXML
    private Label labelDirection;

    @FXML
    private IconAndLabel directionPane;

    @InjectViewModel
    private WindViewModel viewModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labelSpeed.textProperty().bind(viewModel.speedProperty());
        labelDirection.textProperty().bind(viewModel.directionProperty().asString("%.1f"));

        directionPane.getViewModel().textProperty().bind(Bindings.convert(viewModel.compassProperty()));
        directionPane.getViewModel().imageRotateProperty().bind(viewModel.directionProperty());
    }

}
