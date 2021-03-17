/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.sidepane.planning.landing;

import com.google.inject.Inject;
import com.intel.missioncontrol.IApplicationContext;
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
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.core.flightplan.LandingModes;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import org.asyncfx.beans.property.PropertyPathStore;
import org.asyncfx.concurrent.Dispatcher;

/** View model used to integrate "Landing" flight plan settings section with FlightPlan domain object. */
public class LandingViewModel extends ViewModelBase {

    private final SimpleBooleanProperty landingButtonPressed = new SimpleBooleanProperty();

    private final ObjectProperty<LandingModes> selectedLandingMode = new SimpleObjectProperty<>();

    private final PropertyPathStore propertyPathStore = new PropertyPathStore();
    private final IApplicationContext applicationContext;
    private final Command toggleChooseLandingPositionCommand;
    private final ISelectionManager selectionManager;
    private final ChangeListener<MSpatialReference> spatialReferenceChangeListener;
    private SrsPosition landingPositionWrapper;

    private final SimpleBooleanProperty landingLocationVisible = new SimpleBooleanProperty();
    private final SimpleBooleanProperty landingLocationEditable = new SimpleBooleanProperty();
    private final SimpleBooleanProperty showHoverAltitude = new SimpleBooleanProperty();

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

        hoverElevation =
            new SimpleQuantityProperty<>(generalSettings, UnitInfo.LOCALIZED_LENGTH, Quantity.of(0.0, Unit.METER));
    }

    @Override
    protected void initializeViewModel() {
        super.initializeViewModel();

        selectedLandingMode.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentLegacyMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::landingModeProperty));

        landingLocationVisible.bind(selectedLandingMode.isNotEqualTo(LandingModes.LAST_WAYPOINT));
        landingLocationEditable.bind(selectedLandingMode.isEqualTo(LandingModes.CUSTOM_LOCATION));

        hoverElevation.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentLegacyMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectObject(FlightPlan::landingHoverElevationProperty));

        autolanding.bindBidirectional(
            propertyPathStore
                .from(applicationContext.currentLegacyMissionProperty())
                .select(Mission::currentFlightPlanProperty)
                .selectBoolean(FlightPlan::landAutomaticallyProperty));

        showHoverAltitude.bind(autolanding.not());

        mapController
            .mouseModeProperty()
            .addListener(new WeakChangeListener<>(mouseModesChangeListener), Dispatcher.platform());

        landingPositionWrapper
            .positionProperty()
            .bindBidirectional(
                propertyPathStore
                    .from(applicationContext.currentLegacyMissionProperty())
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

    private void toggleChooseLandingPosition() {
        if (mapController.getMouseMode() != InputMode.SET_LANDING_POINT) {
            selectionManager.setSelection(
                applicationContext.getCurrentLegacyMission().getCurrentFlightPlan().getLegacyFlightplan().getLandingpoint());
            mapController.setMouseMode(InputMode.SET_LANDING_POINT);
        } else {
            mapController.tryCancelMouseModes(InputMode.SET_LANDING_POINT);
        }
    }

    public ObjectProperty<LandingModes> selectedLandingModeProperty() {
        return selectedLandingMode;
    }

    public SimpleBooleanProperty landingLocationEditableProperty() {
        return landingLocationEditable;
    }

    public SimpleBooleanProperty landingLocationVisibleProperty() {
        return landingLocationVisible;
    }

    public SimpleBooleanProperty showHoverAltitudeProperty() {
        return showHoverAltitude;
    }

    public QuantityProperty<Dimension.Length> hoverElevationProperty() {
        return hoverElevation;
    }

    public SimpleBooleanProperty autolandingProperty() {
        return autolanding;
    }
}
