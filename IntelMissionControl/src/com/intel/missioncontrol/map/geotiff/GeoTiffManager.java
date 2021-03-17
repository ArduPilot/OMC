/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.geotiff;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.IApplicationContext;
import com.intel.missioncontrol.beans.AsyncObservable;
import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.collections.LockedList;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.concurrent.FluentFuture;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.elevation.IElevationLayer;
import com.intel.missioncontrol.map.elevation.IElevationModelsManager;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.settings.GeoTiffSettings;
import com.intel.missioncontrol.settings.GeoTiffsSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import com.intel.missioncontrol.utils.IBackgroundTaskManager;
import java.io.File;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;

public class GeoTiffManager implements IGeoTiffManager {

    private final GeoTiffsSettings geoTiffsSettings;

    private final AsyncListProperty<ILayer> imageryLayers =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<ILayer>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final AsyncListProperty<IElevationLayer> elevationLayers =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<IElevationLayer>>()
                .initialValue(FXAsyncCollections.observableArrayList())
                .create());

    private final AsyncListProperty<GeoTiffEntry> geoTiffEntries =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<GeoTiffEntry>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(
                        geoTiffEntry ->
                            new AsyncObservable[] {
                                geoTiffEntry.enabledProperty(),
                                geoTiffEntry.mapLayerProperty(),
                                geoTiffEntry.elevationLayerProperty(),
                                geoTiffEntry
                                    .shiftProperty() // this line will make sure shift changes are propagating as
                                // elevation
                                // Model changes through the SW
                            }))
                .create());

    @Inject
    public GeoTiffManager(
            ISettingsManager settingsManager,
            ILanguageHelper languageHelper,
            IBackgroundTaskManager backgroundTaskManager,
            IApplicationContext applicationContext,
            @Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot,
            IMapView mapView,
            IElevationModelsManager elevationModelsManager) {
        geoTiffsSettings = settingsManager.getSection(GeoTiffsSettings.class);

        // load from settings
        geoTiffEntries.bindContent(
            geoTiffsSettings.geoTiffsProperty(),
            value ->
                new GeoTiffEntry(
                    GeoTiffManager.this,
                    settingsManager,
                    value,
                    applicationContext,
                    languageHelper,
                    backgroundTaskManager,
                    syncRoot,
                    mapView));

        // elevationLayers.bindContent();

        // TODO make binding
        geoTiffEntries.addListener(
            (InvalidationListener)
                c -> {
                    try (LockedList<GeoTiffEntry> lockedEntries = geoTiffEntries.lock()) {
                        elevationLayers.setAll(
                            lockedEntries
                                .stream()
                                .filter(GeoTiffEntry::isEnabled)
                                .map(geoTiffEntry -> geoTiffEntry.elevationLayerProperty().get())
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()));

                        imageryLayers.setAll(
                            lockedEntries
                                .stream()
                                .map(geoTiffEntry -> geoTiffEntry.mapLayerProperty().get())
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()));
                    }
                });

        elevationModelsManager.register(elevationLayers);
    }

    // TODO move to settings
    public void addGeoTiff(File geoTiffFile) {
        GeoTiffSettings gs = new GeoTiffSettings();
        gs.pathProperty().setValue(geoTiffFile.getAbsolutePath());
        gs.nameProperty().setValue(geoTiffFile.getName());
        geoTiffsSettings.geoTiffsProperty().add(gs);
    }

    public AsyncListProperty<ILayer> imageryLayersProperty() {
        return imageryLayers;
    }

    public AsyncListProperty<IElevationLayer> elevationLayersProperty() {
        return elevationLayers;
    }

    public AsyncListProperty<GeoTiffEntry> geoTiffEntriesProperty() {
        return geoTiffEntries;
    }

    // TODO move to the Settings
    public void dropGeotiffImport(GeoTiffEntry geoTiffEntry) {
        geoTiffEntry.unloadEntry();
        // geoTiffEntries.remove(geoTiffEntry);
        geoTiffsSettings.geoTiffsProperty().remove(geoTiffEntry.getGeoTiffSettings());
    }

    @Override
    public FluentFuture<GeoTiffEntry> importGeoTiff(GeoTiffEntry geoTiffEntry) {
        return Dispatcher.post(
            () -> {
                geoTiffEntry.loadWwj();
                return geoTiffEntry;
            });
    }

}
