/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.settings.viewmodel;

import com.google.inject.Inject;
import com.intel.missioncontrol.airspaces.services.Airmap2AirspaceService;
import com.intel.missioncontrol.measure.AngleStyle;
import com.intel.missioncontrol.measure.SystemOfMeasurement;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.settings.MapRotationStyle;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.settings.SrsSettings;
import com.intel.missioncontrol.ui.Theme;
import com.intel.missioncontrol.ui.ViewModelBase;
import com.intel.missioncontrol.ui.dialogs.IDialogService;
import de.saxsys.mvvmfx.utils.commands.Command;
import de.saxsys.mvvmfx.utils.commands.DelegateCommand;
import eu.mavinci.desktop.gui.widgets.wkt.SpatialReferenceChooserViewModel;
import eu.mavinci.desktop.helper.gdal.MSpatialReference;
import gov.nasa.worldwind.WorldWind;
import java.util.Locale;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import org.asyncfx.beans.binding.Converters;
import org.asyncfx.beans.property.UIAsyncBooleanProperty;
import org.asyncfx.beans.property.UIAsyncListProperty;
import org.asyncfx.beans.property.UIAsyncObjectProperty;
import org.asyncfx.concurrent.Dispatcher;

public class GeneralSettingsViewModel extends ViewModelBase {

    private final UIAsyncListProperty<Theme> availableThemes = new UIAsyncListProperty<>(this);

    private final UIAsyncListProperty<Locale> availableLocales = new UIAsyncListProperty<>(this);

    private final UIAsyncListProperty<OperationLevel> availableOperationLevels = new UIAsyncListProperty<>(this);

    private final UIAsyncListProperty<SystemOfMeasurement> availableSystemsOfMeasurement =
        new UIAsyncListProperty<>(this);

    private final StringProperty spatialReference = new SimpleStringProperty();

    private final ChangeListener<MSpatialReference> spatialReferenceUpdateListener =
        (obj, oldVal, newVal) -> Dispatcher.platform().runLater(() -> spatialReference.set(newVal.id));

    private final ReadOnlyListProperty<AngleStyle> availableAngleStyles =
        new SimpleListProperty<>(FXCollections.observableArrayList(AngleStyle.values()));

    private final ReadOnlyListProperty<MapRotationStyle> availableMapRotationStyles =
        new SimpleListProperty<>(FXCollections.observableArrayList(MapRotationStyle.values()));

    private final UIAsyncObjectProperty<Theme> selectedTheme = new UIAsyncObjectProperty<>(this);

    private final UIAsyncObjectProperty<Locale> selectedLocale = new UIAsyncObjectProperty<>(this);

    private final UIAsyncObjectProperty<OperationLevel> selectedOperationLevel = new UIAsyncObjectProperty<>(this);

    private final UIAsyncObjectProperty<SystemOfMeasurement> selectedSystemOfMeasurement =
        new UIAsyncObjectProperty<>(this);

    private final UIAsyncObjectProperty<AngleStyle> selectedAngleStyle = new UIAsyncObjectProperty<>(this);

    private final UIAsyncObjectProperty<MapRotationStyle> selectedMapRotationStyle = new UIAsyncObjectProperty<>(this);

    private final UIAsyncBooleanProperty softwareUpdateEnabled = new UIAsyncBooleanProperty(this);

    private final UIAsyncBooleanProperty mapUpdateEnabled = new UIAsyncBooleanProperty(this);

    private final GeneralSettings generalSettings;
    private final SrsSettings srsSettings;
    private final IDialogService dialogService;

    private final Command changeSrsCommand;

    @SuppressWarnings("FieldCanBeLocal")
    private final ChangeListener<Boolean> updateMapCheckListener =
        (observable, oldValue, newValue) -> {
            //  WWFactory.setOfflineMode(!newValue); //< can't do this because its broken
            WorldWind.setOfflineMode(!newValue);
            Airmap2AirspaceService.setOfflineMode(!newValue);
        };

    @Inject
    public GeneralSettingsViewModel(ISettingsManager settingsManager, IDialogService dialogService) {
        generalSettings = settingsManager.getSection(GeneralSettings.class);
        srsSettings = settingsManager.getSection(SrsSettings.class);
        if (srsSettings.applicationSrsProperty().get() != null) {
            spatialReference.setValue(srsSettings.applicationSrsProperty().get().id);
        }

        srsSettings.applicationSrsProperty().addListener(spatialReferenceUpdateListener);

        this.dialogService = dialogService;

        availableThemes.bind(generalSettings.availableThemesProperty());
        availableLocales.bind(generalSettings.availableLocalesProperty());
        availableOperationLevels.bind(generalSettings.availableOperationLevelsProperty());
        availableSystemsOfMeasurement.bind(generalSettings.availableSystemsOfMeasurementProperty());
        selectedTheme.bindBidirectional(generalSettings.themeProperty());
        selectedLocale.bindBidirectional(generalSettings.localeProperty());
        selectedOperationLevel.bindBidirectional(generalSettings.operationLevelProperty());
        selectedSystemOfMeasurement.bindBidirectional(generalSettings.systemOfMeasurementProperty());
        selectedAngleStyle.bindBidirectional(generalSettings.angleStyleProperty());
        selectedMapRotationStyle.bindBidirectional(generalSettings.mapRotationStyleProperty());
        softwareUpdateEnabled.bindBidirectional(generalSettings.softwareUpdateEnabledProperty());
        changeSrsCommand = new DelegateCommand(this::changeSrs);
        mapUpdateEnabled.bindBidirectional(generalSettings.offlineModeProperty(), Converters.not());
        mapUpdateEnabled.addListener(updateMapCheckListener);
    }

    public ReadOnlyListProperty<Theme> availableThemesProperty() {
        return availableThemes.getReadOnlyProperty();
    }

    public ReadOnlyListProperty<OperationLevel> availableOperationLevelsProperty() {
        return availableOperationLevels.getReadOnlyProperty();
    }

    public ReadOnlyListProperty<Locale> availableLocalesProperty() {
        return availableLocales.getReadOnlyProperty();
    }

    public ReadOnlyListProperty<SystemOfMeasurement> availableSystemsOfMeasurementProperty() {
        return availableSystemsOfMeasurement.getReadOnlyProperty();
    }

    public Property<Theme> selectedThemeProperty() {
        return selectedTheme;
    }

    public Property<OperationLevel> selectedOperationLevelProperty() {
        return selectedOperationLevel;
    }

    public Property<SystemOfMeasurement> selectedSystemOfMeasurementProperty() {
        return selectedSystemOfMeasurement;
    }

    public Property<AngleStyle> selectedAngleStyleProperty() {
        return selectedAngleStyle;
    }

    public Property<MapRotationStyle> selectedMapRotationStyleProperty() {
        return selectedMapRotationStyle;
    }

    public Property<Locale> selectedLocaleProperty() {
        return selectedLocale;
    }

    public Property<Boolean> softwareUpdateEnabledProperty() {
        return softwareUpdateEnabled;
    }

    public Property<Boolean> mapUpdateEnabledProperty() {
        return mapUpdateEnabled;
    }

    public ReadOnlyBooleanProperty restartRequiredProperty() {
        return this.generalSettings.restartRequiredProperty();
    }

    public String getSpatialReference() {
        return spatialReference.get();
    }

    public ReadOnlyStringProperty spatialReferenceProperty() {
        return spatialReference;
    }

    public ReadOnlyListProperty<AngleStyle> availableAngleStylesProperty() {
        return availableAngleStyles;
    }

    public ReadOnlyListProperty<MapRotationStyle> availableMapRotationStylesProperty() {
        return availableMapRotationStyles;
    }

    public Command getChangeSrsCommand() {
        return changeSrsCommand;
    }

    private void changeSrs() {
        UIAsyncObjectProperty<MSpatialReference> srs = new UIAsyncObjectProperty<>(this);
        srs.bindBidirectional(srsSettings.applicationSrsProperty());
        dialogService.requestDialogAndWait(this, SpatialReferenceChooserViewModel.class, () -> srs).getDialogResult();
    }

}
