/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.airmap;

import com.intel.missioncontrol.airspace.render.TiledRenderableLayer;
import org.asyncfx.concurrent.SynchronizationRoot;

public class AirMap2Layer extends TiledRenderableLayer {

    public AirMap2Layer(SynchronizationRoot syncRoot) {
        super(AirMap2Source.getInstance(), syncRoot);
        setMaxActiveAltitude(70_000);
    }

}
