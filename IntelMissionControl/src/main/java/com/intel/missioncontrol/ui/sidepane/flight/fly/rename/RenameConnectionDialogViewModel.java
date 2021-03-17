/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.rename;

import com.google.inject.Inject;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;

public class RenameConnectionDialogViewModel extends DialogViewModel<RenameConnectionDialogResult, Void> {

    private final Command confirmCommand;
    private final UIAsyncObjectProperty<FlightPlan> activeFlightPlan = new UIAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);

    @InjectScope
    private FlightScope flightScope;

    @Inject
    public RenameConnectionDialogViewModel() {
        //        ObservableRuleBasedValidator waypointValidator = new ObservableRuleBasedValidator();
        //        waypointValidator.addRule(selectedWaypointItem.isNotNull(), ValidationMessage.error("UAV must be
        // selected"));
        //        formValidator = new CompositeValidator();
        //        formValidator.addValidators(waypointValidator);

        confirmCommand =
            new DelegateCommand(
                () -> {
                    setDialogResult(new RenameConnectionDialogResult("Cheese"));
                    getCloseCommand().execute();
                });
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        drone.bind(flightScope.currentDroneProperty());
    }

    Command getConfirmCommand() {
        return confirmCommand;
    }
}
