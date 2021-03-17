/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.view;

import com.intel.missioncontrol.ui.ViewBase;
import com.intel.missioncontrol.ui.navbar.settings.viewmodel.SupportSettingsViewModel;
import de.saxsys.mvvmfx.FxmlView;
import de.saxsys.mvvmfx.ViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.layout.Pane;

public class SupportSettingsView extends ViewBase<SupportSettingsViewModel> {

    @FXML
    private Pane rootNode;

    @Override
    public Pane getRootNode() {
        return rootNode;
    }

    @Override
    protected ViewModel getViewModel() {
        return null;
    }

}
