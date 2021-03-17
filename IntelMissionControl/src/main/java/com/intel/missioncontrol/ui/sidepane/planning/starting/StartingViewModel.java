/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.starting;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.drone.IDrone;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.InputMode;
import com.intel.missioncontrol.measure.Dimension;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.VariantQuantity;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.SimpleQuantityProperty;
import com.intel.missioncontrol.measure.property.VariantQuantityProperty;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.mission.SrsPosition;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.SrsSettings;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.sidepane.flight.FlightScope;
import de.saxsys.mvvmfx.InjectScope;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.flightplan.AltitudeAdjustModes;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.Dispatcher;

/** View model used to integrate "Start" mission settings section with FlightPlan domain object. */
public class StartingViewModel extends ViewModelBase {

    private final BooleanProperty autoEnabled = new SimpleBooleanProperty(true);
    private final BooleanProperty showTakeoffElevation = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty takeoffButtonPressed = new SimpleBooleanProperty();
    private final BooleanProperty recalculateOnEveryChange;
    private final ReadOnlyObjectProperty<AltitudeAdjustModes> currentAltMode;

    private final PropertyPathStore propertyPathStore = new PropertyPathStore();
    private final IApplicationContext applicationContext;
    private final Command toggleChooseTakeOffPositionCommand;
    private final Command takeOffPositionFromUavCommand;
    private final ISelectionManager selectionManager;
    private final ChangeListener<MSpatialReference> spatialReferenceChangeListener;
    private SrsPosition takeoffPositionWrapper;
    private ChangeListener<InputMode> mouseModesChangeListener =
        ((observable, oldValue, newValue) -> {
            if (newValue.equals(InputMode.SET_TAKEOFF_POINT)) {
                takeoffButtonPressed.setValue(true);
            } else {
                takeoffButtonPressed.setValue(false);
            }
        });

    private final IMapController mapController;

    @InjectScope
    private FlightScope flightScope;

    private final AsyncObjectProperty<Position> dronePosition = new SimpleAsyncObjectProperty<>(this);

    @Inject
    public StartingViewModel(
            IApplicationContext applicationContext,
            ISettingsManager settingsManager,
            IMapController mapController,
            ISelectionManager selectionManager) {
        this.applicationContext = applicationContext;
        this.mapController = mapController;
        this.selectionManager = selectionManager;
        GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

        recalculateOnEveryChange =
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectBoolean(FlightPlan::recalculateOnEveryChangeProperty);

        takeoffPositionWrapper = new SrsPosition(generalSettings);
        SrsSettings srsSettings = settingsManager.getSection(SrsSettings.class);
        takeoffPositionWrapper.setSrs(srsSettings.getApplicationSrs());

        spatialReferenceChangeListener = (observable, oldValue, newValue) -> takeoffPositionWrapper.setSrs(newValue);
        srsSettings.applicationSrsProperty().addListener(new WeakChangeListener<>(spatialReferenceChangeListener));

        toggleChooseTakeOffPositionCommand = new DelegateCommand(this::toggleChooseTakeOffPosition);
        takeOffPositionFromUavCommand = new DelegateCommand(this::takeOffPositionFromUav, dronePosition.isNotNull());

        currentAltMode =
            PropertyPath.from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectReadOnlyObject(FlightPlan::currentAltModeProperty);
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        dronePosition.bind(
            PropertyPath.from(flightScope.currentDroneProperty()).selectReadOnlyAsyncObject(IDrone::positionProperty));

        mapController
            .mouseModeProperty()
            .addListener(new WeakChangeListener<>(mouseModesChangeListener), Dispatcher.platform()::run);

        Mission m = applicationContext.currentMissionProperty().get();
        if (m != null && m.getCurrentFlightPlan() != null) {
            takeoffPositionWrapper
                .positionProperty()
                .bindBidirectional(
                    propertyPathStore
                        .from(applicationContext.currentMissionProperty())
                        .select(Mission::currentFlightPlanProperty)
                        .selectObject(FlightPlan::takeoffPositionProperty));

            autoEnabled.bindBidirectional(
                propertyPathStore
                    .from(applicationContext.currentMissionProperty())
                    .select(Mission::currentFlightPlanProperty)
                    .selectBoolean(FlightPlan::takeoffAutoProperty));
        }

        takeoffPositionWrapper
            .positionProperty()
            .addListener(
                (obj, oldV, newV) -> {
                    if (newV == null) {
                        return;
                    }
                });

    }

    public BooleanProperty recalculateOnEveryChangeProperty() {
        return recalculateOnEveryChange;
    }

    public BooleanProperty showTakeoffElevationProperty() {
        return showTakeoffElevation;
    }

    public BooleanProperty autoEnabledProperty() {
        return autoEnabled;
    }

    public VariantQuantityProperty takeOffLatitudeProperty() {
        return takeoffPositionWrapper.latitudeQuantity();
    }

    public SimpleBooleanProperty takeoffButtonPressedProperty() {
        return takeoffButtonPressed;
    }
    
    public VariantQuantityProperty takeOffLongitudeProperty() {
        return takeoffPositionWrapper.longitudeQuantity();
    }

    public Command getToggleChooseTakeOffPositionCommand() {
        return toggleChooseTakeOffPositionCommand;
    }

    public Command getTakeOffPositionFromUavCommand() {
        return takeOffPositionFromUavCommand;
    }

    private void toggleChooseTakeOffPosition() {
        if (mapController.getMouseMode() != InputMode.SET_TAKEOFF_POINT) {
            selectionManager.setSelection(
                applicationContext.getCurrentMission().getCurrentFlightPlan().getLegacyFlightplan().getTakeoff());
            mapController.setMouseMode(InputMode.SET_TAKEOFF_POINT);
        } else {
            mapController.tryCancelMouseModes(InputMode.SET_TAKEOFF_POINT);
        }
    }

    private void takeOffPositionFromUav() {
        Mission currentMission = applicationContext.getCurrentMission();
        if (currentMission == null) {
            return;
        }

        LatLon latLonNew = dronePosition.getValueUncritical();
        if (latLonNew == null) {
            return;
        }

        FlightPlan fp = currentMission.currentFlightPlanProperty().get();
        if (fp == null) {
            return;
        }

        Position oldPos = fp.takeoffPositionProperty().get();
        Position newPos = new Position(latLonNew, oldPos == null ? 0 : oldPos.elevation);
        fp.takeoffPositionProperty().set(newPos);
    }

    public ReadOnlyObjectProperty<AltitudeAdjustModes> currentAltModeProperty() {
        return currentAltMode;
    }
}
