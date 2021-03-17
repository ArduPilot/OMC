/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol;

import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.notifications.Toast;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
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
    public ReadOnlyObjectProperty<Mission> currentMissionProperty() {
        return null;
    }

    @Override
    public @Nullable Mission getCurrentMission() {
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

}
