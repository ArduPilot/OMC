/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.map.geotiff;

import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.ReadOnlyAsyncListProperty;
import com.intel.missioncontrol.concurrent.Dispatcher;
import com.intel.missioncontrol.concurrent.FluentFuture;
import com.intel.missioncontrol.map.ILayer;
import com.intel.missioncontrol.map.elevation.IElevationLayer;
import java.io.File;

public class MockGeoTiffManager implements IGeoTiffManager {
    @Override
    public FluentFuture<GeoTiffEntry> importGeoTiff(GeoTiffEntry geoTiffEntry) {
        return Dispatcher.post(
            () -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return geoTiffEntry;
            });
    }

    @Override
    public void addGeoTiff(File geoTiffFile) {}

    @Override
    public ReadOnlyAsyncListProperty<ILayer> imageryLayersProperty() {
        return null;
    }

    @Override
    public ReadOnlyAsyncListProperty<IElevationLayer> elevationLayersProperty() {
        return null;
    }

    @Override
    public AsyncListProperty<GeoTiffEntry> geoTiffEntriesProperty() {
        return null;
    }

    @Override
    public void dropGeotiffImport(GeoTiffEntry geoTiffEntry) {}

}
