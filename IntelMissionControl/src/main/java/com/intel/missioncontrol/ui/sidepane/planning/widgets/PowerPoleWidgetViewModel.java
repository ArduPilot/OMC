/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.widgets;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.planning.EditPowerpolePointsViewModel;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
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
                    Futures.addCallback(
                        dialogService.requestDialogAsync(
                            this, EditPowerpolePointsViewModel.class, () -> areaOfInterest, false),
                        new FutureCallback<>() {
                            @Override
                            public void onSuccess(EditPowerpolePointsViewModel editPowerpolePointsViewModel) {
                                canShowEditPowerPoleDialogCommand.set(true);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                canShowEditPowerPoleDialogCommand.set(true);
                            }

                        });
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
