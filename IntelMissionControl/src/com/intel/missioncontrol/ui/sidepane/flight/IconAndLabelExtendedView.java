/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight;

import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class IconAndLabelExtendedView extends AlertAwareView<IconAndLabelExtendedViewModel> {

    @FXML
    private VBox iconAndLabelExtendedBox;

    @FXML
    private IconAndLabel iconAndLabelBox;

    @FXML
    private Label labelLeft;

    @FXML
    private Label labelSeparator;

    @FXML
    private Label labelRight;

    @InjectViewModel
    private IconAndLabelExtendedViewModel viewModel;

    @Override
    public void initializeView() {
        super.initializeView();

        viewModel.textProperty().bindBidirectional(iconAndLabelBox.getViewModel().textProperty());
        viewModel.imageProperty().bindBidirectional(iconAndLabelBox.getViewModel().imageProperty());

        labelLeft.textProperty().bind(viewModel.labelLeftProperty());
        labelRight.textProperty().bind(viewModel.labelRightProperty());
        labelSeparator.textProperty().bind(viewModel.separatorProperty());
    }

    @Override
    protected Parent getRootNode() {
        return iconAndLabelExtendedBox;
    }

    @Override
    protected AlertAwareViewModel getViewModel() {
        return viewModel;
    }

}
