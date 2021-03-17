/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.grids;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.modules.MapModule;
import eu.mavinci.core.obfuscation.IKeepAll;
import org.asyncfx.concurrent.Dispatcher;

@LayerDefaults(name = "%com.intel.missioncontrol.map.worldwind.layers.grids.LatLonGraticuleLayer", enabled = false)
public class LatLonGraticuleLayer extends WWLayerWrapper implements IKeepAll {

    @Inject
    LatLonGraticuleLayer(@Named(MapModule.DISPATCHER) Dispatcher dispatcher) {
        super(new gov.nasa.worldwind.layers.LatLonGraticuleLayer(), dispatcher);
    }

}
