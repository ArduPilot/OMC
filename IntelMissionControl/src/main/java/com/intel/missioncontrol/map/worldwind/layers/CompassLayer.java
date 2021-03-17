/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.modules.MapModule;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Vec4;
import org.asyncfx.concurrent.Dispatcher;

@LayerDefaults(internal = true)
public class CompassLayer extends WWLayerWrapper {

    @Inject
    CompassLayer(@Named(MapModule.DISPATCHER) Dispatcher dispatcher) {
        super(new gov.nasa.worldwind.layers.CompassLayer(), dispatcher);
        gov.nasa.worldwind.layers.CompassLayer compassLayer = (gov.nasa.worldwind.layers.CompassLayer)getWrappedLayer();
        compassLayer.setResizeBehavior(AVKey.RESIZE_SHRINK_ONLY);
        compassLayer.setIconScale(0.5);
        compassLayer.setPosition(AVKey.NORTHWEST);
        compassLayer.setLocationOffset(new Vec4(ScaleHelper.emsToPixels(2), 0));

        ScaleHelper.scalePropProperty()
            .addListener(
                (observable, oldVal, newVal) -> {
                    compassLayer.setIconScale(0.5);
                });
    }

}
