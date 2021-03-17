/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers.airTraffic;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.airtraffic.IAirTrafficManager;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.elevation.IEgmModel;
import com.intel.missioncontrol.map.elevation.IElevationModel;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.modules.MapModule;
import eu.mavinci.core.obfuscation.IKeepAll;
import org.asyncfx.concurrent.Dispatcher;

@LayerDefaults(name = "%com.intel.missioncontrol.map.worldwind.layers.grids.AirTrafficLayerWW", enabled = false)
public class AirTrafficLayer extends WWLayerWrapper implements IKeepAll {

    @Inject
    public AirTrafficLayer(
            @Named(MapModule.DISPATCHER) Dispatcher dispatcher,
            IAirTrafficManager atm,
            IElevationModel elevationModel,
            IEgmModel egmModel) {
        super(new AirTrafficLayerWW(atm, elevationModel, egmModel), dispatcher);
    }

}
