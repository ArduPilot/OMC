/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.dataset;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.LayerName;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.mission.Matching;
import com.intel.missioncontrol.modules.MapModule;
import org.asyncfx.concurrent.Dispatcher;

public class DatasetLayer extends WWLayerWrapper {

    private final Matching matching;

    @Inject
    DatasetLayer(
            @Named(MapModule.DISPATCHER) Dispatcher dispatcher,
            Matching matching,
            IMapController mapController,
            ISelectionManager selectionManager) {
        super(
            new eu.mavinci.desktop.gui.doublepanel.planemain.tagging.rendering.MatchingLayer(
                matching.getLegacyMatching(), mapController, selectionManager, dispatcher),
            dispatcher);
        this.matching = matching;
        nameProperty().bind(matching.nameProperty(), LayerName::new);
    }

    protected Matching getMatching() {
        return matching;
    }

}
