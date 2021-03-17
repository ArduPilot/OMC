/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.google.inject.Inject;
import com.intel.missioncontrol.beans.AsyncObservable;
import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.AsyncObjectProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncListProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncObjectProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncBooleanProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;
import com.intel.missioncontrol.collections.LockedList;
import com.intel.missioncontrol.map.elevation.AbstractElevationModel;
import com.intel.missioncontrol.map.elevation.ElevationLayerWrapper;
import com.intel.missioncontrol.map.elevation.IElevationLayer;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.elevation.IElevationModelsManager;
import com.intel.missioncontrol.settings.ElevationModelSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.terrain.CompoundElevationModel;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import java.util.LinkedList;
import java.util.List;
import javafx.collections.ListChangeListener;

public class WWElevationModel extends AbstractElevationModel implements IElevationModel, IElevationModelsManager {

    private CompoundElevationModel elev;
    private final AsyncObjectProperty<ElevationModel> elevProperty = new SimpleAsyncObjectProperty<>(this);
    private final Globe globe;
    private final ElevationModel elevZero;

    private final AsyncListProperty<IElevationLayer> elevationLayers =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<IElevationLayer>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(
                        elevationLayer -> new AsyncObservable[] {elevationLayer.enabledProperty()}))
                .create());

    private final AsyncListProperty<ReadOnlyAsyncListProperty<IElevationLayer>> elevationLayerProviders =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<ReadOnlyAsyncListProperty<IElevationLayer>>>()
                .initialValue(FXAsyncCollections.observableArrayList(provider -> new AsyncObservable[] {provider}))
                .create());

    private final IElevationLayer baseLayer;
    private final AsyncBooleanProperty terrainEnabled = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty useTerrainBaselayer = new SimpleAsyncBooleanProperty(this);
    private final AsyncBooleanProperty useGeoTiffs = new SimpleAsyncBooleanProperty(this);

    @Inject
    public WWElevationModel(IWWGlobes globes, ISettingsManager settingsManager) {
        this.globe = globes.getDefaultGlobe();
        elev = (CompoundElevationModel)globe.getElevationModel();
        elevProperty.setValue(elev);
        baseLayer = new ElevationLayerWrapper(elev.getElevationModels().get(0));
        elevZero = new ZeroElevationModel();

        ElevationModelSettings elevationModelSettings = settingsManager.getSection(ElevationModelSettings.class);
        useTerrainBaselayer.bind(elevationModelSettings.useDefaultElevationModelProperty());
        terrainEnabled.bind(elevationModelSettings.useSurfaceDataForPlanningProperty());
        useGeoTiffs.bind(elevationModelSettings.useGeoTIFFProperty());

        useGeoTiffs.addListener((observable, oldValue, newValue) -> updateWwElevationModels());
        terrainEnabled.addListener((observable, oldValue, newValue) -> updateWwElevationModels());
        useTerrainBaselayer.addListener((observable, oldValue, newValue) -> updateWwElevationModels());
        elevationLayers.addListener((ListChangeListener<? super IElevationLayer>)c -> updateWwElevationModels());

        elevationLayerProviders.addListener(
            (ListChangeListener<? super ReadOnlyAsyncListProperty<IElevationLayer>>)
                c -> {
                    // flatteing list with locks
                    try (LockedList<ReadOnlyAsyncListProperty<IElevationLayer>> tmp = elevationLayerProviders.lock()) {
                        LinkedList<IElevationLayer> elevationLayerNew = new LinkedList<>();
                        for (ReadOnlyAsyncListProperty<IElevationLayer> tmp2 : tmp) {
                            try (LockedList<IElevationLayer> tmpElev = tmp2.lock()) {
                                elevationLayerNew.addAll(tmpElev);
                            }
                        }

                        elevationLayers.setAll(elevationLayerNew);
                    }
                });
    }

    /** update the list of elevation layers in the compound model of WorldWind */
    private void updateWwElevationModels() {
        CompoundElevationModel elev = new CompoundElevationModel();
        if (!terrainEnabled.get()) {
            elev.addElevationModel(elevZero);
        } else {
            if (useTerrainBaselayer.get()) {
                elev.addElevationModel(baseLayer.getElevationModel());
            } else {
                elev.addElevationModel(elevZero);
            }

            if (useGeoTiffs.get()) {
                try (LockedList<IElevationLayer> tmp = elevationLayers.lock()) {
                    tmp.stream()
                        .filter(el -> el.getElevationModel() != null && el.enabledProperty().get())
                        .forEach(el -> elev.addElevationModel(el.getElevationModel()));
                }
            }
        }
        // since this is atomic, we dont need any synchronization or locks
        this.elev = elev;

        // not sure if the next two lines are on the correct thread?
        globe.setElevationModel(elev);
        globe.firePropertyChange(
            AVKey.LAYER,
            null,
            null); // TODO not sure if this really informs WW about the change or nor... is a redraw triggered?

        // inform all listeners about new model
        elevProperty.setValue(elev);
    }

    /**
     * in order to get notify about change in the elevation model setup just listen to this property please note that
     * its NOT invalidated on arrival of new elevation data by network or from disk loading
     */
    public ReadOnlyAsyncObjectProperty<ElevationModel> wwjElevationModelProperty() {
        return elevProperty;
    }

    @Override
    public double getElevations(
            Sector sector,
            List<? extends LatLon> latlons,
            double targetResolution,
            double[] buffer,
            CompoundElevationModel.ElevationModelRerence bestModel) {
        return elev.getElevations(sector, latlons, targetResolution, buffer, bestModel);
    }

    @Override
    protected double getBestResolution(Sector sector) {
        return elev.getBestResolution(sector);
    }

    @Override
    public double getRadiusAt(LatLon latLon) {
        return globe.getRadiusAt(latLon);
    }

    @Override
    public AsyncListProperty<IElevationLayer> layersProperty() {
        return elevationLayers;
    }

    @Override
    public AsyncBooleanProperty terrainEnabledProperty() {
        return terrainEnabled;
    }

    @Override
    public AsyncBooleanProperty useTerrainBaselayerProperty() {
        return useTerrainBaselayer;
    }

    @Override
    public AsyncBooleanProperty useGeoTiffsProperty() {
        return useGeoTiffs;
    }

    @Override
    public IElevationLayer baseLayerProperty() {
        return baseLayer;
    }

    @Override
    public void register(ReadOnlyAsyncListProperty<IElevationLayer> elevationModels) {
        elevationLayerProviders.add(elevationModels);
    }
}
