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
import org.asyncfx.concurrent.Dispatcher;
import org.asyncfx.concurrent.Future;

public class MockGeoTiffManager implements IGeoTiffManager {
    @Override
    public Future<GeoTiffEntry> importGeoTiffAsync(GeoTiffEntry geoTiffEntry) {
        Dispatcher dispatcher = Dispatcher.background();
        return dispatcher.getLaterAsync(
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
