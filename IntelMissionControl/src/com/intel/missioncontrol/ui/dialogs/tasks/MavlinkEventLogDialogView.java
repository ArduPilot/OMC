/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.tasks;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.RootView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class MavlinkEventLogDialogView extends RootView<MavlinkEventLogDialogViewModel> {

    @FXML
    private VBox rootlayout;

    @InjectViewModel
    private MavlinkEventLogDialogViewModel viewModel;

    @Inject
    public MavlinkEventLogDialogView() {

    }

    @Override
    protected Parent getRootNode() {
        return rootlayout;
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }
}
