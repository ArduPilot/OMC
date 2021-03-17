/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedCommand;
import de.saxsys.mvvmfx.utils.commands.ParameterizedDelegateCommand;
import eu.mavinci.core.flightplan.PlanType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class ChooseAoiViewModel extends ViewModelBase {

    @InjectScope
    private PlanningScope planningScope;

    private final BooleanProperty initializePage = new SimpleBooleanProperty(false);
    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final Command editPlanSettingsCommand;
    private final ParameterizedCommand<PlanType> chooseAoiCommand;
    private final IMapModel mapModel;

    @Inject
    public ChooseAoiViewModel(
            IApplicationContext applicationContext, INavigationService navigationService, IMapModel mapModel) {
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.mapModel = mapModel;
        editPlanSettingsCommand = new DelegateCommand(() -> navigationService.navigateTo(SidePanePage.EDIT_FLIGHTPLAN));
        chooseAoiCommand = new ParameterizedDelegateCommand<>(this::chooseAreaOfInterest);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        navigationService
            .sidePanePageProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    initializePage.setValue(newValue == SidePanePage.CHOOSE_AOI);
                });
    }

    public IPlatformDescription getPlatformDescription() {
        return planningScope.getCurrentFlightplan() != null
            ? planningScope
                .getCurrentFlightplan()
                .getLegacyFlightplan()
                .getHardwareConfiguration()
                .getPlatformDescription()
            : null;
    }

    public ParameterizedCommand<PlanType> getChooseAoiCommand() {
        return chooseAoiCommand;
    }

    public ReadOnlyObjectProperty<Mission> currentMissionProperty() {
        return applicationContext.currentMissionProperty();
    }

    public Command getEditPlanSettingsCommand() {
        return editPlanSettingsCommand;
    }

    public BooleanProperty initializePageProperty() {
        return initializePage;
    }

    private void chooseAreaOfInterest(PlanType aoiId) {
        planningScope.generateDefaultName(aoiId);
        navigationService.navigateTo(SidePanePage.EDIT_FLIGHTPLAN);
        mapModel.addAreaOfInterest(applicationContext.getCurrentMission(), aoiId);
    }

}
