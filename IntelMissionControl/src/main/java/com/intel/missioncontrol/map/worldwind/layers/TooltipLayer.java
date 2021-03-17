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
import com.intel.missioncontrol.map.worldwind.WorldWindowProvider;
import com.intel.missioncontrol.modules.MapModule;
import eu.mavinci.desktop.gui.wwext.ToolTipLayer;
import org.asyncfx.concurrent.Dispatcher;

@LayerDefaults(internal = true)
public class TooltipLayer extends WWLayerWrapper {

    @Inject
    TooltipLayer(@Named(MapModule.DISPATCHER) Dispatcher dispatcher, WorldWindowProvider worldWindowProvider) {
        super(new ToolTipLayer(dispatcher), dispatcher);
        ToolTipLayer layer = (ToolTipLayer)getWrappedLayer();
        worldWindowProvider.whenAvailable(layer::setWorldWindow);
    }

}
