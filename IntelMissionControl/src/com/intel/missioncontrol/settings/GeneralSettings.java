/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.google.inject.Inject;
import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.AsyncDoubleProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.AsyncStringProperty;
import com.intel.missioncontrol.beans.property.IQuantityStyleProvider;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncListProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncListWrapper;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncDoubleProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncStringProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.measure.AngleStyle;
import com.intel.missioncontrol.measure.SystemOfMeasurement;
import com.intel.missioncontrol.measure.TimeStyle;
import com.intel.missioncontrol.ui.Theme;
import eu.mavinci.core.licence.ILicenceManager;
import java.util.LinkedHashMap;
import java.util.Locale;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

@SettingsMetadata(section = "general")
public class GeneralSettings implements ISettings, IQuantityStyleProvider {

    private AsyncObjectProperty<Locale> locale =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<Locale>().initialValue(Locale.ENGLISH).create());

    private AsyncObjectProperty<Theme> theme =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<Theme>().initialValue(Theme.LIGHT).create());

    private AsyncObjectProperty<OperationLevel> operationLevel =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<OperationLevel>().initialValue(OperationLevel.USER).create());

    private AsyncObjectProperty<SystemOfMeasurement> systemOfMeasurement =
        new SimpleAsyncObjectProperty<>(
            this,
            new PropertyMetadata.Builder<SystemOfMeasurement>().initialValue(SystemOfMeasurement.METRIC).create());

    private AsyncObjectProperty<AngleStyle> angleStyle =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<AngleStyle>().initialValue(AngleStyle.DECIMAL_DEGREES).create());

    private AsyncObjectProperty<TimeStyle> timeStyle =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<TimeStyle>().initialValue(TimeStyle.HOUR_MINUTE_SECOND).create());

    private AsyncObjectProperty<MapRotationStyle> mapRotationStyle =
        new SimpleAsyncObjectProperty<>(
            this, new PropertyMetadata.Builder<MapRotationStyle>().initialValue(MapRotationStyle.DEFAULT).create());

    private AsyncBooleanProperty softwareUpdateEnabled =
        new SimpleAsyncBooleanProperty(this, new PropertyMetadata.Builder<Boolean>().initialValue(false).create());

    private AsyncBooleanProperty offlineMode = new SimpleAsyncBooleanProperty(this);

    private AsyncStringProperty lastFlightPlanExportFolder = new SimpleAsyncStringProperty(this);

    private AsyncBooleanProperty profilingEnabled = new SimpleAsyncBooleanProperty(this);

    private AsyncBooleanProperty eraseLogsAfterCopy = new SimpleAsyncBooleanProperty(this);

    private AsyncDoubleProperty aoiMaximizationAspectRatio =
        new SimpleAsyncDoubleProperty(this, new PropertyMetadata.Builder<Number>().initialValue(2.5).create());

    private transient ReadOnlyAsyncListWrapper<Theme> availableThemes =
        new ReadOnlyAsyncListWrapper<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<Theme>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private transient ReadOnlyAsyncListWrapper<Locale> availableLocales =
        new ReadOnlyAsyncListWrapper<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<Locale>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private transient ReadOnlyAsyncListWrapper<SystemOfMeasurement> availableSystemsOfMeasurement =
        new ReadOnlyAsyncListWrapper<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<SystemOfMeasurement>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private transient ReadOnlyAsyncListWrapper<OperationLevel> availableOperationLevels =
        new ReadOnlyAsyncListWrapper<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<OperationLevel>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private transient BooleanProperty restartRequired = new SimpleBooleanProperty(this, "restartRequired");

    // SendSupportDialog data
    private StringProperty fullNameInSupport = new SimpleStringProperty();
    private StringProperty countryInSupport = new SimpleStringProperty();
    private StringProperty emailsInSupport = new SimpleStringProperty();

    private StringProperty lastSeenAPForSupport = new SimpleStringProperty(this, "lastSeenAP");
    private StringProperty lastSeenConnectorForSupport = new SimpleStringProperty(this, "lastSeenConnector");

    // end SendSupportDialog data

    private AsyncBooleanProperty logDefaultOn = new SimpleAsyncBooleanProperty(this);

    private SimpleStringProperty defaultCameraFilename = new SimpleStringProperty(this, "defaultCameraFilename");
    private SimpleIntegerProperty autoClearingIntervall = new SimpleIntegerProperty(this, "autoClearingIntervall", 30);

    private final MapProperty<String, Integer> lastSearchResults =
        new SimpleMapProperty<>(FXCollections.observableMap(new LinkedHashMap<>()));

    private final transient ILicenceManager licenceManager;

    @Inject
    public GeneralSettings(ILicenceManager licenseManager) {
        this.licenceManager = licenseManager;
    }

    @Override
    public void onLoaded() {
        configureAvailableOperationLevels();
        configureAvailableThemes();
        configureAvailableLocales();
        configureAvailableSystemsOfMeasurement();
        timeStyle.set(
            TimeStyle
                .HOUR_MINUTE_SECOND); // hard coded on 2019-01-17 to make sure no legacy settings are confusing this

        coerceOperationLevel();
        coerceTheme();
        coerceSystemOfMeasurement();

        licenceManager
            .activeLicenceProperty()
            .addListener(
                (observable, oldValue, newValue) -> {
                    coerceOperationLevel();
                    configureAvailableOperationLevels();
                });

        locale.addListener((observable, oldValue, newValue) -> restartRequired.set(true));
        theme.addListener((observable, oldValue, newValue) -> Theme.Accessor.setCurrentTheme(newValue));

        availableThemes.addListener((ListChangeListener<? super Theme>)observable -> coerceTheme());
        availableSystemsOfMeasurement.addListener(
            (ListChangeListener<? super SystemOfMeasurement>)observable -> coerceSystemOfMeasurement());
        availableOperationLevels.addListener(
            (ListChangeListener<? super OperationLevel>)observable -> coerceOperationLevel());
    }

    public AsyncObjectProperty<MapRotationStyle> mapRotationStyleProperty() {
        return mapRotationStyle;
    }

    public AsyncObjectProperty<Theme> themeProperty() {
        return this.theme;
    }

    public Theme getTheme() {
        return this.theme.get();
    }

    public void setTheme(Theme theme) {
        this.theme.set(theme);
    }

    public AsyncObjectProperty<Locale> localeProperty() {
        return this.locale;
    }

    public Locale getLocale() {
        return this.locale.get();
    }

    public void setLocale(Locale locale) {
        this.locale.set(locale);
    }

    public ReadOnlyAsyncListProperty<Theme> availableThemesProperty() {
        return availableThemes.getReadOnlyProperty();
    }

    public AsyncObservableList<Theme> getAvailableThemes() {
        return availableThemes.get();
    }

    public ReadOnlyAsyncListProperty<Locale> availableLocalesProperty() {
        return availableLocales.getReadOnlyProperty();
    }

    public AsyncObservableList<Locale> getAvailableLocales() {
        return availableLocales.get();
    }

    public ReadOnlyAsyncListProperty<OperationLevel> availableOperationLevelsProperty() {
        return availableOperationLevels.getReadOnlyProperty();
    }

    public AsyncObservableList<OperationLevel> getAvailableOperationLevels() {
        return availableOperationLevels.get();
    }

    public ReadOnlyAsyncListProperty<SystemOfMeasurement> availableSystemsOfMeasurementProperty() {
        return availableSystemsOfMeasurement.getReadOnlyProperty();
    }

    public AsyncObservableList<SystemOfMeasurement> getAvailableSystemsOfMeasurement() {
        return availableSystemsOfMeasurement.get();
    }

    public AsyncObjectProperty<OperationLevel> operationLevelProperty() {
        return this.operationLevel;
    }

    public OperationLevel getOperationLevel() {
        return this.operationLevel.get();
    }

    public void setOperationLevel(OperationLevel operationLevel) {
        this.operationLevel.set(operationLevel);
    }

    public AsyncObjectProperty<SystemOfMeasurement> systemOfMeasurementProperty() {
        return this.systemOfMeasurement;
    }

    public AsyncObjectProperty<AngleStyle> angleStyleProperty() {
        return this.angleStyle;
    }

    public AsyncObjectProperty<TimeStyle> timeStyleProperty() {
        return this.timeStyle;
    }

    public SystemOfMeasurement getSystemOfMeasurement() {
        return this.systemOfMeasurement.get();
    }

    public AngleStyle getAngleStyle() {
        return this.angleStyle.get();
    }

    public TimeStyle getTimeStyle() {
        return this.timeStyle.get();
    }

    public MapRotationStyle getMapRotationStyle() {
        return mapRotationStyle.get();
    }

    public void setSystemOfMeasurement(SystemOfMeasurement systemOfMeasurement) {
        this.systemOfMeasurement.set(systemOfMeasurement);
    }

    public void setAngleStyle(AngleStyle angleStyle) {
        this.angleStyle.set(angleStyle);
    }

    public void setTimeStyle(TimeStyle timeStyle) {
        this.timeStyle.set(timeStyle);
    }

    public ReadOnlyBooleanProperty restartRequiredProperty() {
        return this.restartRequired;
    }

    public boolean isRestartRequired() {
        return this.restartRequired.get();
    }

    public AsyncBooleanProperty softwareUpdateEnabledProperty() {
        return this.softwareUpdateEnabled;
    }

    public AsyncBooleanProperty offlineModeProperty() {
        return this.offlineMode;
    }

    public boolean isOfflineMode() {
        return this.offlineMode.get();
    }

    public AsyncBooleanProperty profilingEnabledProperty() {
        return profilingEnabled;
    }

    public boolean getProfilingEnabled() {
        return profilingEnabled.get();
    }

    public AsyncBooleanProperty eraseLogsAfterCopyProperty() {
        return eraseLogsAfterCopy;
    }

    public boolean getEraseLogsAfterCopy() {
        return eraseLogsAfterCopy.get();
    }

    public String getLastFlightPlanExportFolder() {
        return lastFlightPlanExportFolder.get();
    }

    public AsyncStringProperty lastFlightPlanExportFolderProperty() {
        return lastFlightPlanExportFolder;
    }

    public StringProperty fullNameInSupportProperty() {
        return fullNameInSupport;
    }

    public StringProperty countryInSupportProperty() {
        return countryInSupport;
    }

    public StringProperty emailsInSupportProperty() {
        return emailsInSupport;
    }

    public StringProperty lastSeenAPForSupportProperty() {
        return lastSeenAPForSupport;
    }

    public StringProperty lastSeenConnectorForSupportProperty() {
        return lastSeenConnectorForSupport;
    }

    public AsyncDoubleProperty aoiMaximizationAspectRatioProperty() {
        return aoiMaximizationAspectRatio;
    }

    private void configureAvailableOperationLevels() {
        availableOperationLevels.setAll(OperationLevel.getAllowedLevels(licenceManager.getMaxOperationLevel()));
    }

    private void configureAvailableThemes() {
        if (operationLevel.get() == OperationLevel.DEBUG) {
            availableThemes.setAll(Theme.values());
        } else {
            availableThemes.setAll(Theme.LIGHT);
        }
    }

    private void configureAvailableLocales() {
        if (operationLevel.get() == OperationLevel.DEBUG) {
            availableLocales.setAll(
                new Locale.Builder().setLanguage("en").setRegion("GB").build(),
                new Locale.Builder().setLanguage("en").build(),
                new Locale.Builder().setLanguage("de").setRegion("DE").build());
        } else {
            availableLocales.setAll(new Locale.Builder().setLanguage("en").build());
        }

        boolean chooseEnglishOrDefault = false;
        Locale configLocale = locale.get();
        if (configLocale != null) {
            if (availableLocales.contains(configLocale)) {
                Locale.setDefault(configLocale);
            } else {
                chooseEnglishOrDefault = true;
            }
        } else {
            chooseEnglishOrDefault = true;
        }

        if (chooseEnglishOrDefault) {
            if (availableLocales.contains(Locale.getDefault())) {
                this.locale.set(Locale.getDefault());
            } else {
                this.locale.set(Locale.ENGLISH);
                Locale.setDefault(Locale.ENGLISH);
            }
        }
    }

    private void configureAvailableSystemsOfMeasurement() {
        if (getOperationLevel() == OperationLevel.DEBUG) {
            availableSystemsOfMeasurement.setAll(SystemOfMeasurement.values());
        } else {
            availableSystemsOfMeasurement.setAll(SystemOfMeasurement.METRIC, SystemOfMeasurement.IMPERIAL);
        }
    }

    private void coerceOperationLevel() {
        var allowedLevels = OperationLevel.getAllowedLevels(licenceManager.getMaxOperationLevel());

        if (!allowedLevels.isEmpty() && !allowedLevels.contains(operationLevel.get())) {
            operationLevel.set(allowedLevels.get(0));
        }
    }

    private void coerceTheme() {
        if (!availableThemes.isEmpty() && !availableThemes.contains(theme.get())) {
            theme.set(availableThemes.get(0));
        }
    }

    private void coerceSystemOfMeasurement() {
        if (!availableSystemsOfMeasurement.isEmpty()
                && !availableSystemsOfMeasurement.contains(systemOfMeasurement.get())) {
            systemOfMeasurement.set(availableSystemsOfMeasurement.get(0));
        }
    }

    public SimpleStringProperty getProperty(String key, String s) {
        return new SimpleStringProperty(this, key, s);
    }

    public void setProperty(String key, String s) {
        new SimpleStringProperty(this, key, s);
    }

    public Boolean getLogDefaultOn() {
        return logDefaultOn.get();
    }

    public SimpleStringProperty getDefaultCameraFilenameProperty() {
        return defaultCameraFilename;
    }

    public long getAutoClearingIntervallInMS() {
        return autoClearingIntervall.get() * 60 * 1000L;
    }

    public SimpleIntegerProperty autoClearingIntervallProperty() {
        return autoClearingIntervall;
    }

    public MapProperty<String, Integer> lastSearchResultsProperty() {
        return lastSearchResults;
    }
}
