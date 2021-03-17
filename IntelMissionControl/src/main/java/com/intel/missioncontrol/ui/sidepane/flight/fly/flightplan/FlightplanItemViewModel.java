/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan;

import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import de.saxsys.mvvmfx.ViewModel;
import de.saxsys.mvvmfx.utils.commands.AsyncCommand;
import de.saxsys.mvvmfx.utils.commands.FutureCommand;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.concurrent.Futures;

public class FlightplanItemViewModel implements ViewModel {

    @SuppressWarnings("FieldCanBeLocal")
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    private final AsyncObjectProperty<Mission> mission = new SimpleAsyncObjectProperty<>(this);
    private final ObjectProperty<FlightPlan> currentFlightPlanProperty =
        propertyPathStore.from(mission).selectObject(Mission::currentFlightPlanProperty);
    private final UIAsyncObjectProperty<FlightPlan> flightPlan = new UIAsyncObjectProperty<>(this);

    private final BooleanProperty isSelectedFlightplan = new SimpleBooleanProperty(false);
    private final BooleanProperty isActiveFlightplan = new SimpleBooleanProperty(false);
    private final StringProperty flightplanName = new SimpleStringProperty();
    private final UIAsyncObjectProperty<FlightPlan> activeFlightPlan = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<FlightPlan> selectedFlightPlan = new UIAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);
    private AsyncCommand editCommand;
    private final INavigationService navigationService;

    public FlightplanItemViewModel(
            FlightPlan flightPlan,
            IApplicationContext applicationContext,
            FlightScope flightScope,
            INavigationService navigationService) {
        this.flightPlan.set(flightPlan);
        flightplanName.set(this.flightPlan.get().getName());
        this.navigationService = navigationService;
        mission.bind(applicationContext.currentMissionProperty());
        selectedFlightPlan.bindBidirectional(currentFlightPlanProperty);

        drone.bind(flightScope.currentDroneProperty());
        activeFlightPlan.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::activeFlightPlanProperty));
        isSelectedFlightplan.bind(
            Bindings.createBooleanBinding(
                () -> {
                    if (selectedFlightPlan.get() == null || this.flightPlan.get() == null) {
                        return false;
                    }

                    if (this.flightPlan.get().equals(selectedFlightPlan.get())) {
                        return true;
                    } else {
                        return false;
                    }
                },
                selectedFlightPlan,
                this.flightPlan));

        isActiveFlightplan.bind(
            Bindings.createBooleanBinding(
                () -> {
                    if (this.flightPlan.get() == null || activeFlightPlan.get() == null) {
                        return false;
                    }

                    if (this.flightPlan.get().equals(activeFlightPlan.get())) {
                        return true;
                    } else {
                        return false;
                    }
                },
                activeFlightPlan,
                this.flightPlan));

        editCommand =
            new FutureCommand(
                () -> {
                    navigationService.navigateTo(SidePanePage.EDIT_FLIGHTPLAN);
                    return Futures.successful();
                },
                isSelectedFlightplan);
    }

    public StringProperty getFlightplanName() {
        return flightplanName;
    }

    public BooleanProperty isSelectedFlightplan() {
        return isSelectedFlightplan;
    }

    public UIAsyncObjectProperty<FlightPlan> getSelectedFlightPlan() {
        return selectedFlightPlan;
    }

    public FlightPlan getFlightPlan() {
        return flightPlan.get();
    }

    public BooleanProperty isActiveFlightplan() {
        return this.isActiveFlightplan;
    }

    public AsyncCommand getEditCommand() {
        return editCommand;
    }
}
