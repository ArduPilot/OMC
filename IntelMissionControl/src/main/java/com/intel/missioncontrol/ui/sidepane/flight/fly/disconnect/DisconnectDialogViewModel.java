/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.disconnect;

import com.google.inject.Inject;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncStringProperty;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.drone.connection.IDroneConnectionService;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import javafx.beans.property.Property;

public class DisconnectDialogViewModel extends DialogViewModel<DisconnectDialogResult, Void> {

    private final Command confirmCommand;
    private final AsyncObjectProperty<IDrone> currentDrone = new SimpleAsyncObjectProperty<>(this);
    private final UIAsyncStringProperty currentConnectionName = new UIAsyncStringProperty(this);
    private final IDroneConnectionService droneConnectionService;

    @InjectScope
    private FlightScope flightScope;

    @Inject
    public DisconnectDialogViewModel(IDroneConnectionService droneConnectionService) {
        this.droneConnectionService = droneConnectionService;

        confirmCommand =
            new DelegateCommand(
                () -> {
                    setDialogResult(new DisconnectDialogResult(true));
                    getCloseCommand().execute();
                });
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();
        currentDrone.set(flightScope.currentDroneProperty().getValue());
        IDrone currentDrone = flightScope.currentDroneProperty().get();
        currentConnectionName.set(droneConnectionService.getConnectionItemForDrone(currentDrone).getName());
    }

    Command getConfirmCommand() {
        return confirmCommand;
    }

    public Property<String> currentConnectionNameProperty() {
        return currentConnectionName;
    }
}
