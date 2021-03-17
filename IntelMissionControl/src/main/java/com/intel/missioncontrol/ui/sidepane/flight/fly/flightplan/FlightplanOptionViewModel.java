/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.worldwind.layers.aircraft.AircraftLayerVisibilitySettings;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.WayPoint;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import de.saxsys.mvvmfx.InjectScope;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleListProperty;

public class FlightplanOptionViewModel extends ViewModelBase {
    private final ISelectionManager selectionManager;

    @InjectScope
    private FlightScope flightScope;

    private final ListProperty<FlightPlan> availableFlightPlans = new SimpleListProperty<>();

    @SuppressWarnings("FieldCanBeLocal")
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    private final UIAsyncObjectProperty<FlightPlan> selectedFlightPlan = new UIAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<FlightPlan> activeFlightPlan = new UIAsyncObjectProperty<>(this);
    private final UIAsyncIntegerProperty activeNextWaypointIndex = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty activeFlightPlanWaypointCount = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty activeFlightPlanProgress = new UIAsyncIntegerProperty(this);

    @Inject
    public FlightplanOptionViewModel(
            IApplicationContext applicationContext,
            INavigationService navigationService,
            AircraftLayerVisibilitySettings aircraftLayerVisibilitySettings,
            ISelectionManager selectionManager) {
        AsyncObjectProperty<Mission> mission = new SimpleAsyncObjectProperty<>(this);
        this.selectionManager = selectionManager;
        mission.bind(applicationContext.currentLegacyMissionProperty());

        selectedFlightPlan.bindBidirectional( // TODO decouple
            propertyPathStore.from(mission).selectObject(Mission::currentFlightPlanProperty));

        // TODO: find out where layer visibility should be controlled from...
        navigationService
            .workflowStepProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == WorkflowStep.FLIGHT) {
                        aircraftLayerVisibilitySettings.flightPlanProperty().set(selectedFlightPlan.get() != null);
                    }
                });

        availableFlightPlans.bind(PropertyPath.from(mission).selectReadOnlyList(Mission::flightPlansProperty));

        activeFlightPlan.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::activeFlightPlanProperty));

        activeNextWaypointIndex.bind(
            PropertyPath.from(drone).selectReadOnlyAsyncInteger(IDrone::activeFlightPlanWaypointIndexProperty));

        activeFlightPlanWaypointCount.bind(
            Bindings.createIntegerBinding(
                () -> activeFlightPlan.get() != null ? activeFlightPlan.get().waypointsProperty().getSize() : 0,
                PropertyPath.from(activeFlightPlan).selectList(FlightPlan::waypointsProperty)));

        AsyncObjectProperty<WayPoint> wp =  new SimpleAsyncObjectProperty<>(this);
        activeFlightPlanProgress.bind(
            Bindings.createDoubleBinding(
                () -> {
                    if (activeFlightPlan.get() == null || activeFlightPlanWaypointCount.getValue() == 0) {
                        return 0.0;
                    }
                    if(wp.get() !=null){
                        selectionManager.getHighlighted().remove(wp.get());
                    }

                    if (activeNextWaypointIndex.get() <= activeFlightPlan.get().waypointsProperty().size()) {
                        wp.set(activeFlightPlan.get().waypointsProperty().get(activeNextWaypointIndex.get()));
                        selectionManager.getHighlighted().add(wp.get());
                    } else {
                        //TODO landing
                        //selectionManager.getHighlighted().clear();
                    }

                    return activeNextWaypointIndex.getValue().doubleValue()
                        / activeFlightPlanWaypointCount.getValue().doubleValue()
                        * 100;
                },
                activeFlightPlan,
                activeNextWaypointIndex,
                activeFlightPlanWaypointCount));
    }

    public void initializeViewModel() {
        super.initializeViewModel();

        drone.bind(flightScope.currentDroneProperty());

        flightScope.selectedFlightPlanProperty().bind(selectedFlightPlan);
    }

    Property<FlightPlan> selectedFlightplanProperty() {
        return selectedFlightPlan;
    }

    ReadOnlyProperty<FlightPlan> activeFlightplanProperty() {
        return activeFlightPlan;
    }

    ReadOnlyProperty<Number> activeNextWaypointIndexProperty() {
        return activeNextWaypointIndex;
    }

    ReadOnlyProperty<Number> activeFlightPlanProgressProperty() {
        return activeFlightPlanProgress;
    }

    ReadOnlyListProperty<FlightPlan> availableFlightPlansListProperty() {
        return availableFlightPlans;
    }
}
