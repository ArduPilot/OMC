/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.airmap.AirMap2Layer;
import com.intel.missioncontrol.airspaces.AirspaceProvider;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.modules.MapModule;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;
import com.intel.missioncontrol.settings.ISettingsManager;
import eu.mavinci.desktop.gui.doublepanel.mapmanager.airspaces.layers.AirspacesLayer;
import org.asyncfx.concurrent.Dispatcher;

@LayerDefaults(name = "%com.intel.missioncontrol.ui.navbar.layers.MapLayersView.AirspaceLayer", enabled = true)
public class AirspaceLayer extends WWLayerWrapper {

    private final AirspacesProvidersSettings airspacesProvidersSettings;

    @Inject
    AirspaceLayer(@Named(MapModule.DISPATCHER) Dispatcher dispatcher, ISettingsManager settingsManager) {
        super(
            (settingsManager.getSection(AirspacesProvidersSettings.class).getAirspaceProvider()
                    == AirspaceProvider.AIRMAP2
                ? new AirMap2Layer(dispatcher)
                : new AirspacesLayer(dispatcher)),
            dispatcher);
        airspacesProvidersSettings = settingsManager.getSection(AirspacesProvidersSettings.class);
    }

    public AirspacesProvidersSettings getAirspacesProvidersSettings() {
        return airspacesProvidersSettings;
    }
}
