/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.modules.MapModule;
import org.asyncfx.concurrent.Dispatcher;

@LayerDefaults(internal = true)
public class SkyGradientLayer extends WWLayerWrapper {

    @Inject
    SkyGradientLayer(@Named(MapModule.DISPATCHER) Dispatcher dispatcher) {
        super(new gov.nasa.worldwind.layers.SkyGradientLayer(), dispatcher);
    }

}
