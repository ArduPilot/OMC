/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.grids;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.PublishSource;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.modules.MapModule;
import eu.mavinci.core.obfuscation.IKeepAll;
import org.asyncfx.concurrent.SynchronizationRoot;

@LayerDefaults(name = "%com.intel.missioncontrol.map.worldwind.layers.grids.ContourLinesLayer", enabled = false)
@PublishSource(module = "World Wind", licenses = "nasa-world-wind")
public class ContourLinesLayer extends WWLayerWrapper implements IKeepAll {

    @Inject
    ContourLinesLayer(@Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot) {
        super(new eu.mavinci.desktop.gui.doublepanel.planemain.wwd.ContourLinesLayer(), syncRoot);
    }

}
