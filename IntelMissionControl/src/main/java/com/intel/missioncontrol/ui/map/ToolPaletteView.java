/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.map;

import com.intel.missioncontrol.map.ViewMode;
import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Pane;

public class ToolPaletteView extends ViewBase<ToolPaletteViewModel> {

    @InjectViewModel
    private ToolPaletteViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private RadioButton viewStayButton;

    @FXML
    private RadioButton viewFollowButton;

    @FXML
    private RadioButton viewCockpitButton;

    @FXML
    private RadioButton viewCameraButton;

    @Override
    protected Parent getRootNode() {
        return layoutRoot;
    }

    @Override
    public ToolPaletteViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        viewModel.viewModeProperty().addListener((observable, oldValue, newValue) -> updateViewButton(newValue));

        viewStayButton.setOnAction(event -> viewModel.viewModeProperty().setValue(ViewMode.DEFAULT));
        viewFollowButton.setOnAction(event -> viewModel.viewModeProperty().setValue(ViewMode.FOLLOW));
        viewCameraButton.setOnAction(event -> viewModel.viewModeProperty().setValue(ViewMode.PAYLOAD));
        viewCockpitButton.setOnAction(event -> viewModel.viewModeProperty().setValue(ViewMode.COCKPIT));

        updateViewButton(viewModel.viewModeProperty().getValue());
    }

    private void updateViewButton(ViewMode newValue) {
        switch (newValue) {
        case DEFAULT:
            viewStayButton.setSelected(true);
            break;
        case FOLLOW:
            viewFollowButton.setSelected(true);
            break;
        case PAYLOAD:
            viewCameraButton.setSelected(true);
            break;
        case COCKPIT:
            viewCockpitButton.setSelected(true);
            break;
        }
    }

}
