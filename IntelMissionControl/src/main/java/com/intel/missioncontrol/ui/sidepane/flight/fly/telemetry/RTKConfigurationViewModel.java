/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.telemetry;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.MainScope;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import org.asyncfx.beans.property.PropertyPathStore;

public class RTKConfigurationViewModel extends DialogViewModel {

    private final PropertyPathStore propertyPathStore = new PropertyPathStore();
    private final IApplicationContext applicationContext;
    private final ChangeListener<Object> missionPropertyListener =
        (observableValue, oldValue, newValue) -> getCloseCommand().execute();

    @InjectScope
    private MainScope mainScope;

    @Inject
    RTKConfigurationViewModel(IApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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
}
