/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

import com.airmap.airmapsdk.networking.services.MappingService;
import com.intel.missioncontrol.airmap.layer.AirMapTileLoader2;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.layers.AirspaceLayer;
import com.intel.missioncontrol.settings.AirspacesProvidersSettings;

public class AirspaceLayerGroupViewModel extends LayerGroupViewModel {

    public AirspaceLayerGroupViewModel(AirspaceLayer airspaceLayer, ILanguageHelper languageHelper) {
        super(airspaceLayer, languageHelper, false);
        AirspacesProvidersSettings airspacesProvidersSettings = airspaceLayer.getAirspacesProvidersSettings();

        final String prefix = "com.intel.missioncontrol.map.worldwind.layers.airspace";
        for (MappingService.AirMapAirspaceType type : AirMapTileLoader2.getAirmapSearchTypes()) {
            LayerViewModel viewModel =
                new SimpleLayerViewModel(languageHelper.getString(prefix + "." + type.toString()));
            viewModel.selected.bindBidirectional(airspacesProvidersSettings.showAirspaceTypeProperty(type));
            subLayerItems.add(viewModel);
        }
    }

}
