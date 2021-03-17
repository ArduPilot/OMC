/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.preflightchecks;

import com.google.inject.Inject;
import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;

public class ManualControlsDialogViewModel extends DialogViewModel {

    @Inject
    public ManualControlsDialogViewModel() {}

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();
    }

    private final ICommand upCommand =
        new DelegateCommand(
            () -> {
                System.out.println("upCommand called");
                // TODO: send Up manual command to backend
            });
    private final ICommand lTurnCommand =
        new DelegateCommand(
            () -> {
                System.out.println("lTurnCommand called");
                // TODO: send Left turn manual command to backend
            });
    private final ICommand forwardCommand =
        new DelegateCommand(
            () -> {
                System.out.println("forwardCommand called");
                // TODO: send Forward manual command to backend
            });
    private final ICommand rTurnCommand =
        new DelegateCommand(
            () -> {
                System.out.println("rTurnCommand called");
                // TODO: send Right turn manual command to backend
            });
    private final ICommand downCommand =
        new DelegateCommand(
            () -> {
                System.out.println("downCommand called");
                // TODO: send Down manual command to backend
            });
    private final ICommand leftCommand =
        new DelegateCommand(
            () -> {
                System.out.println("leftCommand called");
                // TODO: send Left manual command to backend
            });
    private final ICommand backCommand =
        new DelegateCommand(
            () -> {
                System.out.println("backCommand called");
                // TODO: send Back manual command to backend
            });
    private final ICommand rightCommand =
        new DelegateCommand(
            () -> {
                System.out.println("rightCommand called");
                // TODO: send Right manual command to backend
            });

    public ICommand getUpCommand() {
        return upCommand;
    }

    public ICommand getlTurnCommand() {
        return lTurnCommand;
    }

    public ICommand getForwardCommand() {
        return forwardCommand;
    }

    public ICommand getrTurnCommand() {
        return rTurnCommand;
    }

    public ICommand getDownCommand() {
        return downCommand;
    }

    public ICommand getLeftCommand() {
        return leftCommand;
    }

    public ICommand getBackCommand() {
        return backCommand;
    }

    public ICommand getRightCommand() {
        return rightCommand;
    }
}
