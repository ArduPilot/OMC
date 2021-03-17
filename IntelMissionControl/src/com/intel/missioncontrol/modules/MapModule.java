/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scope;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.intel.missioncontrol.LocalScope;
import com.intel.missioncontrol.VisibilityTracker;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.diagnostics.WorldWindProfiler;
import com.intel.missioncontrol.map.ILayerFactory;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.map.IMapView;
import com.intel.missioncontrol.map.credits.AirspacesProvidersCredits;
import com.intel.missioncontrol.map.credits.IMapCreditsManager;
import com.intel.missioncontrol.map.credits.MapCreditsManager;
import com.intel.missioncontrol.map.elevation.EgmModel;
import com.intel.missioncontrol.map.elevation.IEgmModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.elevation.IElevationModelsManager;
import com.intel.missioncontrol.map.geotiff.GeoTiffManager;
import com.intel.missioncontrol.map.geotiff.IGeoTiffManager;
import com.intel.missioncontrol.map.kml.IKmlManager;
import com.intel.missioncontrol.map.kml.KmlManager;
import com.intel.missioncontrol.map.wms.IWmsManager;
import com.intel.missioncontrol.map.wms.WmsManager;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.IWWMapModel;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.map.worldwind.WWMapController;
import com.intel.missioncontrol.map.worldwind.WWElevationModel;
import com.intel.missioncontrol.map.worldwind.WWGlobes;
import com.intel.missioncontrol.map.worldwind.WWLayerFactory;
import com.intel.missioncontrol.map.worldwind.WWMapModel;
import com.intel.missioncontrol.map.worldwind.WWMapView;
import com.intel.missioncontrol.map.worldwind.WorldWindowProvider;
import com.intel.missioncontrol.map.worldwind.impl.IScreenshotManager;
import com.intel.missioncontrol.map.worldwind.impl.ScreenshotManager;
import com.intel.missioncontrol.ui.navbar.layers.IMapClearingCenter;
import com.intel.missioncontrol.ui.navbar.layers.MapClearingCenter;
import eu.mavinci.desktop.gui.wwext.search.SearchManager;

public final class MapModule extends AbstractModule {

    public static final String SYNC_ROOT = "MapModuleSyncRoot";

    @Override
    protected void configure() {
        // Singleton scope
        bind(IEgmModel.class).to(EgmModel.class).in(Singleton.class);
        bind(IGeoTiffManager.class).to(GeoTiffManager.class).in(Singleton.class);
        bind(IKmlManager.class).to(KmlManager.class).in(Singleton.class);
        bind(IWmsManager.class).to(WmsManager.class).in(Singleton.class);
        bind(WWElevationModel.class).in(Singleton.class);
        bind(IElevationModel.class).to(WWElevationModel.class);
        bind(IElevationModelsManager.class).to(WWElevationModel.class);
        bind(SearchManager.class).in(Singleton.class);
        bind(IWWGlobes.class).to(WWGlobes.class).in(Singleton.class);

        // Local scope
        Scope scope = LocalScope.getInstance();

        SynchronizationRoot synchronizationRoot = new SynchronizationRoot();
        bind(SynchronizationRoot.class).annotatedWith(Names.named(SYNC_ROOT)).toInstance(synchronizationRoot);
        bind(SynchronizationRoot.class).toInstance(synchronizationRoot);

        // TODO for proper multi window support roll back to this and make sure we only inject with NAMED annotation
        // bind(SynchronizationRoot.class)
        // .annotatedWith(Names.named(SYNC_ROOT))
        // .toProvider(SynchronizationRoot::new)
        // .in(scope);

        bind(WWMapController.class).in(scope);
        bind(IMapController.class).to(WWMapController.class);
        bind(WWMapModel.class).in(scope);
        bind(IMapModel.class).to(WWMapModel.class);
        bind(IWWMapModel.class).to(WWMapModel.class);
        bind(WWMapView.class).in(scope);
        bind(IMapView.class).to(WWMapView.class);
        bind(IWWMapView.class).to(WWMapView.class);
        bind(WWLayerFactory.class).in(scope);
        bind(ILayerFactory.class).to(WWLayerFactory.class);
        bind(VisibilityTracker.class).in(scope);
        bind(WorldWindProfiler.class).in(scope);
        bind(WorldWindowProvider.class).in(scope);
        bind(IMapClearingCenter.class).to(MapClearingCenter.class).in(scope);
        bind(IMapCreditsManager.class).to(MapCreditsManager.class).in(Singleton.class);
        bind(AirspacesProvidersCredits.class).asEagerSingleton();
        bind(IScreenshotManager.class).to(ScreenshotManager.class);
    }

}
