/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.navbar.layers;

import com.intel.missioncontrol.beans.property.AsyncBooleanProperty;
import com.intel.missioncontrol.collections.ArrayMap;
import com.intel.missioncontrol.helper.ILanguageHelper;
import com.intel.missioncontrol.map.worldwind.layers.aircraft.AircraftLayerGroup;
import com.intel.missioncontrol.map.worldwind.layers.aircraft.AircraftLayerVisibilitySettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AircraftLayerGroupViewModel extends LayerGroupViewModel {

    public AircraftLayerGroupViewModel(AircraftLayerGroup aircraftLayerGroup, ILanguageHelper languageHelper) {
        super(aircraftLayerGroup, languageHelper, false, true);

        final String prefix = AircraftLayerGroup.class.getName();
        List<SimpleLayerViewModel> newLayers = new ArrayList<>();
        AircraftLayerVisibilitySettings aircraftLayerVisibilitySettings =
            aircraftLayerGroup.getAircraftLayerVisibility();

        Map<String, AsyncBooleanProperty> items = new ArrayMap<>();
        items.put("model", aircraftLayerVisibilitySettings.model3DProperty());
        items.put("track", aircraftLayerVisibilitySettings.trackProperty());
        items.put("startingPos", aircraftLayerVisibilitySettings.startingPositionProperty());
        items.put("boundingBox", aircraftLayerVisibilitySettings.boundingBoxProperty());
        items.put("coverage", aircraftLayerVisibilitySettings.coveragePreviewProperty());
        items.put("camView", aircraftLayerVisibilitySettings.cameraFieldOfViewProperty());
        items.put("gcs", aircraftLayerVisibilitySettings.groundStationProperty());
        items.put("flightPlan", aircraftLayerVisibilitySettings.flightPlanProperty());

        for (Map.Entry<String, AsyncBooleanProperty> entry : items.entrySet()) {
            SimpleLayerViewModel viewModel =
                new SimpleLayerViewModel(languageHelper.getString(prefix + "." + entry.getKey()));
            viewModel.selected.bindBidirectional(entry.getValue());
            newLayers.add(viewModel);
        }

        subLayerItems.setAll(newLayers);
    }
}
