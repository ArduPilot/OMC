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
import org.asyncfx.concurrent.SynchronizationRoot;

@LayerDefaults(name = "%com.intel.missioncontrol.map.worldwind.layers.grids.MGRSGraticuleLayer", enabled = false)
public class MGRSGraticuleLayer extends WWLayerWrapper implements IKeepAll {

    @Inject
    MGRSGraticuleLayer(@Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot) {
        super(new gov.nasa.worldwind.layers.Earth.MGRSGraticuleLayer(), syncRoot);
    }

}
