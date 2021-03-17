/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.map.ISelectionManager;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.worldwind.IWWGlobes;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.mission.IMissionManager;
import com.intel.missioncontrol.modules.MapModule;
import org.asyncfx.concurrent.SynchronizationRoot;

@LayerDefaults(internal = true)
public class MissionOverviewLayer extends WWLayerWrapper {

    @Inject
    MissionOverviewLayer(
            @Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot,
            IWWGlobes globes,
            IMissionManager missionManager,
            ISelectionManager selectionManager) {
        super(
            new eu.mavinci.desktop.gui.doublepanel.planemain.wwd.MissionOverviewLayer(
                missionManager, globes.getDefaultGlobe(), syncRoot, selectionManager),
            syncRoot);
    }

}
