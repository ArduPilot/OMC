/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.start;

import com.intel.missioncontrol.ui.ViewBase;
import de.saxsys.mvvmfx.InjectViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

/** Introduction view */
public class IntroductionView extends ViewBase<IntroductionViewModel> {

    @InjectViewModel
    private IntroductionViewModel viewModel;

    @FXML
    private Pane localLayoutRoot;

    @FXML
    private Button openDemoMissionButton;

    //@FXML
    //private Button readManualButton;

    @FXML
    private Button viewVideoTourButton;

    @FXML
    public void onHideViewAction() {
        viewModel.hidePanel();
    }

    @Override
    public void initializeView() {
        super.initializeView();
        BooleanProperty visible = viewModel.visibleProperty();
        visible.bindBidirectional(viewModel.introductionEnabledProperty());
        localLayoutRoot.visibleProperty().bind(visible);
        localLayoutRoot.managedProperty().bind(visible);

        //readManualButton.disableProperty().bind(viewModel.getShowUserManualCommand().notExecutableProperty());
        openDemoMissionButton.disableProperty().bind(viewModel.getDemoMissionCommand().notExecutableProperty());
        viewVideoTourButton.disableProperty().bind(viewModel.getShowQuickStartCommand().notExecutableProperty());
    }

    @Override
    protected Parent getRootNode() {
        return localLayoutRoot;
    }

    @Override
    public IntroductionViewModel getViewModel() {
        return viewModel;
    }

    @FXML
    public void onCreateDemoMission() {
        viewModel.getDemoMissionCommand().execute();
    }

    /*@FXML
    private void onShowReadManual() {
        viewModel.getShowUserManualCommand().execute();
    }*/

    @FXML
    private void onShowVideoTour() {
        viewModel.getShowQuickStartCommand().execute();
    }

}
