/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.project.Dataset;
import com.intel.missioncontrol.project.FlightPlan;
import com.intel.missioncontrol.project.Project;
import com.intel.missioncontrol.ui.notifications.Toast;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncObjectProperty;
import org.asyncfx.concurrent.Future;
import org.checkerframework.checker.nullness.qual.Nullable;

public class IApplicationContextTestDummy implements IApplicationContext {
    @Override
    public boolean checkDroneConnected(boolean execute) {
        return false;
    }

    @Override
    public void addToast(Toast toast) {}

    @Override
    public void addCloseRequestListener(ICloseRequestListener listener) {}

    @Override
    public void removeCloseRequestListener(ICloseRequestListener listener) {}

    @Override
    public void addClosingListener(IClosingListener listener) {}

    @Override
    public void removeClosingListener(IClosingListener listener) {}

    @Override
    public ReadOnlyObjectProperty<Mission> currentLegacyMissionProperty() {
        return null;
    }

    @Override
    public @Nullable Mission getCurrentLegacyMission() {
        return null;
    }

    @Override
    public boolean unloadCurrentMission() {
        return true;
    }

    @Override
    public boolean askUserForMissionSave() {
        return true;
    }

    @Override
    public boolean renameCurrentMission() {
        return true;
    }

    @Override
    public BooleanExpression currentMissionIsNoDemo() {
        return null;
    }

    @Override
    public Future<Void> ensureMissionAsync() {
        return null;
    }

    @Override
    public Future<Void> loadNewMissionAsync() {
        return null;
    }

    @Override
    public Future<Void> loadMissionAsync(Mission mission) {
        return null;
    }

    @Override
    public Future<Void> loadClonedMissionAsync(Mission mission) {
        return null;
    }

    @Override
    public ReadOnlyListProperty<Toast> toastsProperty() {
        return null;
    }

    @Override
    public ReadOnlyAsyncObjectProperty<Project> currentProjectProperty() {
        return null;
    }

    @Override
    public Project getCurrentProject() {
        return null;
    }

    @Override
    public void revertProjectChange() {}

    @Override
    public AsyncObjectProperty<com.intel.missioncontrol.project.Mission> currentMissionProperty() {
        return null;
    }

    @Override
    public com.intel.missioncontrol.project.@Nullable Mission getCurrentMission() {
        return null;
    }

    @Override
    public AsyncObjectProperty<FlightPlan> currentFlightPlanProperty() {
        return null;
    }

    @Override
    public @Nullable FlightPlan getCurrentFlightPlan() {
        return null;
    }

    @Override
    public AsyncObjectProperty<Dataset> currentDatasetProperty() {
        return null;
    }

    @Override
    public @Nullable Dataset getCurrentDataset() {
        return null;
    }

}
