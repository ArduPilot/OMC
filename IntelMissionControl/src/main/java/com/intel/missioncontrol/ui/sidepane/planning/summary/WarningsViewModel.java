/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.summary;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.sidepane.planning.EditWaypointsViewModel;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/** ViewModel for Warnings panel if flight is too big. */
public class WarningsViewModel extends ViewModelBase {

    private final Command showEditWayointsDialogCommand;
    private final BooleanProperty canShowEditWayointsDialogCommand = new SimpleBooleanProperty(true);
    private final IApplicationContext applicationContext;
    private final IQuantityStyleProvider quantityStyleProvider;

    @Inject
    public WarningsViewModel(
            IApplicationContext applicationContext, ISettingsManager settingsManager, IDialogService dialogService) {
        this.applicationContext = applicationContext;
        this.quantityStyleProvider = settingsManager.getSection(GeneralSettings.class);
        showEditWayointsDialogCommand =
            new DelegateCommand(
                () -> {
                    canShowEditWayointsDialogCommand.set(false);
                    Futures.addCallback(
                        dialogService.requestDialogAsync(this, EditWaypointsViewModel.class, false),
                        new FutureCallback<>() {
                            @Override
                            public void onSuccess(EditWaypointsViewModel editWaypointsViewModel) {
                                canShowEditWayointsDialogCommand.set(true);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                canShowEditWayointsDialogCommand.set(true);
                            }

                        });
                },
                canShowEditWayointsDialogCommand);
        ;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();
    }

    public Command getShowEditWayointsDialogCommand() {
        return showEditWayointsDialogCommand;
    }
}
