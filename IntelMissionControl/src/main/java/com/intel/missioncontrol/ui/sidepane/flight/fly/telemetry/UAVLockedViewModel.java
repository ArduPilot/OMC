/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import com.google.inject.Inject;
import com.intel.missioncontrol.linkbox.ILinkBoxConnectionService;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateAsyncCommand;
import org.asyncfx.concurrent.Dispatcher;

public class UAVLockedViewModel extends DialogViewModel {
    private final Command requestAuthorizationDialogCommand;
    private final ILinkBoxConnectionService linkBoxConnectionService;

    @Inject
    UAVLockedViewModel(ILinkBoxConnectionService linkBoxConnectionService) {
        this.linkBoxConnectionService = linkBoxConnectionService;
        this.requestAuthorizationDialogCommand = new DelegateAsyncCommand(this::requestLinkBoxAuthorization);
    }

    Command getRequestAuthorizationDialogCommand() {
        return requestAuthorizationDialogCommand;
    }

    private void requestLinkBoxAuthorization() {
        linkBoxConnectionService.requestLinkBoxAuthentication();
        Dispatcher.platform().run(() -> getCloseCommand().execute());
    }

}
