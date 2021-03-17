/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.flight.fly.start;

import com.google.inject.Inject;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.beans.property.UIAsyncIntegerProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.beans.property.UIPropertyMetadata;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.WayPoint;
import com.intel.missioncontrol.ui.common.hardware.PlatformItem;
import com.intel.missioncontrol.ui.dialogs.DialogViewModel;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.validation.CompositeValidator;
import de.saxsys.mvvmfx.utils.validation.ObservableRuleBasedValidator;
import de.saxsys.mvvmfx.utils.validation.ValidationMessage;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

public class StartPlanDialogViewModel extends DialogViewModel<StartPlanDialogResult, Void> {

    private final Command confirmCommand;
    private final ObjectProperty<PlatformItem> selectedWaypointItem = new SimpleObjectProperty<>();
    private final ListProperty<PlatformItem> allWaypointItems =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private final UIAsyncObjectProperty<FlightPlan> activeFlightPlan = new UIAsyncObjectProperty<>(this);
    private final UIAsyncObjectProperty<StartPlanType> startPlanType =
        new UIAsyncObjectProperty<>(this, new UIPropertyMetadata.Builder<StartPlanType>().initialValue(null).create());
    private final UIAsyncIntegerProperty activeFlightPlanWaypointCount = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty selectedFlightPlanWaypointCount = new UIAsyncIntegerProperty(this);
    private final UIAsyncIntegerProperty activeNextWaypointIndex = new UIAsyncIntegerProperty(this);
    private final AsyncObjectProperty<IDrone> drone = new SimpleAsyncObjectProperty<>(this);
    private final UIAsyncIntegerProperty startingWaypoint = new UIAsyncIntegerProperty(this);
    private final UIAsyncObjectProperty<FlightPlan> selectedFlightPlan = new UIAsyncObjectProperty<>(this);
    private CompositeValidator formValidator;
    private AsyncObjectProperty<WayPoint> wp = new SimpleAsyncObjectProperty<>(this);

    @InjectScope
    private FlightScope flightScope;

    private ISelectionManager selectionManager;

    @Inject
    public StartPlanDialogViewModel(ISelectionManager selectionManager) {
        ObservableRuleBasedValidator waypointValidator = new ObservableRuleBasedValidator();
        waypointValidator.addRule(selectedWaypointItem.isNotNull(), ValidationMessage.error("Drone must be selected"));
        formValidator = new CompositeValidator();
        formValidator.addValidators(waypointValidator);
        this.selectionManager = selectionManager;

        confirmCommand =
            new DelegateCommand(
                () -> {
                    setDialogResult(
                        new StartPlanDialogResult(
                            startPlanType.get(), startingWaypointProperty().getValue().intValue()));
                    getCloseCommand().execute();
                });

        activeFlightPlan.bind(PropertyPath.from(drone).selectReadOnlyAsyncObject(IDrone::activeFlightPlanProperty));

        activeNextWaypointIndex.bind(
            PropertyPath.from(drone).selectReadOnlyAsyncInteger(IDrone::activeFlightPlanWaypointIndexProperty));

        activeFlightPlanWaypointCount.bind(
            Bindings.createIntegerBinding(
                () -> activeFlightPlan.get() != null ? activeFlightPlan.get().waypointsProperty().getSize() : 0,
                PropertyPath.from(activeFlightPlan).selectReadOnlyList(FlightPlan::waypointsProperty)));

        selectedFlightPlanWaypointCount.bind(
            Bindings.createIntegerBinding(
                () -> selectedFlightPlan.get() != null ? selectedFlightPlan.get().waypointsProperty().getSize() : 0,
                PropertyPath.from(selectedFlightPlan).selectReadOnlyList(FlightPlan::waypointsProperty)));
    }

    ReadOnlyListProperty<PlatformItem> allWaypointItemsProperty() {
        return allWaypointItems;
    }

    ObjectProperty<PlatformItem> selectedWaypointItemProperty() {
        return selectedWaypointItem;
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        drone.bind(flightScope.currentDroneProperty());

        selectedFlightPlan.bind(flightScope.selectedFlightPlanProperty());

        startingWaypoint.set(activeNextWaypointIndex.getValue() + 1);

        flightScope
            .nextWayPointIndexProperty()
            .bind(Bindings.createIntegerBinding(() -> startingWaypoint.get() - 1, startingWaypoint));
        startingWaypoint.addListener(
            (observable, oldValue, newValue) -> {
                if (wp.get() != null) {
                    selectionManager.getHighlighted().remove(wp.get());
                }

                if (activeFlightPlan.get() != null) {
                    wp.set(activeFlightPlan.get().waypointsProperty().get(newValue.intValue() - 1));
                } else {
                    wp.set(selectedFlightPlan.get().waypointsProperty().get(newValue.intValue() - 1));
                }

                selectionManager.getHighlighted().add(wp.get());
                startPlanType.set(StartPlanType.START_PLAN_FROM_WAYPOINT);
            });

        if (startingWaypoint.get() - 1 > 0) {
            wp.set(selectedFlightPlan.get().waypointsProperty().get(startingWaypoint.get() - 1));
            // selectionManager.getHighlighted().clear();
            selectionManager.getHighlighted().add(wp.get());
        }

        // pre-select run plan options
        if (activeFlightplanProperty().getValue() == null
                || !activeFlightplanProperty().getValue().equals(selectedFlightplanProperty().getValue())) {
            startPlanType.set(StartPlanType.START_PLAN_FROM_BEGINNING);
        } else {
            startPlanType.set(StartPlanType.RESUME_PLAN);
        }
    }

    @Override
    protected void onClosing() {
        selectionManager.getHighlighted().clear();
    }

    Property<StartPlanType> startPlanTypeProperty() {
        return startPlanType;
    }

    ReadOnlyProperty<FlightPlan> activeFlightplanProperty() {
        return activeFlightPlan;
    }

    Property<Number> activeFlightPlanWaypointCountProperty() {
        return activeFlightPlanWaypointCount;
    }

    Property<Number> selectedFlightPlanWaypointCountProperty() {
        return selectedFlightPlanWaypointCount;
    }

    Property<Number> activeNextWaypointIndexProperty() {
        return activeNextWaypointIndex;
    }

    ReadOnlyProperty<FlightPlan> selectedFlightplanProperty() {
        return selectedFlightPlan;
    }

    Command getConfirmCommand() {
        return confirmCommand;
    }

    Property<Number> startingWaypointProperty() {
        return startingWaypoint;
    }

}
