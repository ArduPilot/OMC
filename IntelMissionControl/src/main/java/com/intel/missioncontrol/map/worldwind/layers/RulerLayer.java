/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.map.IMapController;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.IWWMapView;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.map.worldwind.WorldWindowProvider;
import com.intel.missioncontrol.measure.property.IQuantityStyleProvider;
import com.intel.missioncontrol.modules.MapModule;
import gov.nasa.worldwind.layers.RenderableLayer;
import org.asyncfx.concurrent.Dispatcher;

@LayerDefaults(internal = true)
public class RulerLayer extends WWLayerWrapper {

    @Inject
    RulerLayer(
            @Named(MapModule.DISPATCHER) Dispatcher dispatcher,
            IElevationModel elevationModel,
            IWWGlobes globes,
            IWWMapView mapView,
            IMapController mapController,
            WorldWindowProvider worldWindowProvider,
            IQuantityStyleProvider quantityStyleProvider) {
        super(new RenderableLayer(), dispatcher);
        worldWindowProvider.whenAvailable(
            worldWindow -> {
                RenderableLayer layer = (RenderableLayer)getWrappedLayer();
                new DistanceRenderable(
                    layer, elevationModel, globes, mapView, mapController, worldWindow, quantityStyleProvider);
            });
    }

}
