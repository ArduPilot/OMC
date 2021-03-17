/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind.layers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.intel.missioncontrol.helper.FontHelper;
import com.intel.missioncontrol.helper.ScaleHelper;
import com.intel.missioncontrol.map.LayerDefaults;
import com.intel.missioncontrol.map.worldwind.WWLayerWrapper;
import com.intel.missioncontrol.modules.MapModule;
import gov.nasa.worldwind.geom.Vec4;
import org.asyncfx.concurrent.SynchronizationRoot;

@LayerDefaults(internal = true)
public class ScalebarLayer extends WWLayerWrapper {

    @Inject
    ScalebarLayer(@Named(MapModule.SYNC_ROOT) SynchronizationRoot syncRoot) {
        super(new gov.nasa.worldwind.layers.ScalebarLayer(), syncRoot);
        gov.nasa.worldwind.layers.ScalebarLayer scalebarLayer =
            (gov.nasa.worldwind.layers.ScalebarLayer)getWrappedLayer();
        updateWidgetScale(scalebarLayer);

        ScaleHelper.scalePropProperty()
            .addListener(
                (observable, oldVal, newVal) -> {
                    updateWidgetScale(scalebarLayer);
                });
    }

    private void updateWidgetScale(gov.nasa.worldwind.layers.ScalebarLayer scalebarLayer) {
        scalebarLayer.setDisplayScaleFactor(ScaleHelper.getScaleFactor());
        scalebarLayer.setFont(FontHelper.getBaseFont(0.833));
        scalebarLayer.setLocationOffset(new Vec4(ScaleHelper.emsToPixels(-5), 0));
    }

}
