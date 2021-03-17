/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.landing;

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
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.PropertyPath;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import org.asyncfx.concurrent.Dispatcher;

/** View model used to integrate "Landing" mission settings section with FlightPlan domain object. */
public class LandingViewModel extends ViewModelBase {

    private final SimpleBooleanProperty landingButtonPressed = new SimpleBooleanProperty();

    private final ObjectProperty<LandingModes> selectedLandingMode = new SimpleObjectProperty<>();

    private final PropertyPathStore propertyPathStore = new PropertyPathStore();
    private final IApplicationContext applicationContext;
    private final Command toggleChooseLandingPositionCommand;
    private final Command landingPositionFromUavCommand;
    private final ISelectionManager selectionManager;
    private final ChangeListener<MSpatialReference> spatialReferenceChangeListener;
    private SrsPosition landingPositionWrapper;

    private final SimpleBooleanProperty landingLocationEditable = new SimpleBooleanProperty();
    private final SimpleBooleanProperty hoverAltitudeEditable = new SimpleBooleanProperty();

    private final SimpleBooleanProperty autolanding = new SimpleBooleanProperty();

    private final QuantityProperty<Dimension.Length> hoverElevation;

    private ChangeListener<InputMode> mouseModesChangeListener =
        ((observable, oldValue, newValue) -> {
            if (newValue.equals(InputMode.SET_LANDING_POINT)) {
                landingButtonPressed.setValue(true);
            } else {
                landingButtonPressed.setValue(false);
            }
        });

    private final IMapController mapController;

    @InjectScope
    private FlightScope flightScope;

    private final AsyncObjectProperty<Position> dronePosition = new SimpleAsyncObjectProperty<>(this);

    @Inject
    public LandingViewModel(
            IApplicationContext applicationContext,
            ISettingsManager settingsManager,
            IMapController mapController,
            ISelectionManager selectionManager) {
        this.applicationContext = applicationContext;
        this.mapController = mapController;
        this.selectionManager = selectionManager;
        GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);

        landingPositionWrapper = new SrsPosition(generalSettings);
        SrsSettings srsSettings = settingsManager.getSection(SrsSettings.class);
        landingPositionWrapper.setSrs(srsSettings.getApplicationSrs());

        spatialReferenceChangeListener = (observable, oldValue, newValue) -> landingPositionWrapper.setSrs(newValue);
        srsSettings.applicationSrsProperty().addListener(new WeakChangeListener<>(spatialReferenceChangeListener));

        toggleChooseLandingPositionCommand = new DelegateCommand(this::toggleChooseLandingPosition);
        landingPositionFromUavCommand = new DelegateCommand(this::landingPositionFromUav, dronePosition.isNotNull());

        hoverElevation =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        dronePosition.bind(
            PropertyPath.from(flightScope.currentDroneProperty()).selectReadOnlyAsyncObject(IDrone::positionProperty));

        selectedLandingMode.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::landingModeProperty));

        landingLocationEditable.bind(selectedLandingMode.isEqualTo(LandingModes.CUSTOM_LOCATION));

        hoverElevation.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::landingHoverElevationProperty));

        autolanding.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectBoolean(FlightPlan::landAutomaticallyProperty));

        hoverAltitudeEditable.bind(autolanding.not());

        mapController
            .mouseModeProperty()
            .addListener(new WeakChangeListener<>(mouseModesChangeListener), Dispatcher.platform()::run);

        landingPositionWrapper
            .positionProperty()
            .bindBidirectional(
                propertyPathStore
                    .from(applicationContext.currentMissionProperty())
                    .select(Mission::currentFlightPlanProperty)
                    .selectObject(FlightPlan::landingPositionProperty));
    }

    public VariantQuantityProperty landingLatitudeProperty() {
        return landingPositionWrapper.latitudeQuantity();
    }

    public SimpleBooleanProperty landingButtonPressedProperty() {
        return landingButtonPressed;
    }

    public VariantQuantity getLandingLatitude() {
        return landingLatitudeProperty().get();
    }

    public void setLandingLatitude(VariantQuantity value) {
        landingLatitudeProperty().set(value);
    }

    public VariantQuantityProperty landingLongitudeProperty() {
        return landingPositionWrapper.longitudeQuantity();
    }

    public VariantQuantity getLandingLongitude() {
        return landingLongitudeProperty().get();
    }

    public void setLandingLongitude(VariantQuantity value) {
        landingLongitudeProperty().set(value);
    }

    public Command getToggleChooseLandingPositionCommand() {
        return toggleChooseLandingPositionCommand;
    }

    public Command getLandingPositionFromUavCommand() {
        return landingPositionFromUavCommand;
    }

    private void toggleChooseLandingPosition() {
        if (mapController.getMouseMode() != InputMode.SET_LANDING_POINT) {
            selectionManager.setSelection(
                applicationContext.getCurrentMission().getCurrentFlightPlan().getLegacyFlightplan().getLandingpoint());
            mapController.setMouseMode(InputMode.SET_LANDING_POINT);
        } else {
            mapController.tryCancelMouseModes(InputMode.SET_LANDING_POINT);
        }
    }

    private void landingPositionFromUav() {
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

        Position oldPos = fp.landingPositionProperty().get();
        Position newPos = new Position(latLonNew, oldPos == null ? 0 : oldPos.elevation);
        fp.landingPositionProperty().set(newPos);
    }

    public ObjectProperty<LandingModes> selectedLandingModeProperty() {
        return selectedLandingMode;
    }

    public SimpleBooleanProperty landingLocationEditableProperty() {
        return landingLocationEditable;
    }

    public SimpleBooleanProperty hoverAltitudeEditableProperty() {
        return hoverAltitudeEditable;
    }

    public QuantityProperty<Dimension.Length> hoverElevationProperty() {
        return hoverElevation;
    }

    public SimpleBooleanProperty autolandingProperty() {
        return autolanding;
    }
}
