/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.settings.KmlSettings;
import gov.nasa.worldwind.layers.Layer;

public class KmlLayerWrapper extends WWLayerWrapper {

    private final KmlSettings settings;

    public KmlLayerWrapper(Layer wwLayer, SynchronizationRoot syncRoot, KmlSettings settings) {
        super(wwLayer, syncRoot);
        this.settings = settings;
    }

    public KmlSettings getSettings() {
        return settings;
    }
}
