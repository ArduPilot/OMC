/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.settings;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.geometry.AreaOfInterest;
import com.intel.missioncontrol.geometry.AreaOfInterestCorner;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Dimension.Speed;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.QuantityBindings;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.measure.property.VariantQuantityProperty;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.ReferencePointType;
import com.intel.missioncontrol.mission.SrsPosition;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.SrsSettings;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.SettingsPage;
import com.intel.missioncontrol.ui.scope.planning.PlanningScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.flightplan.AltitudeAdjustModes;
import eu.mavinci.core.flightplan.FlightplanSpeedModes;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import gov.nasa.worldwind.geom.Position;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.concurrent.Dispatcher;

public class GeneralSettingsSectionViewModel extends ViewModelBase {

    public static final int MIN_SIZE = 2;
    private final List<AltitudeAdjustModes> availableAltitudeAdjustModes =
        Arrays.asList(AltitudeAdjustModes.CONSTANT_OVER_R, AltitudeAdjustModes.FOLLOW_TERRAIN);
    private final ChangeListener<MSpatialReference> spatialReferenceChangeListener;

    @InjectScope
    private PlanningScope planningScope;

    private final ObjectProperty<AltitudeAdjustModes> selectedTerrainMode = new SimpleObjectProperty<>();
    private final BooleanProperty maxSpeedUIVisible = new SimpleBooleanProperty();
    private final ObjectProperty<FlightplanSpeedModes> selectedMaxGroundSpeedAutomatic = new SimpleObjectProperty<>();
    private final BooleanProperty stopAtWaypoints = new SimpleBooleanProperty();
    private final BooleanProperty maxSpeedSpinnerEnabled = new SimpleBooleanProperty();
    private final BooleanProperty recalculateOnEveryChange = new SimpleBooleanProperty();
    private final QuantityProperty<Speed> maxGroundSpeedUpperLimit;
    private final QuantityProperty<Speed> maxGroundSpeed;
    private final QuantityProperty<Dimension.Percentage> gsdToleranceQuantity;
    private final DoubleProperty gsdTolerance = new SimpleDoubleProperty();
    private SrsPosition refPointPositionWrapper;
    private final QuantityProperty<Dimension.Length> refPointElevation;
    private BooleanProperty manualRefPoint = new SimpleBooleanProperty(false);
    private final ListProperty<Object> refPointOptionsList =
        new SimpleListProperty<>(FXCollections.observableArrayList());
    private ObjectProperty<Object> selectedRefPointOption = new SimpleObjectProperty<>();
    private BooleanProperty chooseRefPointButtonPressed = new SimpleBooleanProperty(false);
    private ObjectProperty<ReferencePointType> selectedRefPointType = new SimpleObjectProperty<>();
    private IntegerProperty selectedRefPointOptionIndex = new SimpleIntegerProperty();
    private BooleanProperty refPointCoordinatesEditable = new SimpleBooleanProperty();
    private BooleanProperty enableJumpOverWaypoints = new SimpleBooleanProperty();

    private final IApplicationContext applicationContext;
    private final INavigationService navigationService;
    private final Command toggleChooseRefPointCommand;
    private final ISelectionManager selectionManager;

    private ChangeListener<ObservableList<AreaOfInterest>> aoiChangeListener =
        (list, oldVal, newVal) -> {
            refillReferencePointOptionsList();
        };

    private ChangeListener<? super Boolean> refPointSetManuallyListener =
        (list, oldVal, newVal) -> {
            // set the last option "Manual" in the dropdown if ref point change manually
            if (!newVal && refPointOptionsList.contains(ReferencePointType.MANUAL)) {
                selectedRefPointOption.set(ReferencePointType.MANUAL);
            }
        };

    private ChangeListener<InputMode> mouseModesChangeListener =
        ((observable, oldValue, newValue) -> {
            if (newValue.equals(InputMode.SET_REF_POINT)) {
                chooseRefPointButtonPressed.setValue(true);
            } else {
                chooseRefPointButtonPressed.setValue(false);
            }
        });

    public BooleanProperty enableJumpOverWaypointsProperty() {
        return enableJumpOverWaypoints;
    }

    private class PositionChangeListener implements ChangeListener<Position> {
        @Override
        public void changed(ObservableValue<? extends Position> observable, Position oldValue, Position newValue) {
            planningScope.currentFlightplanProperty().get().refPointPositionProperty().set(newValue);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof PositionChangeListener;
        }
    }

    private final PositionChangeListener positionChangeListener = new PositionChangeListener();
    private final ChangeListener<Position> weakPositionChangeListener =
        new WeakChangeListener<>(positionChangeListener);
    private final PropertyPathStore propertyPathStore = new PropertyPathStore();
    private final IMapModel mapModel;
    private final IMapController mapController;

    @Inject
    public GeneralSettingsSectionViewModel(
            IApplicationContext applicationContext,
            INavigationService navigationService,
            ISettingsManager settingsManager,
            ISelectionManager selectionManager,
            IMapModel mapModel,
            IMapController mapController) {
        this.applicationContext = applicationContext;
        this.navigationService = navigationService;
        this.mapModel = mapModel;
        this.mapController = mapController;
        this.selectionManager = selectionManager;
        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);
        refPointPositionWrapper = new SrsPosition(generalSettings);
        SrsSettings srsSettings = settingsManager.getSection(SrsSettings.class);
        refPointPositionWrapper.setSrs(srsSettings.getApplicationSrs());

        spatialReferenceChangeListener = (observable, oldValue, newValue) -> refPointPositionWrapper.setSrs(newValue);
        srsSettings.applicationSrsProperty().addListener(new WeakChangeListener<>(spatialReferenceChangeListener));
        maxGroundSpeedUpperLimit = new SimpleQuantityProperty<>(generalSettings, UnitInfo.INVARIANT_SPEED_MPS);
        maxGroundSpeed = new SimpleQuantityProperty<>(generalSettings, UnitInfo.INVARIANT_SPEED_MPS);
        gsdToleranceQuantity =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.PERCENTAGE, Quantity.of(0.0, Unit.PERCENTAGE));
        refPointElevation =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
        QuantityBindings.bindBidirectional(gsdToleranceQuantity, gsdTolerance, Unit.FACTOR);
        toggleChooseRefPointCommand = new DelegateCommand(this::toggleChooseRefPosition);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        selectedTerrainMode.bindBidirectional(
            propertyPathStore
                .from(planningScope.currentFlightplanProperty())
                .selectObject(FlightPlan::currentAltModeProperty));

        mapController
            .mouseModeProperty()
            .addListener(new WeakChangeListener<>(mouseModesChangeListener), Dispatcher.platform());

        maxSpeedUIVisible.bind(
            Bindings.createBooleanBinding(
                () ->
                    !planningScope
                        .selectedHardwareConfigurationProperty()
                        .get()
                        .getPlatformDescription()
                        .isInFixedWingEditionMode(),
                planningScope.selectedHardwareConfigurationProperty()));

        maxGroundSpeedUpperLimit.bind(
            Bindings.createObjectBinding(
                () -> planningScope.getSelectedHardwareConfiguration().getPlatformDescription().getMaxPlaneSpeed(),
                planningScope.selectedHardwareConfigurationProperty()));

        stopAtWaypoints.bindBidirectional(
            propertyPathStore
                .from(planningScope.currentFlightplanProperty())
                .selectBoolean(FlightPlan::stopAtWaypointsProperty));
        selectedMaxGroundSpeedAutomatic.bindBidirectional(
            propertyPathStore
                .from(planningScope.currentFlightplanProperty())
                .selectObject(FlightPlan::maxGroundSpeedAutomaticProperty));

        maxGroundSpeed.bindBidirectional(
            propertyPathStore
                .from(planningScope.currentFlightplanProperty())
                .selectObject(FlightPlan::maxGroundSpeedProperty));

        recalculateOnEveryChange.bindBidirectional(
            propertyPathStore
                .from(planningScope.currentFlightplanProperty())
                .selectBoolean(FlightPlan::recalculateOnEveryChangeProperty));
        gsdTolerance.bindBidirectional(
            propertyPathStore
                .from(planningScope.currentFlightplanProperty())
                .selectDouble(FlightPlan::gsdToleranceProperty));

        maxSpeedSpinnerEnabled.bind(selectedMaxGroundSpeedAutomatic.isEqualTo(FlightplanSpeedModes.MANUAL_CONSTANT));

        enableJumpOverWaypoints.bindBidirectional(
            propertyPathStore
                .from(planningScope.currentFlightplanProperty())
                .selectBoolean(FlightPlan::enableJumpOverWaypointsProperty));
        initRefPointProperties();
    }

    public Command getToggleChooseRefPointCommand() {
        return toggleChooseRefPointCommand;
    }

    private void toggleChooseRefPosition() {
        if (mapController.getMouseMode() != InputMode.SET_REF_POINT) {
            selectionManager.setSelection(
                applicationContext.getCurrentMission().getCurrentFlightPlan().getLegacyFlightplan().getRefPoint());
            mapController.setMouseMode(InputMode.SET_REF_POINT);
        } else {
            mapController.tryCancelMouseModes(InputMode.SET_REF_POINT);
        }
    }

    private void initRefPointProperties() {
        refPointPositionWrapper
            .positionProperty()
            .bindBidirectional(
                propertyPathStore
                    .from(applicationContext.currentMissionProperty())
                    .select(Mission::currentFlightPlanProperty)
                    .selectObject(FlightPlan::refPointPositionProperty));
        refPointElevation.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::refPointElevationProperty));

        refPointCoordinatesEditable.bind(selectedRefPointType.isEqualTo(ReferencePointType.MANUAL));

        initRefPointOptionsList();
    }

    private void initRefPointOptionsList() {
        selectedRefPointType.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::refPointTypeProperty));
        selectedRefPointOptionIndex.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectInteger(FlightPlan::getRefPointOptionIndexProperty));

        selectedRefPointOption.addListener(
            (obj, oldV, newV) -> {
                if (oldV != null) {
                    if (oldV instanceof AreaOfInterestCorner) {
                        ((AreaOfInterestCorner)oldV)
                            .cornerPositionProperty()
                            .removeListener(weakPositionChangeListener);
                        ((AreaOfInterestCorner)oldV).isReferencePointProperty().set(false);
                    }
                    // TAKEOFF position
                    else if (oldV instanceof ObjectProperty) {
                        ((ObjectProperty<Position>)oldV).removeListener(weakPositionChangeListener);
                    }
                }

                FlightPlan flightplan = planningScope.getCurrentFlightplan();
                if (newV != null && flightplan != null) {
                    selectedRefPointOptionIndex.setValue(refPointOptionsList.indexOf(newV));
                    if (newV.equals(ReferencePointType.MANUAL)) {
                        flightplan.refPointAutoProperty().set(false);

                        selectedRefPointType.set(ReferencePointType.MANUAL);
                    } else {
                        flightplan.refPointAutoProperty().set(true);

                        if (newV instanceof AreaOfInterestCorner) {
                            selectedRefPointType.set(ReferencePointType.VERTEX);
                            ((AreaOfInterestCorner)newV).isReferencePointProperty().set(true);
                            ObjectProperty<Position> positionObjectProperty =
                                ((AreaOfInterestCorner)newV).cornerPositionProperty();
                            positionObjectProperty.addListener(weakPositionChangeListener);
                            flightplan.refPointPositionProperty().set(positionObjectProperty.getValue());
                        }
                        // TAKEOFF position
                        else if (newV instanceof ObjectProperty) {
                            selectedRefPointType.set(ReferencePointType.TAKEOFF);
                            ((ObjectProperty<Position>)newV).addListener(weakPositionChangeListener);
                            flightplan.refPointPositionProperty().set(((ObjectProperty<Position>)newV).getValue());
                        }
                    }
                }
            });

        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .select(Mission::currentFlightPlanProperty)
            .selectReadOnlyList(FlightPlan::areasOfInterestProperty)
            .addListener(new WeakChangeListener<>(aoiChangeListener));

        propertyPathStore
            .from(applicationContext.currentMissionProperty())
            .select(Mission::currentFlightPlanProperty)
            .selectBoolean(FlightPlan::refPointAutoProperty)
            .addListener(new WeakChangeListener<>(refPointSetManuallyListener));

        refillReferencePointOptionsList();
    }

    private void refillReferencePointOptionsList() {
        FlightPlan flightPlan =
            applicationContext.getCurrentMission() == null
                ? null
                : applicationContext.getCurrentMission().currentFlightPlanProperty().get();
        ReadOnlyListProperty<AreaOfInterest> list = flightPlan == null ? null : flightPlan.areasOfInterestProperty();

        refPointOptionsList.clear();
        if (list != null) {
            for (AreaOfInterest a : list.getValue()) {
                List<AreaOfInterestCorner> corners = a.cornerListProperty().get();
                for (AreaOfInterestCorner c : corners) {
                    refPointOptionsList.add(c);
                }
            }
        }

        if (flightPlan != null) {
            refPointOptionsList.add(flightPlan.takeoffPositionProperty());
        }

        refPointOptionsList.add(ReferencePointType.MANUAL);

        setSelectedOption();
    }

    private void setSelectedOption() {
        int size = refPointOptionsList.size();
        int idx = selectedRefPointOptionIndex.get();
        if (size > MIN_SIZE) {
            if (selectedRefPointType.get().equals(ReferencePointType.MANUAL)) {
                selectedRefPointOption.set(refPointOptionsList.get(size - 1));
            } else if (selectedRefPointType.get().equals(ReferencePointType.TAKEOFF)) {
                selectedRefPointOption.set(refPointOptionsList.get(size - 2));
            } else if (selectedRefPointType.get().equals(ReferencePointType.VERTEX) && size > idx) {
                selectedRefPointOption.set(refPointOptionsList.get(idx));
            } else if (idx == 0) {
                selectedRefPointOption.set(refPointOptionsList.get(0));
            }
        }
    }

    public BooleanProperty stopAtWaypointsProperty() {
        return stopAtWaypoints;
    }

    public ObjectProperty<FlightplanSpeedModes> selectedMaxGroundSpeedAutomaticProperty() {
        return selectedMaxGroundSpeedAutomatic;
    }

    public List<AltitudeAdjustModes> getAvailableAltitudeAdjustModes() {
        return availableAltitudeAdjustModes;
    }

    public ObjectProperty<AltitudeAdjustModes> selectedTerrainModeProperty() {
        return selectedTerrainMode;
    }

    public BooleanProperty recalculateOnEveryChangeProperty() {
        return recalculateOnEveryChange;
    }

    public BooleanProperty maxSpeedUIVisibleProperty() {
        return maxSpeedUIVisible;
    }

    public BooleanProperty maxSpeedSpinnerEnabledProperty() {
        return maxSpeedSpinnerEnabled;
    }

    public QuantityProperty<Speed> maxGroundSpeedProperty() {
        return maxGroundSpeed;
    }

    public QuantityProperty<Speed> maxGroundSpeedUpperLimitProperty() {
        return maxGroundSpeedUpperLimit;
    }

    @SuppressLinter(
        value = "IllegalViewModelMethod",
        reviewer = "mstrauss",
        justification = "Legacy code: this should be a command."
    )
    public void recalculateNow() {
        planningScope.getCurrentFlightplan().doFlightplanCalculation();
    }

    public VariantQuantityProperty refPointLatitudeProperty() {
        return refPointPositionWrapper.latitudeQuantity();
    }

    public VariantQuantityProperty refPointLongitudeProperty() {
        return refPointPositionWrapper.longitudeQuantity();
    }

    public QuantityProperty<Dimension.Length> refPointElevationProperty() {
        return refPointElevation;
    }

    public BooleanProperty chooseRefPointButtonPressedProperty() {
        return chooseRefPointButtonPressed;
    }

    public BooleanProperty manualRefPointProperty() {
        return manualRefPoint;
    }

    public QuantityProperty<Dimension.Percentage> gsdToleranceQuantityProperty() {
        return gsdToleranceQuantity;
    }

    @SuppressLinter(
        value = "IllegalViewModelMethod",
        reviewer = "mstrauss",
        justification = "Legacy code: this should be a command."
    )
    public void navigateToElevationSettings() {
        navigationService.navigateTo(SettingsPage.AIRSPACES_PROVIDERS);
    }

    public ListProperty<Object> refPointOptionsListProperty() {
        return refPointOptionsList;
    }

    public ObjectProperty<Object> selectedRefPointOptionProperty() {
        return selectedRefPointOption;
    }

    public ObjectProperty<ReferencePointType> selectedRefPointTypeProperty() {
        return selectedRefPointType;
    }

    public BooleanProperty refPointCoordinatesEditableProperty() {
        return refPointCoordinatesEditable;
    }
}
