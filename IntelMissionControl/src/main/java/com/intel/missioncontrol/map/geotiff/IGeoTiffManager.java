/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.geotiff;

import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.elevation.IElevationLayer;
import java.io.File;
import org.asyncfx.beans.property.AsyncListProperty;
import org.asyncfx.beans.property.ReadOnlyAsyncListProperty;
import org.asyncfx.concurrent.Future;

public interface IGeoTiffManager {

    Future<GeoTiffEntry> importGeoTiffAsync(GeoTiffEntry geoTiffEntry);

    public void addGeoTiff(File geoTiffFile);

    public ReadOnlyAsyncListProperty<ILayer> imageryLayersProperty();

    public ReadOnlyAsyncListProperty<IElevationLayer> elevationLayersProperty();

    public AsyncListProperty<GeoTiffEntry> geoTiffEntriesProperty();

    public void dropGeotiffImport(GeoTiffEntry geoTiffEntry);

}
