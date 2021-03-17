/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import org.asyncfx.beans.property.PropertyPathStore;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

public class TelemetryDetailViewModel extends DialogViewModel {

    @InjectScope
    private MainScope mainScope;

    @InjectScope
    private PlanningScope planningScope;

    @Inject
    private IApplicationContext applicationContext;

    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();
        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .selectReadOnlyObject(Mission::currentFlightPlanProperty)
            .addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        getCloseCommand().execute();
                    }
                });
        applicationContext
            .currentMissionProperty()
            .addListener(
                new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        getCloseCommand().execute();
                    }
                });
    }

    public MainScope getMainScope() {
        return mainScope;
    }

    private IPlatformDescription getPlatformDescription() {
        return planningScope.getSelectedHardwareConfiguration().getPlatformDescription();
    }

    /*public Drone getUav() {
        if (mainScope == null) {
            return null;
        }

        Mission currentMission = applicationContext.getCurrentMission();
        if (currentMission == null) {
            return null;
        }

        return currentMission.droneProperty().get();
    }*/
}
