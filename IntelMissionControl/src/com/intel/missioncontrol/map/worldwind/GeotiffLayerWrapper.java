/*
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.worldwind;

import com.intel.missioncontrol.concurrent.SynchronizationRoot;
import com.intel.missioncontrol.settings.GeoTiffSettings;
import gov.nasa.worldwind.layers.Layer;

public class GeotiffLayerWrapper extends WWLayerWrapper {

    private final GeoTiffSettings geoTiffSettings;

    public GeotiffLayerWrapper(Layer wwLayer, SynchronizationRoot syncRoot, GeoTiffSettings settings) {
        super(wwLayer, syncRoot);
        this.geoTiffSettings = settings;
    }

    public GeoTiffSettings getGeoTiffSettings() {
        return geoTiffSettings;
    }
}
