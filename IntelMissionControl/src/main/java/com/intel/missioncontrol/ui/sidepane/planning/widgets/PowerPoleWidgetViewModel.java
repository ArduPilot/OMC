/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.inject.Inject;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.planning.EditPowerpolePointsViewModel;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class PowerPoleWidgetViewModel extends ViewModelBase {

    private final BooleanProperty canShowEditPowerPoleDialogCommand = new SimpleBooleanProperty(true);
    private final IntegerProperty numberOfPoints = new SimpleIntegerProperty(0);
    private final IntegerProperty numberOfActivePoints = new SimpleIntegerProperty(0);
    private final IDialogService dialogService;
    public AreaOfInterest areaOfInterest;
    private DelegateCommand showEditPowerPoleDialogCommand;

    @Inject
    public PowerPoleWidgetViewModel(IDialogService dialogService) {
        this.dialogService = dialogService;

        showEditPowerPoleDialogCommand =
            new DelegateCommand(
                () -> {
                    canShowEditPowerPoleDialogCommand.set(false);
                    dialogService
                        .requestDialogAsync(this, EditPowerpolePointsViewModel.class, () -> areaOfInterest, false)
                        .whenDone(f -> canShowEditPowerPoleDialogCommand.set(true), Platform::runLater);
                },
                canShowEditPowerPoleDialogCommand);
    }

    public DelegateCommand getShowEditPowerPoleDialogCommand() {
        return showEditPowerPoleDialogCommand;
    }

    public int getNumberOfPoints() {
        return numberOfPoints.get();
    }

    public IntegerProperty numberOfPointsProperty() {
        return numberOfPoints;
    }

    public int getNumberOfActivePoints() {
        return numberOfActivePoints.get();
    }

    public IntegerProperty numberOfActivePointsProperty() {
        return numberOfActivePoints;
    }
}
