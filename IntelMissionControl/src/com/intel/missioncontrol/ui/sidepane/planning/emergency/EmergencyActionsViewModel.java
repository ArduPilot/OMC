/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.emergency;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.intel.missioncontrol.beans.binding.QuantityBindings;
import com.intel.missioncontrol.beans.property.PropertyPath;
import com.intel.missioncontrol.beans.property.PropertyPathStore;
import com.intel.missioncontrol.beans.property.QuantityProperty;
import com.intel.missioncontrol.beans.property.SimpleQuantityProperty;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.hardware.IPlatformDescription;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Time;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import de.saxsys.mvvmfx.InjectScope;
import eu.mavinci.core.flightplan.CEvent;
import eu.mavinci.core.flightplan.CEventList;
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.core.plane.AirplaneEventActions;
import eu.mavinci.flightplan.EventList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

/** ViewModel for Emergency Actions fligh plan settigns panel. */
public class EmergencyActionsViewModel extends ViewModelBase {

    public static final List<AirplaneEventActions> GPS_LOSS_COPTER =
        ImmutableList.<AirplaneEventActions>of(AirplaneEventActions.positionHold, AirplaneEventActions.circleDown);
    public static final List<AirplaneEventActions> LINK_LOSS_COPTER =
        ImmutableList.<AirplaneEventActions>of(
            AirplaneEventActions.circleDown,
            AirplaneEventActions.returnToStartOnSafetyAltitude,
            AirplaneEventActions.returnToStart);

    @InjectScope
    private PlanningScope planningScope;

    private GeneralSettings settings;

    private QuantityProperty<Time> gnssLostDelayQuantity;
    private DoubleProperty gnssLostDelayProperty;

    private QuantityProperty<Time> rcAndDataLostDelayQuantity;
    private DoubleProperty rcAndDataLostDelayProperty;

    private QuantityProperty<Time> dataLostDelayQuantity;
    private DoubleProperty dataLostDelayProperty;

    private QuantityProperty<Time> rcLostDelayQuantity;
    private DoubleProperty rcLostDelayProperty;

    private BooleanProperty gnssLostRecoverable;
    private BooleanProperty rcAndDataLostRecoverable;
    private BooleanProperty dataLostRecoverable;
    private BooleanProperty rcLostRecoverable;
    private BooleanProperty gnssLostCopterRecoverable;
    private BooleanProperty rcAndDataLostCopterRecoverable;
    private final BooleanProperty autoComputeSafetyHeight = new SimpleBooleanProperty();
    private final ObjectProperty<LandingModes> landingMode = new SimpleObjectProperty<>();

    private QuantityProperty<Length> safetyAltitudeQuantity;
    private BooleanProperty autoSafetyAltitude;
    private final DoubleProperty safetyAltitudeMeterProperty = new SimpleDoubleProperty();
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();

    private ChangeListener<FlightPlan> flightPlanChangeListener =
        new ChangeListener<>() {
            @Override
            public void changed(
                    ObservableValue<? extends FlightPlan> observable, FlightPlan oldValue, FlightPlan newValue) {
                planningScope.currentFlightplanProperty().removeListener(flightPlanChangeListener);
                QuantityBindings.unbindBidirectional(safetyAltitudeQuantity, safetyAltitudeMeterProperty);
                QuantityBindings.unbindBidirectional(gnssLostDelayQuantity, gnssLostDelayProperty);
                QuantityBindings.unbindBidirectional(rcAndDataLostDelayQuantity, rcAndDataLostDelayProperty);
                QuantityBindings.unbindBidirectional(dataLostDelayQuantity, dataLostDelayProperty);
                QuantityBindings.unbindBidirectional(rcLostDelayQuantity, rcLostDelayProperty);
            }
        };

    @Inject
    public EmergencyActionsViewModel(ISettingsManager settingsManager) {
        this.settings = settingsManager.getSection(GeneralSettings.class);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        landingMode.bind(
            PropertyPath.from(planningScope.currentFlightplanProperty())
                .selectReadOnlyObject(FlightPlan::landingModeProperty));
        planningScope.currentFlightplanProperty().addListener(new WeakChangeListener<>(flightPlanChangeListener));
        gnssLostDelayProperty = initEventDelayProperty(CEventList.NAME_GPSLOSS);
        rcAndDataLostDelayProperty = initEventDelayProperty(CEventList.NAME_RCDATALOSS);
        dataLostDelayProperty = initEventDelayProperty(CEventList.NAME_DATALOSS);
        rcLostDelayProperty = initEventDelayProperty(CEventList.NAME_RCLOSS);

        gnssLostRecoverable = initEventRecoverableProperty(CEventList.NAME_GPSLOSS);
        rcAndDataLostRecoverable = initEventRecoverableProperty(CEventList.NAME_RCDATALOSS);
        dataLostRecoverable = initEventRecoverableProperty(CEventList.NAME_DATALOSS);
        rcLostRecoverable = initEventRecoverableProperty(CEventList.NAME_RCLOSS);
        gnssLostCopterRecoverable = initEventRecoverableProperty(CEventList.NAME_GPSLOSS);
        rcAndDataLostCopterRecoverable = initEventRecoverableProperty(CEventList.NAME_RCDATALOSS);

        safetyAltitudeMeterProperty.bindBidirectional(
            propertyPathStore
                .from(planningScope.currentFlightplanProperty())
                .selectDouble(FlightPlan::safetyHeightProperty));
        autoComputeSafetyHeight.bindBidirectional(
            propertyPathStore
                .from(planningScope.currentFlightplanProperty())
                .selectBoolean(FlightPlan::autoComputeSafetyHeightProperty));
        autoSafetyAltitude = initAutoSafetyAltitudeProperty();

        safetyAltitudeQuantity =
            new SimpleQuantityProperty<>(
                settings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(safetyAltitudeMeterProperty.getValue(), Unit.METER));

        gnssLostDelayQuantity =
            new SimpleQuantityProperty<>(
                settings, UnitInfo.TIME_SECONDS, Quantity.of(gnssLostDelayProperty.getValue(), Unit.SECOND));

        rcAndDataLostDelayQuantity =
            new SimpleQuantityProperty<>(
                settings, UnitInfo.TIME_SECONDS, Quantity.of(rcAndDataLostDelayProperty.getValue(), Unit.SECOND));

        dataLostDelayQuantity =
            new SimpleQuantityProperty<>(
                settings, UnitInfo.TIME_SECONDS, Quantity.of(dataLostDelayProperty.getValue(), Unit.SECOND));

        rcLostDelayQuantity =
            new SimpleQuantityProperty<>(
                settings, UnitInfo.TIME_SECONDS, Quantity.of(rcLostDelayProperty.getValue(), Unit.SECOND));

        QuantityBindings.bindBidirectional(safetyAltitudeQuantity, safetyAltitudeMeterProperty, Unit.METER);

        QuantityBindings.bindBidirectional(gnssLostDelayQuantity, gnssLostDelayProperty, Unit.SECOND);
        QuantityBindings.bindBidirectional(rcAndDataLostDelayQuantity, rcAndDataLostDelayProperty, Unit.SECOND);
        QuantityBindings.bindBidirectional(dataLostDelayQuantity, dataLostDelayProperty, Unit.SECOND);
        QuantityBindings.bindBidirectional(rcLostDelayQuantity, rcLostDelayProperty, Unit.SECOND);
    }

    public List<AirplaneEventActions> getActiveActions(String eventName) {
        CEvent event = getFlightPlanEvent(eventName);
        if (event == null) {
            return new LinkedList<>();
        }

        return event.getActiveActions();
    }

    public List<AirplaneEventActions> getPossibleActions(String eventName) {
        CEvent event = getFlightPlanEvent(eventName);
        if (event == null) {
            return new LinkedList<>();
        }

        return Arrays.asList(event.getPossibleActions());
    }

    public AirplaneEventActions getAction(String eventName) {
        CEvent event = getFlightPlanEvent(eventName);
        if (event == null) {
            return null;
        }

        return event.getAction();
    }

    public BooleanBinding isAdvancedOperationLevelBinding() {
        return this.settings
            .operationLevelProperty()
            .isEqualTo(OperationLevel.TECHNICIAN)
            .or(this.settings.operationLevelProperty().isEqualTo(OperationLevel.DEBUG));
    }

    private SimpleDoubleProperty initSafetyAltitudeMeterProperty() {
        EventList eventList = getEventList();
        if (eventList == null) {
            return new SimpleDoubleProperty();
        }

        SimpleDoubleProperty property = new SimpleDoubleProperty(eventList.getAltWithinCM() / 100.);
        property.addListener((observable, old, newAlt) -> eventList.setAltWithinCM(newAlt.intValue() * 100));
        return property;
    }

    public BooleanProperty initAutoSafetyAltitudeProperty() {
        return autoComputeSafetyHeight;
    }

    /** Helper method to bind Event type property to fligth plan event and set initial value. */
    public SimpleObjectProperty<AirplaneEventActions> initEventActionProperty(String eventName) {
        SimpleObjectProperty<AirplaneEventActions> property = new SimpleObjectProperty<>();
        CEvent event = getFlightPlanEvent(eventName);
        if (event != null) {
            property.set(event.getAction());
            property.addListener((observable, oldValue, newValue) -> event.setAction(newValue));
        }

        return property;
    }

    private DoubleProperty initEventDelayProperty(String eventName) {
        DoubleProperty property = new SimpleDoubleProperty();
        CEvent event = getFlightPlanEvent(eventName);
        if (event != null) {
            property.setValue(event.getDelay());
            property.addListener((observable, oldValue, newValue) -> event.setDelay(newValue.intValue()));
        }

        return property;
    }

    /** Helper method to bind "Recoverable" property to fligth plan event and set initial value. */
    private SimpleBooleanProperty initEventRecoverableProperty(String eventName) {
        SimpleBooleanProperty property = new SimpleBooleanProperty();
        CEvent event = getFlightPlanEvent(eventName);
        if (event != null) {
            property.set(event.isRecover());
            property.addListener((observable, oldValue, newValue) -> event.setRecover(newValue));
        }

        return property;
    }

    private CEvent getFlightPlanEvent(String eventName) {
        EventList eventList = getEventList();
        if (eventList == null) {
            return null;
        }

        return eventList.getEventByName(eventName);
    }

    private EventList getEventList() {
        FlightPlan fp = planningScope.getCurrentFlightplan();
        if (fp == null) {
            return null;
        }

        return fp.getLegacyFlightplan().getEventList();
    }

    public BooleanBinding isSelectedUavFixedWing() {
        return Bindings.createBooleanBinding(
            () ->
                Optional.ofNullable(
                        planningScope.selectedHardwareConfigurationProperty().get().getPlatformDescription())
                    .map(IPlatformDescription::isInFixedWingEditionMode)
                    .orElse(true),
            planningScope.selectedHardwareConfigurationProperty());
    }

    public List<AirplaneEventActions> getGpsLostCopterActions() {
        return GPS_LOSS_COPTER;
    }

    public List<AirplaneEventActions> getLinkLostCopterActions() {
        return LINK_LOSS_COPTER;
    }

    public ReadOnlyObjectProperty<LandingModes> autoLandingModeProperty() {
        return landingMode;
    }

    public QuantityProperty<Length> safetyAltitudeQuantityProperty() {
        return safetyAltitudeQuantity;
    }

    public QuantityProperty<Time> gnssLostDelayQuantityProperty() {
        return gnssLostDelayQuantity;
    }

    public QuantityProperty<Time> rcAndDataLostDelayQuantityProperty() {
        return rcAndDataLostDelayQuantity;
    }

    public QuantityProperty<Time> dataLostDelayQuantityProperty() {
        return dataLostDelayQuantity;
    }

    public QuantityProperty<Time> rcLostDelayQuantityProperty() {
        return rcLostDelayQuantity;
    }

    public ReadOnlyObjectProperty<FlightPlan> currentFlightPlanProperty() {
        return planningScope.currentFlightplanProperty();
    }

    public BooleanProperty gnssLostCopterRecoverableProperty() {
        return gnssLostCopterRecoverable;
    }

    public BooleanProperty rcAndDataLostCopterRecoverableProperty() {
        return rcAndDataLostCopterRecoverable;
    }

    public BooleanProperty autoSafetyAltitudeProperty() {
        return autoSafetyAltitude;
    }

    public BooleanProperty rcLostRecoverableProperty() {
        return rcLostRecoverable;
    }

    public BooleanProperty dataLostRecoverableProperty() {
        return dataLostRecoverable;
    }

    public BooleanProperty rcAndDataLostRecoverableProperty() {
        return rcAndDataLostRecoverable;
    }

    public BooleanProperty gnssLostRecoverableProperty() {
        return gnssLostRecoverable;
    }
}
