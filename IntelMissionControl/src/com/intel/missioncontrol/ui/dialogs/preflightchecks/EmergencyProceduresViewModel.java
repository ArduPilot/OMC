/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.dialogs.preflightchecks;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.Uav;
import com.intel.missioncontrol.ui.commands.DelegateCommand;
import com.intel.missioncontrol.ui.commands.ICommand;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import eu.mavinci.core.plane.AirplaneFlightphase;

public class EmergencyProceduresViewModel extends DialogViewModel {

    private IApplicationContext applicationContext;

    @Inject
    public EmergencyProceduresViewModel(IApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();
    }

    private final ICommand landNowCommand =
        new DelegateCommand(
            () -> {
                Mission mission = applicationContext.currentMissionProperty().get();
                Uav uav = mission.uavProperty().get();
                uav.getLegacyPlane().setFlightPhase(AirplaneFlightphase.jumpToLanding);
                getCloseCommand().execute();
            });

    private final ICommand takeSafeAltitude50MCommand =
        new DelegateCommand(
            () -> { // TODO: To call backend to take safe altitude 50Meter
                getCloseCommand().execute();
            });

    private final ICommand returnToHomeCommand =
        new DelegateCommand(
            () -> {
                Mission mission = applicationContext.currentMissionProperty().get();
                Uav uav = mission.uavProperty().get();
                uav.getLegacyPlane().setFlightPhase(AirplaneFlightphase.returnhome);
                getCloseCommand().execute();
            });

    public ICommand getLandNowCommand() {
        return landNowCommand;
    }

    public ICommand getTakeSafeAltitude50MCommand() {
        return takeSafeAltitude50MCommand;
    }

    public ICommand getReturnToHomeCommand() {
        return returnToHomeCommand;
    }
}
