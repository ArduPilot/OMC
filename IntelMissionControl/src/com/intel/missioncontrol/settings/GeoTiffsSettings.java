/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.settings;

import com.intel.missioncontrol.beans.AsyncObservable;
import com.intel.missioncontrol.beans.property.AsyncListProperty;
import com.intel.missioncontrol.beans.property.PropertyMetadata;
import com.intel.missioncontrol.beans.property.SimpleAsyncListProperty;
import com.intel.missioncontrol.collections.AsyncObservableList;
import com.intel.missioncontrol.collections.FXAsyncCollections;

@SettingsMetadata(section = "geoTiffsLoaded")
public class GeoTiffsSettings implements ISettings {

    private AsyncListProperty<GeoTiffSettings> geoTiffs =
        new SimpleAsyncListProperty<>(
            this,
            new PropertyMetadata.Builder<AsyncObservableList<GeoTiffSettings>>()
                .initialValue(
                    FXAsyncCollections.observableArrayList(
                        geoTiff ->
                            new AsyncObservable[] {
                                geoTiff.enabledProperty(),
                                geoTiff.cacheNameProperty(),
                                geoTiff.manualElevationShiftProperty(),
                                geoTiff.elevationModelShiftTypeProperty()
                            }))
                .create());

    public AsyncListProperty<GeoTiffSettings> geoTiffsProperty() {
        return geoTiffs;
    }

}
