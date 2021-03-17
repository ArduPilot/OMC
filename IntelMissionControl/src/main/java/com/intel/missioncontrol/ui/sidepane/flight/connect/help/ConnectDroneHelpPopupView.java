/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.connect.help;

import com.intel.missioncontrol.ui.sidepane.FancyTabView;
import de.saxsys.mvvmfx.InjectViewModel;
import de.saxsys.mvvmfx.ViewModel;
import javafx.fxml.FXML;

public class ConnectDroneHelpPopupView extends FancyTabView<ConnectDroneHelpPopupViewModel> {

    @InjectViewModel
    private ConnectDroneHelpPopupViewModel viewModel;

    @FXML
    public void closePopup() {
        viewModel.getCloseCommand().execute();
    }

    @Override
    protected ViewModel getViewModel() {
        return viewModel;
    }

}
