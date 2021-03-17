/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.flightplan;

import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.IMapModel;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.LayerName;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.mission.Drone;
import com.intel.missioncontrol.mission.FlightPlan;
import com.intel.missioncontrol.settings.GeneralSettings;
import com.intel.missioncontrol.ui.navigation.INavigationService;
import org.asyncfx.concurrent.SynchronizationRoot;

public class FlightplanLayer extends WWLayerWrapper {

    FlightplanLayer(
            SynchronizationRoot syncRoot,
            FlightPlan flightPlan,
            Drone uav,
            IWWGlobes globes,
            IMapModel mapModel,
            IWWMapView mapView,
            IMapController mapController,
            IElevationModel elevationModel,
            INavigationService navigationService,
            ISelectionManager selectionManager,
            ILanguageHelper languageHelper,
            GeneralSettings generalSettings,
            FlightplanLayerVisibilitySettings flightplanLayerVisibilitySettings) {
        super(
            new eu.mavinci.desktop.gui.doublepanel.planemain.wwd.FlightplanLayer(
                flightPlan,
                uav,
                globes,
                mapModel,
                mapView,
                mapController,
                selectionManager,
                elevationModel,
                navigationService,
                languageHelper,
                generalSettings,
                flightplanLayerVisibilitySettings,
                syncRoot),
            syncRoot);
        nameProperty().bind(flightPlan.nameProperty(), LayerName::new);
    }

    @Override
    protected eu.mavinci.desktop.gui.doublepanel.planemain.wwd.FlightplanLayer getWrappedLayer() {
        return (eu.mavinci.desktop.gui.doublepanel.planemain.wwd.FlightplanLayer)super.getWrappedLayer();
    }

}
