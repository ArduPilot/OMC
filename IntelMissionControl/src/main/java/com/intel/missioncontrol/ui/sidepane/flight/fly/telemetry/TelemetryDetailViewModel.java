/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.linkbox.ILinkBoxConnectionService;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.FutureCommand;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import org.asyncfx.beans.property.PropertyPathStore;

public class TelemetryDetailViewModel extends DialogViewModel {

    private final PropertyPathStore propertyPathStore = new PropertyPathStore();
    private final ChangeListener<Object> missionPropertyListener =
        (observableValue, oldValue, newValue) -> getCloseCommand().execute();
    private final BooleanProperty linkBoxConnected = new SimpleBooleanProperty(false);
    private final FutureCommand showRTKConfigurationDialogCommand;
    private final IApplicationContext applicationContext;

    @InjectScope
    private MainScope mainScope;

    @Inject
    TelemetryDetailViewModel(
            IApplicationContext applicationContext,
            ILinkBoxConnectionService linkBoxConnectionService,
            IDialogService dialogService) {
        this.applicationContext = applicationContext;
        linkBoxConnected.bind(
            Bindings.createBooleanBinding(
                () ->
                    linkBoxConnectionService.linkBoxStatusProperty().get()
                        != ILinkBoxConnectionService.LinkBoxStatus.OFFLINE,
                linkBoxConnectionService.linkBoxStatusProperty()));

        showRTKConfigurationDialogCommand =
            new FutureCommand(() -> dialogService.requestDialogAsync(this, RTKConfigurationViewModel.class, false));
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();
        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .selectReadOnlyObject(Mission::currentFlightPlanProperty)
            .addListener(observable -> getCloseCommand().execute());
        applicationContext.currentMissionProperty().addListener(new WeakChangeListener<>(missionPropertyListener));
    }

    public MainScope getMainScope() {
        return mainScope;
    }

    ReadOnlyBooleanProperty linkBoxOnlineProperty() {
        return linkBoxConnected;
    }

    Command getShowRTKConfigurationDialogCommand() {
        return showRTKConfigurationDialogCommand;
    }
}
