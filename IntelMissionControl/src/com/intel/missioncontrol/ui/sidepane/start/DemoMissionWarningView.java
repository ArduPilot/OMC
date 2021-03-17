/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.start;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.menu.MainMenuCommandManager;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.InjectViewModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class DemoMissionWarningView extends ViewBase<DemoMissionWarningViewModel> {

    @FXML
    private Pane rootNode;

    @FXML
    public Label missionName;

    @InjectViewModel
    private DemoMissionWarningViewModel viewModel;

    @FXML
    public void onRenameAction() {
        viewModel.renameDemoMission();
    }

    @Override
    public void initializeView() {
        super.initializeView();
        missionName.setText(String.format("%s - ", MainMenuCommandManager.DEMO_SESSION_NAME));
    }

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    public DemoMissionWarningViewModel getViewModel() {
        return viewModel;
    }

}
