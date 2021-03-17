/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.connect.help;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;

public class ConnectDroneHelpPopupViewModel extends ViewModelBase {

    private final INavigationService navigationService;
    private final Command closeCommand = new DelegateCommand(this::close);

    @Inject
    public ConnectDroneHelpPopupViewModel(INavigationService navigationService) {
        this.navigationService = navigationService;
    }

    public Command getCloseCommand() {
        return closeCommand;
    }

    private void close() {
        navigationService.navigateTo(SidePanePage.CONNECT_DRONE);
    }

}
