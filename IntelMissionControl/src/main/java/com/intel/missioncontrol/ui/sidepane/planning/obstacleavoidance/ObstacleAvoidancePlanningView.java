/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.obstacleavoidance;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.controls.ToggleSwitch;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.layout.VBox;

public class ObstacleAvoidancePlanningView extends ViewBase<ObstacleAvoidancePlanningViewModel> {

    @InjectViewModel
    ObstacleAvoidancePlanningViewModel viewModel;

    @FXML
    private Control rootNode;

    @FXML
    private VBox obstacleAvoidanceRoot;

    @FXML
    private ToggleSwitch obstacleAvoidanceSwitch;

    @FXML
    private ToggleSwitch addSafetyWaypointSwtich;

    @Override
    protected Parent getRootNode() {
        return rootNode;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void initializeView() {
        super.initializeView();

        // Enabling/Disabling based on whether the selected hardware is having OA capability.
        obstacleAvoidanceRoot.visibleProperty().bindBidirectional(viewModel.hardwareOACapableProperty());
        obstacleAvoidanceRoot.managedProperty().bindBidirectional(viewModel.hardwareOACapableProperty());

        obstacleAvoidanceSwitch.selectedProperty().bindBidirectional(viewModel.enableObstacleAvoidanceProperty());
        addSafetyWaypointSwtich.selectedProperty().bindBidirectional(viewModel.enableAddSafetyWaypointsProperty());
    }
}
