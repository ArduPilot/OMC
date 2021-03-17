/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.aircraft;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.IApplicationContext;
import org.asyncfx.beans.property.AsyncObjectProperty;
import org.asyncfx.beans.property.AsyncProperty;
import org.asyncfx.beans.property.SimpleAsyncObjectProperty;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.map.LayerGroup;
import com.intel.missioncontrol.map.LayerGroupType;
import com.intel.missioncontrol.map.LayerName;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.mission.Mission;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.settings.OperationLevel;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import com.intel.missioncontrol.ui.navigation.WorkflowStep;
import eu.mavinci.core.licence.ILicenceManager;
import eu.mavinci.core.obfuscation.IKeepClassname;
import eu.mavinci.desktop.gui.doublepanel.planemain.tagging.MapLayerCoverageLive;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.IMapLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayerCurrentCameraView;
import eu.mavinci.desktop.gui.doublepanel.planemain.tree.maplayers.MapLayerPictures;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.AssistedBoundingBoxLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.BackendLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.PlaneModelLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.PlaneTextOverlayLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.StartingPosLayer;
import eu.mavinci.desktop.gui.doublepanel.planemain.wwd.TrackLayer;
import eu.mavinci.plane.IAirplane;
import gov.nasa.worldwind.layers.Layer;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import org.asyncfx.concurrent.SynchronizationRoot;
import playground.liveview.ILiveVideoWidgetService;

public class AircraftLayerGroup extends LayerGroup implements IKeepClassname {

    private final AsyncObjectProperty<Mission> currentMission = new SimpleAsyncObjectProperty<>(this);
    private final List<IMapLayer> legacyMapLayers = new ArrayList<>();
    private final SynchronizationRoot syncRoot;
    private final IMapModel mapModel;
    private final ILiveVideoWidgetService liveVideoWidgetService;
    private final AircraftLayerVisibilitySettings aircraftLayerVisibilitySettings;

    @Inject
    public AircraftLayerGroup(
            @Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot,
            IMapModel mapModel,
            IApplicationContext applicationContext,
            INavigationService navigationService,
            ILicenceManager licenceManager,
            GeneralSettings generalSettings,
            AircraftLayerVisibilitySettings aircraftLayerVisibilitySettings,
            ILiveVideoWidgetService liveVideoWidgetService) {
        super(LayerGroupType.AIRCRAFT_GROUP);
        this.syncRoot = syncRoot;
        this.mapModel = mapModel;
        this.liveVideoWidgetService = liveVideoWidgetService;
        this.aircraftLayerVisibilitySettings = aircraftLayerVisibilitySettings;
        this.currentMission.bind(applicationContext.currentMissionProperty());
        this.currentMission.addListener((observable, oldValue, newValue) -> revalidate(newValue), syncRoot::runAsync);
        setName(new LayerName("%" + getClass().getName()));

        internalProperty()
            .bind(
                Bindings.createBooleanBinding(
                    () ->
                        navigationService.getWorkflowStep() != WorkflowStep.FLIGHT
                            && generalSettings.getOperationLevel() != OperationLevel.DEBUG
                            && !licenceManager.isGrayHawkEditionProperty().get(),
                    navigationService.workflowStepProperty(),
                    generalSettings.operationLevelProperty(),
                    licenceManager.isGrayHawkEditionProperty()));

        // the live video checkbox needs special treatment, because it is synchronous
        aircraftLayerVisibilitySettings.liveVideoProperty().bindBidirectional(liveVideoWidgetService.isVisibleProperty());
    }

    private void revalidate(Mission mission) {
        syncRoot.verifyAccess();

        legacyMapLayers.clear();
        subLayersProperty().clear();

        if (mission == null) {
            return;
        }

        IAirplane plane = mission.getLegacyPlane();
        if (plane == null) {
            return;
        }

        MapLayerPictures pic = new MapLayerPictures(plane);
        MapLayerCoverageLive cov = new MapLayerCoverageLive(plane, pic);
        MapLayerCurrentCameraView camView = new MapLayerCurrentCameraView(plane);
        legacyMapLayers.add(pic);
        legacyMapLayers.add(cov);
        legacyMapLayers.add(camView);

        final String prefix = "%" + getClass().getName();

        subLayersProperty()
            .addAll(
                createLayer(
                    new PlaneModelLayer(plane),
                    syncRoot,
                    prefix + ".model",
                    aircraftLayerVisibilitySettings.model3DProperty()),
                createLayer(
                    new PlaneTextOverlayLayer(plane),
                    syncRoot,
                    prefix + ".text",
                    aircraftLayerVisibilitySettings.model3DProperty()),
                createLayer(
                    new TrackLayer(plane),
                    syncRoot,
                    prefix + ".track",
                    aircraftLayerVisibilitySettings.trackProperty()),
                createLayer(
                    new StartingPosLayer(plane, mapModel),
                    syncRoot,
                    prefix + ".startingPos",
                    aircraftLayerVisibilitySettings.startingPositionProperty()),
                createLayer(
                    new AssistedBoundingBoxLayer(plane),
                    syncRoot,
                    prefix + ".boundingBox",
                    aircraftLayerVisibilitySettings.boundingBoxProperty()),
                createLayer(
                    pic.getWWLayer(),
                    syncRoot,
                    prefix + ".pics",
                    aircraftLayerVisibilitySettings.imageAreaPreviewProperty()),
                createLayer(
                    cov.getWWLayer(),
                    syncRoot,
                    prefix + ".coverage",
                    aircraftLayerVisibilitySettings.coveragePreviewProperty()),
                createLayer(
                    camView.getWWLayer(),
                    syncRoot,
                    prefix + ".camView",
                    aircraftLayerVisibilitySettings.cameraFieldOfViewProperty()),
                createLayer(
                    new BackendLayer(plane),
                    syncRoot,
                    prefix + ".gcs",
                    aircraftLayerVisibilitySettings.groundStationProperty()));

    }

    private static ILayer createLayer(
            Layer wwLayer, SynchronizationRoot syncRoot, String name, AsyncProperty<Boolean> enabled) {
        ILayer layer = new WWLayerWrapper(wwLayer, syncRoot);
        layer.setName(new LayerName(name));
        layer.enabledProperty().bindBidirectional(enabled);
        return layer;
    }

    public AircraftLayerVisibilitySettings getAircraftLayerVisibility() {
        return aircraftLayerVisibilitySettings;
    }
}
