/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.flightplan;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.worldwind.layers.aircraft.AircraftLayerVisibilitySettings;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.WayPoint;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SidePanePage;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import com.intel.missioncontrol.ui.sidepane.start.ProjectItemViewModel;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncListProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import org.asyncfx.collections.AsyncObservableList;
import org.asyncfx.collections.FXAsyncCollections;
// @ScopeProvider(scopes = FlightScope.class)
public class FlightplanOptionViewModel extends ViewModelBase {
    private final ISelectionManager selectionManager;
    private final IApplicationContext applicationContext;

    @InjectScope
    private FlightScope flightScope;

    private INavigationService navigationService;

    private final ListProperty<FlightPlan> availableFlightPlans = new SimpleListProperty<>();

    @SuppressWarnings("FieldCanBeLocal")
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    private final UIAsyncObjectProperty<FlightPlan> selectedFlightPlan = new UIAsyncObjectProperty<>(this);
    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<FlightPlan> activeFlightPlan = new UIAsyncObjectProperty<>(this);
    private final UIAsyncIntegerProperty activeNextWaypointIndex = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty activeFlightPlanWaypointCount = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty activeFlightPlanProgress = new UIAsyncIntegerProperty(this);
    private final DoubleProperty activeFlightplanPosition = new SimpleDoubleProperty();

    @Inject
    public FlightplanOptionViewModel(
            IApplicationContext applicationContext,
            INavigationService navigationService,
            AircraftLayerVisibilitySettings aircraftLayerVisibilitySettings,
            ISelectionManager selectionManager) {
        AsyncObjectProperty<Mission> mission = new SimpleAsyncObjectProperty<>(this);
        this.selectionManager = selectionManager;
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        mission.bind(applicationContext.currentMissionProperty());

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

        AsyncObjectProperty<WayPoint> wp = new SimpleAsyncObjectProperty<>(this);
        activeFlightPlanProgress.bind(
            Bindings.createDoubleBinding(
                () -> {
                    if (activeFlightPlan.get() == null || activeFlightPlanWaypointCount.getValue() == 0) {
                        return 0.0;
                    }

                    if (wp.get() != null) {
                        selectionManager.getHighlighted().remove(wp.get());
                    }

                    if (activeNextWaypointIndex.get() <= activeFlightPlan.get().waypointsProperty().size()) {
                        wp.set(activeFlightPlan.get().waypointsProperty().get(activeNextWaypointIndex.get()));
                        selectionManager.getHighlighted().add(wp.get());
                    } else {
                        // TODO landing
                        // selectionManager.getHighlighted().clear();
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

        availableFlightPlans.addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    items.clear();
                    for (FlightPlan a : newValue) {
                        FlightplanItemViewModel item =
                            new FlightplanItemViewModel(a, applicationContext, flightScope, navigationService);
                        items.add(item);
                    }
                }
            });
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
    } // Leave this here!

    ReadOnlyListProperty<FlightPlan> availableFlightPlansListProperty() {
        return availableFlightPlans;
    }

    private final UIAsyncListProperty<ViewModel> items =
        new UIAsyncListProperty<>(
            this,
            new UIPropertyMetadata.Builder<AsyncObservableList<ViewModel>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final ObjectProperty<ProjectItemViewModel> selectedItem = new SimpleObjectProperty<>();

    public ObservableValue<? extends ObservableList<ViewModel>> itemsProperty() {
        return items;
    }
}
