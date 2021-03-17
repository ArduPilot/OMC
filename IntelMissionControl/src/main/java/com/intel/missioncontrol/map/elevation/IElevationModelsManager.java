/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.elevation;

import org.asyncfx.beans.property.AsyncBooleanProperty;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;

public interface IElevationModelsManager {

    AsyncListProperty<IElevationLayer> layersProperty();

    AsyncBooleanProperty terrainEnabledProperty();

    AsyncBooleanProperty useTerrainBaselayerProperty();

    AsyncBooleanProperty useGeoTiffsProperty();

    IElevationLayer baseLayerProperty();

    void register(ReadOnlyAsyncListProperty<IElevationLayer> elevationModels);
}
