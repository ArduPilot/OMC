/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.start;

import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;

public class DemoMissionWarningViewModel extends ViewModelBase {

    @InjectScope
    private PlanningScope planningScope;

    private final Command renameDemoMissionCommand;

    DemoMissionWarningViewModel() {
        renameDemoMissionCommand = new DelegateCommand(this::renameDemoMission);
    }

    public Command getRenameDemoMissionCommand() {
        return renameDemoMissionCommand;
    }

    private void renameDemoMission() {
        planningScope.missionRenameRequestedProperty().set(true);
    }

}
