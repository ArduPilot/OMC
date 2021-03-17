/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.view;

import com.google.inject.Inject;
import com.intel.missioncontrol.StereoMode;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import com.intel.missioncontrol.ui.navbar.settings.viewmodel.DisplaySettingsViewModel;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class DisplaySettingsView extends ViewBase<DisplaySettingsViewModel> {

    @InjectViewModel
    private DisplaySettingsViewModel viewModel;

    @FXML
    private Pane rootNode;

    @Inject
    private ILanguageHelper languageHelper;

    @FXML
    private Label restartHintLabel;

    @FXML
    private Label newsSwitchLbl;

    @FXML
    private ComboBox<StereoMode> stereoModeBox;

    @FXML
    private ToggleSwitch realisticLightSwitch;

    @FXML
    private ToggleSwitch showPreviewImagesSwitch;

    @FXML
    private ToggleSwitch addAreaOfInterestSwitch;

    @FXML
    private ToggleSwitch introductionSwitch;

    @FXML
    private ToggleSwitch srsCheckSwitch;

    @FXML
    private ToggleSwitch datatransferSwitch;

    @FXML
    private ToggleSwitch newsSwitch;


    @Override
    public void initializeView() {
        super.initializeView();

        introductionSwitch.selectedProperty().bindBidirectional(viewModel.introductionEnabledProperty());
        addAreaOfInterestSwitch
            .selectedProperty()
            .bindBidirectional(viewModel.addAreaOfInterestWorkflowHintEnabledProperty());
        datatransferSwitch.selectedProperty().bindBidirectional(viewModel.dataTransferWorkflowHintEnabledProperty());
        srsCheckSwitch.selectedProperty().bindBidirectional(viewModel.srsCheckEnabledProperty());
        newsSwitch.selectedProperty().bindBidirectional(viewModel.showNewsProperty());

        newsSwitch.visibleProperty().bind(viewModel.showNewsVisibleProperty());
        newsSwitch.managedProperty().bind(viewModel.showNewsVisibleProperty());
        newsSwitchLbl.visibleProperty().bind(viewModel.showNewsVisibleProperty());
        newsSwitchLbl.managedProperty().bind(viewModel.showNewsVisibleProperty());
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public DisplaySettingsViewModel getViewModel() {
        return viewModel;
    }

}
