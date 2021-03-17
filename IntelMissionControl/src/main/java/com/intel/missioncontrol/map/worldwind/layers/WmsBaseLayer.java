/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.modules.MapModule;
import org.asyncfx.concurrent.Dispatcher;

public class WmsBaseLayer extends WWLayerWrapper {

    @Inject
    WmsBaseLayer(@Named(MapModule.DISPATCHER) Dispatcher dispatcher) {
        super(new gov.nasa.worldwind.layers.Earth.BMNGWMSLayer(), dispatcher);
    }

}
