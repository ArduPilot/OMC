/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.analysis;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import org.asyncfx.beans.property.PropertyPath;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.menu.MenuModel;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TransferringDataViewModel extends ViewModelBase {

    @InjectScope
    private MainScope mainScope;

    private final Property<IBackgroundTaskManager.BackgroundTask> shownTask = new SimpleObjectProperty<>();
    private final IApplicationContext applicationContext;
    private final Command renameMissionCommand;

    @Inject
    public TransferringDataViewModel(IApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        renameMissionCommand = new DelegateCommand(applicationContext::renameCurrentMission);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        shownTask.bind(
            PropertyPath.from(applicationContext.currentLegacyMissionProperty())
                .select(Mission::currentMatchingProperty)
                .selectReadOnlyObject(Matching::dataTransferBackgroundTaskProperty));
    }

    public Property<IBackgroundTaskManager.BackgroundTask> shownTaskProperty() {
        return shownTask;
    }

    public ReadOnlyObjectProperty<MenuModel> datasetMenuModelProperty() {
        return mainScope.datasetMenuModelProperty();
    }

    public Command getRenameMissionCommand() {
        return renameMissionCommand;
    }

}
