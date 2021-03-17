/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.grids;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.modules.MapModule;
import eu.mavinci.core.obfuscation.IKeepAll;

@LayerDefaults(name = "%com.intel.missioncontrol.map.worldwind.layers.grids.UTMGraticuleLayer", enabled = false)
public class UTMGraticuleLayer extends WWLayerWrapper implements IKeepAll {

    @Inject
    UTMGraticuleLayer(@Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot) {
        super(new gov.nasa.worldwind.layers.Earth.UTMGraticuleLayer(), syncRoot);
    }

}
