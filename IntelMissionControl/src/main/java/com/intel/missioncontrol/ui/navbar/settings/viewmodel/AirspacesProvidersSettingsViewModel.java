/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.SuppressLinter;
import com.intel.missioncontrol.airspaces.AirspaceProvider;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.measure.Dimension.Length;
import com.intel.missioncontrol.measure.Dimension.Time;
import com.intel.missioncontrol.measure.Quantity;
import com.intel.missioncontrol.measure.Unit;
import com.intel.missioncontrol.measure.UnitInfo;
import com.intel.missioncontrol.measure.property.QuantityConverters;
import com.intel.missioncontrol.measure.property.QuantityProperty;
import com.intel.missioncontrol.measure.property.UIAsyncQuantityProperty;
import com.intel.missioncontrol.measure.property.UIQuantityPropertyMetadata;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import com.intel.missioncontrol.settings.ElevationModelSettings;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import com.intel.missioncontrol.ui.navbar.layers.GeoTiffExternalSourceViewModel;
import com.intel.missioncontrol.ui.validation.QuantityRangeValidator;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import de.saxsys.mvvmfx.utils.validation.ValidationStatus;
import de.saxsys.mvvmfx.utils.validation.Validator;
import javafx.beans.property.Property;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.concurrent.Dispatcher;

public class AirspacesProvidersSettingsViewModel extends ViewModelBase {
    public static final Quantity<Length> MAX_ALTITUDE_ABOVE_GROUND_LOWER = Quantity.of(0.0, Unit.METER);
    public static final Quantity<Length> MAX_ALTITUDE_ABOVE_GROUND_UPPER = Quantity.of(1000.0, Unit.METER);
    public static final Quantity<Length> MAX_ALTITUDE_ABOVE_SEA_LEVEL_LOWER = Quantity.of(0.0, Unit.METER);
    public static final Quantity<Length> MAX_ALTITUDE_ABOVE_SEA_LEVEL_UPPER = Quantity.of(6000.0, Unit.METER);
    public static final Quantity<Time> MIN_TIME_BETWEEN_LANDING_AND_SUNSET_LOWER = Quantity.of(-30.0, Unit.MINUTE);
    public static final Quantity<Time> MIN_TIME_BETWEEN_LANDING_AND_SUNSET_UPPER = Quantity.of(120.0, Unit.MINUTE);
    public static final Quantity<Length> MIN_HORIZONTAL_DISTANCE_LOWER = Quantity.of(0.0, Unit.METER);
    public static final Quantity<Length> MIN_HORIZONTAL_DISTANCE_UPPER = Quantity.of(20000.0, Unit.METER);
    public static final Quantity<Length> MIN_VERTICAL_DISTANCE_LOWER = Quantity.of(0.0, Unit.METER);
    public static final Quantity<Length> MIN_VERTICAL_DISTANCE_UPPER = Quantity.of(2000.0, Unit.METER);

    private final UIAsyncQuantityProperty<Length> maxAltitudeAboveGround;
    private final UIAsyncQuantityProperty<Length> maxAltitudeAboveSeaLevel;
    private final UIAsyncQuantityProperty<Length> minHorizontalDistance;
    private final UIAsyncQuantityProperty<Length> minVerticalDistance;
    private final UIAsyncQuantityProperty<Time> minTimeBetweenLandingAndSunset;

    private final UIAsyncBooleanProperty useSurfaceDataForPlanning = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty useDefaultElevationModel = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty useGeoTIFF = new UIAsyncBooleanProperty(this);
    private final UIAsyncBooleanProperty useAirspaceDataForPlanning = new UIAsyncBooleanProperty(this);
    private final UIAsyncObjectProperty<AirspaceProvider> airspaceProviderProperty = new UIAsyncObjectProperty<>(this);

    private final Validator maxAltitudeAboveGroundValidator;
    private final Validator maxAltitudeAboveSeaLevelValidator;
    private final Validator minHorizontalDistanceValidator;
    private final Validator minVerticalDistanceValidator;
    private final Validator minTimeBetweenLandingAndSunsetValidator;

    private final AirspacesProvidersSettings airspacesProvidersSettings;
    private final ElevationModelSettings elevationModelSettings;

    private final AirspaceProvider currentAirspaceProvider;

    private final Command showDataSourcesCommand;

    @Inject
    public AirspacesProvidersSettingsViewModel(
            ISettingsManager settingsManager, ILanguageHelper languageHelper, IDialogService dialogService) {
        final GeneralSettings generalSettings = settingsManager.getSection(GeneralSettings.class);
        airspacesProvidersSettings = settingsManager.getSection(AirspacesProvidersSettings.class);
        elevationModelSettings = settingsManager.getSection(ElevationModelSettings.class);
        currentAirspaceProvider = airspacesProvidersSettings.getAirspaceProvider();

        maxAltitudeAboveGround =
            new UIAsyncQuantityProperty<>(
                this,
                new UIQuantityPropertyMetadata.Builder<Length>()
                    .quantityStyleProvider(generalSettings)
                    .unitInfo(UnitInfo.LOCALIZED_LENGTH_NO_KM)
                    .create());

        maxAltitudeAboveSeaLevel =
            new UIAsyncQuantityProperty<>(
                this,
                new UIQuantityPropertyMetadata.Builder<Length>()
                    .quantityStyleProvider(generalSettings)
                    .unitInfo(UnitInfo.LOCALIZED_LENGTH_NO_KM)
                    .create());

        minHorizontalDistance =
            new UIAsyncQuantityProperty<>(
                this,
                new UIQuantityPropertyMetadata.Builder<Length>()
                    .quantityStyleProvider(generalSettings)
                    .unitInfo(UnitInfo.LOCALIZED_LENGTH)
                    .create());

        minVerticalDistance =
            new UIAsyncQuantityProperty<>(
                this,
                new UIQuantityPropertyMetadata.Builder<Length>()
                    .quantityStyleProvider(generalSettings)
                    .unitInfo(UnitInfo.LOCALIZED_LENGTH)
                    .create());

        minTimeBetweenLandingAndSunset =
            new UIAsyncQuantityProperty<>(
                this,
                new UIQuantityPropertyMetadata.Builder<Time>()
                    .quantityStyleProvider(generalSettings)
                    .unitInfo(UnitInfo.TIME_MINUTES)
                    .create());

        this.maxAltitudeAboveGroundValidator =
            new QuantityRangeValidator<>(
                generalSettings,
                languageHelper,
                this.maxAltitudeAboveGround,
                MAX_ALTITUDE_ABOVE_GROUND_LOWER,
                MAX_ALTITUDE_ABOVE_GROUND_UPPER);

        this.maxAltitudeAboveSeaLevelValidator =
            new QuantityRangeValidator<>(
                generalSettings,
                languageHelper,
                this.maxAltitudeAboveSeaLevel,
                MAX_ALTITUDE_ABOVE_SEA_LEVEL_LOWER,
                MAX_ALTITUDE_ABOVE_SEA_LEVEL_UPPER);

        this.minHorizontalDistanceValidator =
            new QuantityRangeValidator<>(
                generalSettings,
                languageHelper,
                this.minHorizontalDistance,
                MIN_HORIZONTAL_DISTANCE_LOWER,
                MIN_HORIZONTAL_DISTANCE_UPPER);

        this.minVerticalDistanceValidator =
            new QuantityRangeValidator<>(
                generalSettings,
                languageHelper,
                this.minVerticalDistance,
                MIN_VERTICAL_DISTANCE_LOWER,
                MIN_VERTICAL_DISTANCE_UPPER);

        this.minTimeBetweenLandingAndSunsetValidator =
            new QuantityRangeValidator<>(
                generalSettings,
                languageHelper,
                this.minTimeBetweenLandingAndSunset,
                MIN_TIME_BETWEEN_LANDING_AND_SUNSET_LOWER,
                MIN_TIME_BETWEEN_LANDING_AND_SUNSET_UPPER);

        maxAltitudeAboveGround.bindBidirectional(
            airspacesProvidersSettings.maxAltitudeAboveGroundProperty(),
            QuantityConverters.numberToQuantity(Unit.METER));

        maxAltitudeAboveSeaLevel.bindBidirectional(
            airspacesProvidersSettings.maxAltitudeAboveSeaLevelProperty(),
            QuantityConverters.numberToQuantity(Unit.METER));

        minHorizontalDistance.bindBidirectional(
            airspacesProvidersSettings.minimumHorizontalDistanceProperty(),
            QuantityConverters.numberToQuantity(Unit.METER));

        minVerticalDistance.bindBidirectional(
            airspacesProvidersSettings.minimumVerticalDistanceProperty(),
            QuantityConverters.numberToQuantity(Unit.METER));
        minTimeBetweenLandingAndSunset.bindBidirectional(
            airspacesProvidersSettings.minimumTimeLandingProperty(), QuantityConverters.numberToQuantity(Unit.MINUTE));

        useSurfaceDataForPlanning.bindBidirectional(elevationModelSettings.useSurfaceDataForPlanningProperty());
        useDefaultElevationModel.bindBidirectional(elevationModelSettings.useDefaultElevationModelProperty());
        useGeoTIFF.bindBidirectional(elevationModelSettings.useGeoTIFFProperty());
        useAirspaceDataForPlanning.bindBidirectional(airspacesProvidersSettings.useAirspaceDataForPlanningProperty());
        airspaceProviderProperty.bindBidirectional(airspacesProvidersSettings.airspaceProviderProperty());

        // these to listeners provide that one checkbox always stays selected (as Alexey wanted)
        useDefaultElevationModel.addListener(
            (obj, oldVal, newVal) -> {
                if (!useGeoTIFF.get() && !newVal) {
                    Dispatcher.platform().run(() -> useDefaultElevationModel.set(true));
                }
            });
        useGeoTIFF.addListener(
            (obj, oldVal, newVal) -> {
                if (!useDefaultElevationModel.get() && !newVal) {
                    Dispatcher.platform().run(() -> useGeoTIFF.set(true));
                }
            });

        showDataSourcesCommand =
            new DelegateCommand(
                () -> {
                    dialogService.requestDialogAsync(
                        AirspacesProvidersSettingsViewModel.this, GeoTiffExternalSourceViewModel.class, false);
                });
    }

    public boolean isUseSurfaceDataForPlanning() {
        return useSurfaceDataForPlanning.get();
    }

    public Property<Boolean> useSurfaceDataForPlanningProperty() {
        return useSurfaceDataForPlanning;
    }

    public boolean getUseDefaultElevationModel() {
        return useDefaultElevationModel.get();
    }

    public Property<Boolean> useDefaultElevationModelProperty() {
        return useDefaultElevationModel;
    }

    public boolean isUseGeoTIFF() {
        return useGeoTIFF.get();
    }

    public Property<Boolean> useGeoTIFFProperty() {
        return useGeoTIFF;
    }

    public boolean isUseAirspaceDataForPlanning() {
        return useAirspaceDataForPlanning.get();
    }

    public Property<Boolean> useAirspaceDataForPlanningProperty() {
        return useAirspaceDataForPlanning;
    }

    public Command getShowDataSourcesCommand() {
        return showDataSourcesCommand;
    }

    public QuantityProperty<Length> maxAltitudeAboveGroundProperty() {
        return this.maxAltitudeAboveGround;
    }

    public QuantityProperty<Length> maxAltitudeAboveSeaLevelProperty() {
        return this.maxAltitudeAboveSeaLevel;
    }

    public QuantityProperty<Time> minTimeBetweenLandingAndSunsetProperty() {
        return this.minTimeBetweenLandingAndSunset;
    }

    public QuantityProperty<Length> minHorizontalDistanceProperty() {
        return this.minHorizontalDistance;
    }

    public QuantityProperty<Length> minVerticalDistanceProperty() {
        return this.minVerticalDistance;
    }

    public ValidationStatus getMaxAltitudeAboveGroundValidationStatus() {
        return this.maxAltitudeAboveGroundValidator.getValidationStatus();
    }

    public ValidationStatus getMaxAltitudeAboveSeaLevelValidationStatus() {
        return this.maxAltitudeAboveSeaLevelValidator.getValidationStatus();
    }

    public ValidationStatus getMinTimeBetweenLandingAndSunsetValidationStatus() {
        return this.minTimeBetweenLandingAndSunsetValidator.getValidationStatus();
    }

    public ValidationStatus getMinHorizontalDistanceValidationStatus() {
        return this.minHorizontalDistanceValidator.getValidationStatus();
    }

    public ValidationStatus getMinVerticalDistanceValidationStatus() {
        return this.minVerticalDistanceValidator.getValidationStatus();
    }

    @SuppressLinter(
        value = "IllegalViewModelMethod",
        reviewer = "mstrauss",
        justification = "Legacy code: this should be a command."
    )
    public void scheduleAirspaceProviderToChange(String chosenProviderAsString) {
        AirspaceProvider chosenProvider = AirspaceProvider.valueOf(chosenProviderAsString);
        airspacesProvidersSettings.airspaceProviderProperty().setValue(chosenProvider);
    }

    public AirspaceProvider getCurrentAirspaceProvider() {
        return currentAirspaceProvider;
    }

    public AirspaceProvider getScheduledForChangeAirspaceProvider() {
        return airspacesProvidersSettings.getAirspaceProvider();
    }

    public Property<AirspaceProvider> airspaceProviderProperty() {
        return airspaceProviderProperty;
    }

}
