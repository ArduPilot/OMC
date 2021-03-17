/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.map;

import com.intel.missioncontrol.map.ViewMode;
import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ToolPaletteView extends ViewBase<ToolPaletteViewModel> {

    @InjectViewModel
    private ToolPaletteViewModel viewModel;

    @FXML
    private Pane layoutRoot;

    @FXML
    private ToggleButton rulerButton;

    @FXML
    private RadioButton map2DButton;

    @FXML
    private RadioButton map3DButton;

    @FXML
    private Button locateMeButton;

    @FXML
    private Button zoomInButton;

    @FXML
    private Button zoomOutButton;

    @FXML
    private VBox viewModeToggleGroupBox;

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
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        rulerButton.selectedProperty().bindBidirectional(viewModel.rulerModeEnabledProperty());

        map2DButton.selectedProperty().bindBidirectional(viewModel.flatMapEnabledProperty());
        map3DButton.setSelected(!map2DButton.isSelected());

        locateMeButton.disableProperty().bind(viewModel.getLocateMeCommand().notExecutableProperty());
        locateMeButton.setOnAction(event -> viewModel.getLocateMeCommand().execute());

        zoomInButton.disableProperty().bind(viewModel.getZoomInCommand().notExecutableProperty());
        zoomInButton.setOnAction(event -> viewModel.getZoomInCommand().execute());

        zoomOutButton.disableProperty().bind(viewModel.getZoomOutCommand().notExecutableProperty());
        zoomOutButton.setOnAction(event -> viewModel.getZoomOutCommand().execute());

        viewModeToggleGroupBox.visibleProperty().bind(viewModel.viewModesVisibleProperty());

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
