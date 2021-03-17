/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.checks.automatic;

import com.google.inject.Inject;
import com.intel.missioncontrol.drone.validation.IFlightValidationService;
import com.intel.missioncontrol.drone.validation.IFlightValidator;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import de.saxsys.mvvmfx.InjectScope;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.collections.ListChangeListener;
import org.asyncfx.beans.property.UIAsyncListProperty;
import org.asyncfx.collections.FXAsyncCollections;
import org.asyncfx.collections.LockedList;
import org.asyncfx.concurrent.Dispatcher;

public class AutomaticChecksDialogViewModel extends DialogViewModel {
    @InjectScope
    private FlightScope flightScope;

    private final UIAsyncListProperty<AutoCheckItemViewModel> autoCheckItems = new UIAsyncListProperty<>(this);

    private final IFlightValidationService flightValidationService;

    @Inject
    public AutomaticChecksDialogViewModel(IFlightValidationService flightValidationService) {
        this.flightValidationService = flightValidationService;

        autoCheckItems.set(FXAsyncCollections.observableArrayList());
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        flightValidationService.validatorsProperty().addListener(this::setAutoCheckItems, Dispatcher.platform()::run);
        setAutoCheckItems(null);
    }

    private void setAutoCheckItems(ListChangeListener.Change<? extends IFlightValidator> change) {
        try (LockedList<IFlightValidator> validators = flightValidationService.validatorsProperty().lock()) {
            this.autoCheckItems.setAll(
                validators.stream().map(AutoCheckItemViewModel::fromValidator).collect(Collectors.toList()));
        }
    }

    ReadOnlyListProperty<AutoCheckItemViewModel> autoCheckItemsProperty() {
        return autoCheckItems.getReadOnlyProperty();
    }
}
